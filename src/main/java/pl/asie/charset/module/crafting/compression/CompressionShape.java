package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.storage.barrels.BarrelUpgrade;
import pl.asie.charset.module.storage.barrels.BlockBarrel;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;
import java.util.*;

public class CompressionShape {
	private static final int MAX_WIDTH = 3;
	private static final int MAX_HEIGHT = 3;

	private static int nextId = 1;

	private final int id;
	private final IBlockAccess world;
	private final Set<BlockPos> positions = new HashSet<>();
	private final Multimap<EnumFacing, BlockPos> expectedFacings = MultimapBuilder.enumKeys(EnumFacing.class).arrayListValues().build();
	private final List<TileCompressionCrafter> compressionCrafters = new ArrayList<>();
	private final List<TileEntityDayBarrel> barrels = new ArrayList<>();
	// private final List<List<BlockPos>> outputPositions = new ArrayList<>();
	private int width, height;
	private BlockPos topLeft;
	private EnumFacing topDir, leftDir, bottomDir, rightDir;
	private Orientation barrelOrientation;
	private boolean invalid = false;
	private boolean[] lastRedstoneLevels = new boolean[6];

	private CompressionShape(IBlockAccess world) {
		this.id = nextId++;
		this.world = world;
	}

	protected Set<EnumFacing> checkRedstoneLevels() {
		EnumSet<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing facing : EnumFacing.VALUES) {
			boolean lastLevel = lastRedstoneLevels[facing.ordinal()];
			boolean currLevel = getRedstoneLevel(facing);
			if (currLevel && !lastLevel) {
				set.add(facing);
			}
			lastRedstoneLevels[facing.ordinal()] = currLevel;
		}

		return set;
	}

	private boolean getRedstoneLevel(EnumFacing side) {
		for (TileCompressionCrafter tile : compressionCrafters) {
			if (expectedFacings.get(side).contains(tile.getPos())) {
				if (tile.redstoneLevel) {
					return true;
				}
			}
		}

		return false;
	}

	public Orientation getBarrelOrientation() {
		return barrelOrientation;
	}

	public boolean isInvalid() {
		if (invalid) {
			return true;
		}

		for (Map.Entry<EnumFacing, BlockPos> entry : expectedFacings.entries()) {
			if (getCrafterDirection(entry.getValue()) != entry.getKey()) {
				return true;
			}
		}

		for (TileEntityDayBarrel barrel : barrels) {
			if (barrel.isInvalid() || barrel.orientation != barrelOrientation) {
				return true;
			}
		}

		return false;
	}

	private void addItemHandlers(Collection<IItemInsertionHandler> outputs, EnumFacing sourceDir, Collection<BlockPos> positions) {
		for (BlockPos sourcePos : positions) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (facing == sourceDir) continue;

				BlockPos spos = sourcePos.offset(facing);
				if (!positions.contains(spos)) {
					IItemInsertionHandler output = CapabilityHelper.get(
							world, spos, Capabilities.ITEM_INSERTION_HANDLER, facing.getOpposite(),
							false, true, false
					);
					if (output != null) {
						outputs.add(output);
					}
				}
			}
		}
	}

	private void outputStack(ItemStack stack, BlockPos sourcePos, EnumFacing sourceDir, Collection<IItemInsertionHandler> outputs) {
		for (IItemInsertionHandler output : outputs) {
			if (stack.isEmpty()) break;
			stack = output.insertItem(stack, false);
		}

		if (!stack.isEmpty()) {
			ItemUtils.spawnItemEntity(
					((World) world), new Vec3d(sourcePos.offset(sourceDir.getOpposite())).addVector(0.5, 0.5, 0.5), stack, 0, 0, 0, 0
			);
		}
	}

	public ItemStack craft(BlockPos sourcePos, EnumFacing sourceDir, boolean simulate) {
		Set<EnumFacing> validSides = EnumSet.noneOf(EnumFacing.class);
		if (!simulate) {
			validSides = checkRedstoneLevels();
			if (validSides.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		InventoryCrafting crafting = RecipeUtils.getCraftingInventory(width, height);
		boolean hasNonEmpty = false;

		for (int i = 0; i < width * height; i++) {
			ItemStack stack = barrels.get(i).item;
			if (!stack.isEmpty()) {
				hasNonEmpty = true;
				boolean copied = false;
				if (barrels.get(i).upgrades.contains(BarrelUpgrade.STICKY)) {
					stack = stack.copy();
					stack.shrink(1);
					copied = true;
				}
				if (stack.getCount() > 1) {
					if (!copied) {
						stack = stack.copy();
						copied = true;
					}
					stack.setCount(1);
				}
			}
			crafting.setInventorySlotContents(i, stack);
		}

		if (!hasNonEmpty) {
			return ItemStack.EMPTY;
		}

		World recipeWorld = world instanceof World ? (World) world : null;
		IRecipe recipe = RecipeUtils.findMatchingRecipe(crafting, recipeWorld);
		if (recipe == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = recipe.getCraftingResult(crafting);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (!simulate) {
			List<IItemInsertionHandler> outputs = new ArrayList<>();
			for (EnumFacing facing : validSides) {
				addItemHandlers(outputs, facing, expectedFacings.get(facing));
			}

			NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(crafting);
			for (int i = 0; i < width * height; i++) {
				ItemStack source = barrels.get(i).item;
				ItemStack target = remainingItems.get(i);

				if (!source.isEmpty() && !barrels.get(i).upgrades.contains(BarrelUpgrade.INFINITE)) {
					source.shrink(1);
				}

				if (target.isEmpty()) {
					// we're fine
				} else if (source.isEmpty()) {
					source = target;
				} else if (ItemUtils.canMerge(source, target)) {
					source.grow(target.getCount());
				} else {
					outputStack(target, sourcePos, sourceDir, outputs);
				}

				barrels.get(i).setItem(source);
			}
			outputStack(stack.copy(), sourcePos, sourceDir, outputs);
		}

		return stack;
	}

	private boolean setCrafterShapeIfMatchesDirection(BlockPos pos, EnumFacing facing) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter) {
			if (state.getValue(Properties.FACING) == facing) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile instanceof TileCompressionCrafter) {
					((TileCompressionCrafter) tile).shape = this;
					compressionCrafters.add((TileCompressionCrafter) tile);
					positions.add(pos);
					expectedFacings.put(facing, pos);
					return true;
				}
			}
		}

		return false;
	}

	@Nullable
	private EnumFacing getCrafterDirection(BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter) {
			return state.getValue(Properties.FACING);
		} else {
			return null;
		}
	}

	private TileEntityDayBarrel getBarrel(BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityDayBarrel ? (TileEntityDayBarrel) tile : null;
	}

	public static CompressionShape build(IBlockAccess world, BlockPos start) {
		CompressionShape shape = new CompressionShape(world);
		EnumFacing firstBarrelFacing = shape.getCrafterDirection(start);
		if (firstBarrelFacing == null) {
			return null;
		}

		BlockPos firstBarrelPos = start.offset(firstBarrelFacing);
		TileEntity tile = world.getTileEntity(firstBarrelPos);
		if (tile instanceof TileEntityDayBarrel) {
			shape.barrelOrientation = ((TileEntityDayBarrel) tile).orientation;
		} else {
			return null;
		}

		// first, find the topleftmost barrel
		BlockPos topLeft = firstBarrelPos;
		EnumFacing topDir = shape.barrelOrientation.top;

		// go up
		while (shape.getBarrel(topLeft.offset(topDir)) != null) {
			topLeft = topLeft.offset(topDir);
		}

		// go to the left
		EnumFacing leftDir = shape.barrelOrientation.getPrevRotationOnFace().top;
		while (shape.getBarrel(topLeft.offset(leftDir)) != null) {
			topLeft = topLeft.offset(leftDir);
		}

		// now calculate the width and the height
		int width = 1;
		BlockPos tmp = topLeft;
		while (shape.getBarrel(tmp.offset(leftDir.getOpposite())) != null) {
			tmp = tmp.offset(leftDir.getOpposite());
			width++;
			if (width > MAX_WIDTH) {
				return null;
			}
		}

		int height = 1;
		while (true) {
			tmp = topLeft.offset(topDir.getOpposite(), height);
			int wDetected = 0;
			while (shape.getBarrel(tmp) != null) {
				tmp = tmp.offset(leftDir.getOpposite());
				wDetected++;
			}
			if (wDetected == 0) {
				// last line
				break;
			} else if (wDetected != width) {
				// width disparity!
				return null;
			} else {
				height++;
				if (height > MAX_HEIGHT) {
					return null;
				}
			}
		}

		// now create an array and add all the barrels in
		shape.width = width;
		shape.height = height;

		shape.topDir = topDir;
		shape.leftDir = leftDir;
		shape.bottomDir = topDir.getOpposite();
		shape.rightDir = leftDir.getOpposite();
		shape.topLeft = topLeft;

		for (int yPos = 0; yPos < height; yPos++) {
			tmp = topLeft.offset(shape.bottomDir, yPos);
			for (int xPos = 0; xPos < width; xPos++) {
				// add barrel
				TileEntityDayBarrel barrel = shape.getBarrel(tmp);
				if (barrel == null) {
					ModCharset.logger.warn("Should never happen!", new Throwable());
					return null;
				}
				shape.barrels.add(barrel);
				shape.positions.add(tmp);

				// check sides
				if (xPos == 0 && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(leftDir), shape.rightDir)) {
					return null;
				} else if (xPos == (width - 1) && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(shape.rightDir), leftDir)) {
					return null;
				}

				if (yPos == 0 && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(topDir), shape.bottomDir)) {
					return null;
				} else if (yPos == (height - 1) && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(shape.bottomDir), topDir)) {
					return null;
				}

				tmp = tmp.offset(shape.rightDir);
			}
		}

		return shape;
	}
}
