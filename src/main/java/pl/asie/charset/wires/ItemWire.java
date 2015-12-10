package pl.asie.charset.wires;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.WireFace;

public class ItemWire extends ItemBlock {
	public ItemWire(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < 18 * 2; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		WireKind kind = WireKind.VALUES[stack.getItemDamage() >> 1];
		String name = "";

		switch (kind.type()) {
			case NORMAL:
				name = StatCollector.translateToLocal("tile.charset.wire.name");
				break;
			case INSULATED:
				name = StatCollector.translateToLocal("charset.color." + EnumDyeColor.byMetadata(kind.color()).getUnlocalizedName()) + " " + StatCollector.translateToLocal("tile.charset.wire.insulated.name");
				break;
			case BUNDLED:
				name = StatCollector.translateToLocal("tile.charset.wire.bundled.name");
				break;
		}

		if (isFreestanding(stack)) {
			name = StatCollector.translateToLocal("tile.charset.wire.freestanding") + " " + name;
		}
		
		return name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		if (worldIn.getBlockState(pos).getBlock() instanceof BlockWire || worldIn.getBlockState(pos.offset(side)).getBlock() instanceof BlockWire) {
			return true;
		}

		return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
	}

	public static boolean isFreestanding(ItemStack stack) {
		return (stack.getItemDamage() & 1) == 1;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (stack.stackSize == 0) {
			return false;
		}

		IBlockState state = world.getBlockState(pos);
		Block blockN = state.getBlock();

		if (!(blockN instanceof BlockWire) && !blockN.isReplaceable(world, pos)) {
			pos = pos.offset(side);
		}

		EnumFacing pSide = side.getOpposite();
		WireFace wpSide = isFreestanding(stack) ? WireFace.CENTER : WireFace.get(pSide);

		state = world.getBlockState(pos);
		blockN = state.getBlock();

		if (blockN instanceof BlockWire) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileWireContainer) {
				if (((TileWireContainer) tileEntity).addWire(wpSide, stack.getItemDamage())) {
					stack.stackSize--;
					return true;
				}
			}

			pos = pos.offset(side);
		}

		if (!isFreestanding(stack) && !WireUtils.canPlaceWire(world, pos.offset(pSide), pSide.getOpposite())) {
			return false;
		}

		if (super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ)) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (world.getTileEntity(pos) == null) {
				tileEntity = block.createTileEntity(world, world.getBlockState(pos));
				world.setTileEntity(pos, tileEntity);
			}

			((TileWireContainer) tileEntity).addWire(wpSide, stack.getItemDamage());

			return true;
		} else {
			return false;
		}
	}
}
