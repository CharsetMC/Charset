/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.block;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.render.ParticleDiggingCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BlockBase extends Block {
	private final boolean isTileProvider = this instanceof ITileEntityProvider;
	private final ISubItemProvider subItemProvider;
	private boolean fullCube = true, opaqueCube = true;
	private ImmutableList<ItemStack> items;

	public BlockBase(Material materialIn) {
		super(materialIn);
		subItemProvider = createSubItemProvider();
	}

	public BlockBase(Material materialIn, MapColor color) {
		super(materialIn, color);
		subItemProvider = createSubItemProvider();
	}

	public final ISubItemProvider getSubItemProvider() {
		return subItemProvider;
	}

	protected BlockBase setFullCube(boolean value) {
		fullCube = value;
		return this;
	}

	protected BlockBase setOpaqueCube(boolean value) {
		opaqueCube = value;
		return this;
	}

	protected ISubItemProvider createSubItemProvider() {
		return null;
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
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				return ((TileBase) tile).getPickedBlock(state);
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
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				((TileBase) tile).onPlacedBy(placer, stack);
			}
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);
		int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;

		List<ItemStack> items = getDrops(worldIn, pos, state, te, fortuneLevel, silkTouch);

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

		onBlockDestroyedByExplosion(world, pos, explosion);

		IBlockState state = world.getBlockState(pos);
		TileEntity tile = world.getTileEntity(pos);

		if (stateOld == state) {
			world.setBlockToAir(pos);

			List<ItemStack> items = getDrops(world, pos, state, tile, 0, false);
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
	public final List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return getDrops(world, pos, state, null, fortune, false);
	}

	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, @Nullable TileEntity te, int fortune, boolean silkTouch) {
		if (te instanceof TileBase) {
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			stacks.add(((TileBase) te).getDroppedBlock(state));
			return stacks;
		}

		return super.getDrops(world, pos, state, fortune);
	}

	@Override
	public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
		// TODO
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		IBlockState state = world.getBlockState(pos);
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		if (model instanceof ModelFactory) {
			state = getExtendedState(state.getActualState(world, pos), world, pos);
			TextureAtlasSprite sprite = ((IStateParticleBakedModel) model).getParticleTexture(state);
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
			TextureAtlasSprite sprite = ((IStateParticleBakedModel) model).getParticleTexture(state);
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
}
