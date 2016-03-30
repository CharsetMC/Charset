package pl.asie.charset.wires;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
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
		super();
		setHasSubtypes(true);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public boolean place(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		if (!isFreestanding(stack) && !WireUtils.canPlaceWire(world, pos.offset(side), side.getOpposite())) {
			return false;
		}

		return super.place(world, pos, side, hit, stack, player);
	}

	@Override
	public IMultipart createPart(World world, BlockPos blockPos, EnumFacing facing, Vec3d vec3, ItemStack stack, EntityPlayer player) {
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
				name = I18n.translateToLocal("tile.charset.wire.name");
				break;
			case INSULATED:
				name = String.format(I18n.translateToLocal("tile.charset.wire.insulated.suffix"), I18n.translateToLocal("charset.color." + EnumDyeColor.byMetadata(kind.color()).getUnlocalizedName()));
				break;
			case BUNDLED:
				name = I18n.translateToLocal("tile.charset.wire.bundled.name");
				break;
		}

		if (isFreestanding(stack)) {
			name = String.format(I18n.translateToLocal("tile.charset.wire.freestanding.prefix"), name);
		}

		return name;
	}

	public static boolean isFreestanding(ItemStack stack) {
		return (stack.getItemDamage() & 1) == 1;
	}
}
