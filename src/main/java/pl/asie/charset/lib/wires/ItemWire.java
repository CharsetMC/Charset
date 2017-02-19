package pl.asie.charset.lib.wires;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.utils.RegistryUtils;

public class ItemWire extends ItemBlockMultipart {
    public ItemWire(Block block) {
        super(block, (IMultipart) block);
    }

    public Wire fromStack(IWireContainer container, ItemStack stack, EnumFacing facing) {
        WireProvider factory = WireManager.REGISTRY.getObjectById(stack.getMetadata() >> 1);
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing);
        return factory.create(container, location);
    }

    @Override
    public boolean placeBlockAtTested(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX,
                                      float hitY, float hitZ, IBlockState newState) {
        // No block form!
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (WireProvider provider : WireManager.REGISTRY.getValues()) {
            int id = WireManager.REGISTRY.getId(provider);
            subItems.add(new ItemStack(this, 1, id * 2));
            subItems.add(new ItemStack(this, 1, id * 2 + 1));
        }
    }
}
