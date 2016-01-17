package pl.asie.charset.wires;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.wires.logic.PartWireBase;
import pl.asie.charset.wires.logic.PartWireProvider;

public class ItemWire extends ItemMultiPart {
	public ItemWire() {
		setHasSubtypes(true);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public boolean place(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {
		if (!isFreestanding(stack) && !WireUtils.canPlaceWire(world, pos.offset(side), side.getOpposite())) {
			return false;
		}

		return super.place(world, pos, side, hit, stack, player);
	}

	@Override
	public IMultipart createPart(World world, BlockPos blockPos, EnumFacing facing, Vec3 vec3, ItemStack stack, EntityPlayer player) {
		PartWireBase part = PartWireProvider.createPart(stack.getItemDamage() >> 1);
		part.location = isFreestanding(stack) ? WireFace.CENTER : WireFace.get(facing);
		return part;
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

	public static boolean isFreestanding(ItemStack stack) {
		return (stack.getItemDamage() & 1) == 1;
	}
}
