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

package pl.asie.charset.lib.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.ModCharsetStorage;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;

public abstract class BlockBase extends Block {
	// TODO: Move me!
	private static final MethodHandle EXPLOSION_SIZE_GETTER;
	private boolean isTileProvider = this instanceof ITileEntityProvider;
	private ImmutableList<ItemStack> items;

	static {
		try {
			EXPLOSION_SIZE_GETTER = MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(Explosion.class, "explosionSize", "field_77280_f"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public BlockBase(Material materialIn) {
		super(materialIn);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (isTileProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBase) {
				return ((TileBase) tile).getPickedBlock();
			}
		}

		return new ItemStack(this);
	}

	protected Collection<ItemStack> getCreativeItems() {
		return ImmutableList.of(new ItemStack(this));
	}

	protected List<Collection<ItemStack>> getCreativeItemSets() {
		return Collections.EMPTY_LIST;
	}

	protected int getCreativeItemSetAmount() {
		return 1;
	}

	@Override
	public void getSubBlocks(Item me, CreativeTabs tab, NonNullList<ItemStack> itemList) {
		if (items == null) {
			ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
			builder.addAll(getCreativeItems());
			List<Collection<ItemStack>> sets = getCreativeItemSets();

			if (sets.size() > 0) {
				if (ModCharsetLib.INDEV) {
					for (Collection<ItemStack> set : sets)
						builder.addAll(set);
				} else {
					Calendar cal = ModCharsetLib.calendar.get();
					int doy = cal.get(Calendar.DAY_OF_YEAR) - 1 /* start at 0, not 1 */;
					Collections.shuffle(sets, new Random(doy));
					for (int i = 0; i < Math.min(getCreativeItemSetAmount(), sets.size()); i++)
						builder.addAll(sets.get(i));
				}
			}

			items = builder.build();
		}

		itemList.addAll(items);
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
		onBlockDestroyedByExplosion(world, pos, explosion);

		IBlockState state = world.getBlockState(pos);
		TileEntity tile = world.getTileEntity(pos);

		world.setBlockToAir(pos);

		List<ItemStack> items = getDrops(world, pos, state, tile, 0, false);
		float chance = 1.0f;
		try {
			chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f / (float) EXPLOSION_SIZE_GETTER.invokeExact(explosion), true, null);
		} catch (Throwable t) { t.printStackTrace(); }

		for (ItemStack item : items)
			if (world.rand.nextFloat() <= chance)
				spawnAsEntity(world, pos, item);
	}

	@Override
	public final List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return getDrops(world, pos, state, null, fortune, false);
	}

	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, @Nullable TileEntity te, int fortune, boolean silkTouch) {
		if (te instanceof TileBase) {
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			stacks.add(((TileBase) te).getDroppedBlock());
			return stacks;
		}

		return super.getDrops(world, pos, state, fortune);
	}
}
