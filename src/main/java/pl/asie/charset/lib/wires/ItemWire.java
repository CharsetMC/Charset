package pl.asie.charset.lib.wires;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        return place(player, world, pos, hand, facing, hitX, hitY, hitZ, this, this.block::getStateForPlacement, multipartBlock,
                this::placeBlockAtTested, this::placeWirePartAt);
    }

    public boolean placeWirePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                      float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
        WireProvider factory = WireManager.REGISTRY.getObjectById(stack.getMetadata() >> 1);
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        if (!factory.canPlace(world, pos, location)) {
            return false;
        }

        return ItemBlockMultipart.placePartAt(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, multipartBlock, state);
    }

    @Override
    public boolean placeBlockAtTested(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX,
                                      float hitY, float hitZ, IBlockState newState) {
        WireProvider factory = WireManager.REGISTRY.getObjectById(stack.getMetadata() >> 1);
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        if (!factory.canPlace(world, pos, location)) {
            return false;
        }

        if (super.placeBlockAtTested(stack, player, world, pos, facing, hitX, hitY, hitZ, newState)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileWire) {
                ((TileWire) tileEntity).onPlacedBy(WireFace.get(facing != null ? facing.getOpposite() : null), stack);
            }
            return true;
        } else {
            return false;
        }
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
