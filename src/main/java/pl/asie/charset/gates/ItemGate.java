package pl.asie.charset.gates;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemGate extends ItemMultiPart {
	public ItemGate() {
		setHasSubtypes(true);
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < ModCharsetGates.gateMeta.length; i++) {
            if (ModCharsetGates.gateMeta[i] != null) {
                subItems.add(new ItemStack(itemIn, 1, i));
            }
        }
    }

    public boolean place(World world, BlockPos pos, EnumFacing side, Vec3 hit, ItemStack stack, EntityPlayer player) {
        if (!world.getBlockState(pos.offset(side)).getBlock().isSideSolid(world, pos.offset(side), side.getOpposite())) {
            return false;
        }

        return super.place(world, pos, side, hit, stack, player);
    }

    public static PartGate getPartGate(int meta) {
        try {
            return ModCharsetGates.gateParts.get(ModCharsetGates.gateMeta[meta]).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IMultipart createPart(World world, BlockPos blockPos, EnumFacing facing, Vec3 vec3, ItemStack stack, EntityPlayer player) {
        PartGate part = getPartGate(stack.getItemDamage());
        return part != null ? part.setSide(facing).setTop(player.getHorizontalFacing()) : null;
    }
}
