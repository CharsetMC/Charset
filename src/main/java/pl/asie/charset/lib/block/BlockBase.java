/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.render.ParticleDiggingCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.lib.utils.UtilProxyCommon;
import pl.asie.charset.lib.utils.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

public abstract class BlockBase extends Block {
	private final boolean isTileProvider;
	private final ISubItemProvider subItemProvider;
	private boolean fullCube = true, opaqueCube = true, comparatorInputOverride = false;

	// I am very forgetful.
	private boolean calledSetHardness = false;

	@Override
	public Block setHardness(float val) {
		calledSetHardness = true;
		return super.setHardness(val);
	}

	public void verifyPreGameLaunch() {
		if (ModCharset.INDEV) {
			if (!calledSetHardness) {
				ModCharset.logger.warn("[INDEV] Modder did not call setHardness on " + getRegistryName() + "!");
			}
		}
	}

	public BlockBase(Material materialIn) {
		super(materialIn);
		isTileProvider = this instanceof ITileEntityProvider;
		subItemProvider = createSubItemProvider();
	}

	public BlockBase(Material materialIn, MapColor color) {
		super(materialIn, color);
		isTileProvider = this instanceof ITileEntityProvider;
		subItemProvider = createSubItemProvider();
	}

	public final ISubItemProvider getSubItemProvider() {
		return subItemProvider;
	}

	protected BlockBase setComparatorInputOverride(boolean value) {
		comparatorInputOverride = value;
		return this;
	}

	protected BlockBase setFullCube(boolean value) {
		fullCube = value;
		return this;
	}

	protected BlockBase setOpaqueCube(boolean value) {
		opaqueCube = value;

		// update affected variables
		this.fullBlock = this.getDefaultState().isOpaqueCube();
		this.lightOpacity = this.fullBlock ? 255 : 0;

		return this;
	}

	protected ISubItemProvider createSubItemProvider() {
		return null;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return comparatorInputOverride;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing facing) {
		return isNormalCube(state, access, pos) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return getBlockFaceShape(world, base_state, pos, side) == BlockFaceShape.SOLID;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return fullCube;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return opaqueCube;
	}

	public int getParticleTintIndex() {
		return -1;
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				return ((TileBase) tile).getPickedBlock(null, null, state);
			}
		}

		return new ItemStack(this);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				return ((TileBase) tile).getPickedBlock(player, target, state);
			}
		}

		return new ItemStack(this);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> itemList) {
		if (subItemProvider != null) {
			itemList.addAll(subItemProvider.getItems());
		} else {
			super.getSubBlocks(tab, itemList);
		}
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		if (isTileProvider) {
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileBase) {
				return ((TileBase) tile).getComparatorValue();
			}
		}
		return 0;
	}

	@Override
	public final void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		onBlockPlacedBy(world, pos, state, placer, stack, null, 0.5f, 0.5f, 0.5f);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, @Nullable EnumFacing face, float hitX, float hitY, float hitZ) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				((TileBase) tile).onPlacedBy(placer, face, stack, hitX, hitY, hitZ);
			}
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);
		int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;

		NonNullList<ItemStack> items = NonNullList.create();
		getDrops(items, worldIn, pos, state, te, fortuneLevel, silkTouch);
		float chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, fortuneLevel, 1.0f, silkTouch, player);

		harvesters.set(player);

		if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
			for (ItemStack item : items)
				if (chance >= 1.0f || worldIn.rand.nextFloat() <= chance)
					spawnAsEntity(worldIn, pos, item);
		}

		harvesters.set(null);
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosionIn) {
		return false; // custom handling to preserve TE
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		IBlockState stateOld = world.getBlockState(pos);

		onExplosionDestroy(world, pos, explosion);

		IBlockState state = world.getBlockState(pos);
		TileEntity tile = world.getTileEntity(pos);

		if (stateOld == state) {
			world.setBlockToAir(pos);

			NonNullList<ItemStack> items = NonNullList.create();
			getDrops(items, world, pos, state, tile, 0, false);
			float chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f / Utils.getExplosionSize(explosion), true, null);

			for (ItemStack item : items)
				if (world.rand.nextFloat() <= chance)
					spawnAsEntity(world, pos, item);
		} else {
			// The block seems to have been replaced with something.
			// Don't do anything.
		}
	}

	@Override
	public final void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		getDrops(drops, world, pos, state, world.getTileEntity(pos), fortune, false);
	}

	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, @Nullable TileEntity te, int fortune, boolean silkTouch) {
		if (te instanceof TileBase) {
			((TileBase) te).getDrops(drops, state, fortune, silkTouch);
		} else {
			super.getDrops(drops, world, pos, state, fortune);
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
		if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
			NonNullList<ItemStack> drops = NonNullList.create();
			getDrops(drops, worldIn, pos, state, worldIn.getTileEntity(pos), fortune, false);
			chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, chance, false, harvesters.get());

			for (ItemStack drop : drops) {
				if (worldIn.rand.nextFloat() <= chance) {
					spawnAsEntity(worldIn, pos, drop);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		IBlockState state = world.getBlockState(pos);
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		if (model instanceof IStateParticleBakedModel) {
			state = getExtendedState(state.getActualState(world, pos), world, pos);
			TextureAtlasSprite sprite = ((IStateParticleBakedModel) model).getParticleTexture(state, null);
			if (sprite != null) {
				for (int j = 0; j < 4; ++j) {
					for (int k = 0; k < 4; ++k) {
						for (int l = 0; l < 4; ++l) {
							double d0 = ((double)j + 0.5D) / 4.0D;
							double d1 = ((double)k + 0.5D) / 4.0D;
							double d2 = ((double)l + 0.5D) / 4.0D;
							manager.addEffect(new ParticleDiggingCharset(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, state, pos, sprite, getParticleTintIndex()));
						}
					}
				}

				return true;
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		if (model instanceof IStateParticleBakedModel) {
			BlockPos pos = target.getBlockPos();
			EnumFacing side = target.sideHit;

			state = getExtendedState(state.getActualState(world, pos), world, pos);
			TextureAtlasSprite sprite = ((IStateParticleBakedModel) model).getParticleTexture(state, side);
			if (sprite != null) {
				int i = pos.getX();
				int j = pos.getY();
				int k = pos.getZ();
				AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
				double d0 = (double)i + RANDOM.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
				double d1 = (double)j + RANDOM.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
				double d2 = (double)k + RANDOM.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

				if (side == EnumFacing.DOWN)
				{
					d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
				}

				if (side == EnumFacing.UP)
				{
					d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
				}

				if (side == EnumFacing.NORTH)
				{
					d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
				}

				if (side == EnumFacing.SOUTH)
				{
					d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
				}

				if (side == EnumFacing.WEST)
				{
					d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
				}

				if (side == EnumFacing.EAST)
				{
					d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
				}

				Particle particle = new ParticleDiggingCharset(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, state, pos, sprite, getParticleTintIndex())
						.multiplyVelocity(0.2F)
						.multipleParticleScaleBy(0.6F);
				manager.addEffect(particle);

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState stateAgain, EntityLivingBase entity, int numberOfParticles) {
		PacketCustomBlockDust packet = new PacketCustomBlockDust(world, pos, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.15f);
		CharsetLib.packet.sendToDimension(packet, world.provider.getDimension());
		return true;
	}

	@Override
	public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
		return UtilProxyCommon.proxy.addRunningParticles(state, world, pos, entity);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		if (axis != null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof ITileWrenchRotatable) {
				return ((ITileWrenchRotatable) tile).rotateWrench(axis);
			} else {
				return super.rotateBlock(world, pos, axis);
			}
		} else {
			return super.rotateBlock(world, pos, axis);
		}
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		if (state.getPropertyKeys().contains(Properties.FACING)) {
			return state.withProperty(Properties.FACING, rot.rotate(state.getValue(Properties.FACING)));
		} else if (state.getPropertyKeys().contains(Properties.FACING4)) {
			return state.withProperty(Properties.FACING4, rot.rotate(state.getValue(Properties.FACING4)));
		} else if (state.getPropertyKeys().contains(Properties.AXIS) && (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90)) {
			EnumFacing.Axis axis = state.getValue(Properties.AXIS);
			if (axis == EnumFacing.Axis.X) {
				return state.withProperty(Properties.AXIS, EnumFacing.Axis.Z);
			} else if (axis == EnumFacing.Axis.Z) {
				return state.withProperty(Properties.AXIS, EnumFacing.Axis.X);
			} else {
				return state;
			}
		} else {
			return state;
		}
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		if (state.getPropertyKeys().contains(Properties.FACING)) {
			return state.withProperty(Properties.FACING, mirror.mirror(state.getValue(Properties.FACING)));
		} else if (state.getPropertyKeys().contains(Properties.FACING4)) {
			return state.withProperty(Properties.FACING4, mirror.mirror(state.getValue(Properties.FACING4)));
		} else {
			return state;
		}
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int type) {
		if (super.eventReceived(state, worldIn, pos, id, type)) {
			return true;
		}

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile != null) {
			return tile.receiveClientEvent(id, type);
		}

		return false;
	}
}
