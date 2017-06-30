package pl.asie.charset.lib.wires;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pl.asie.charset.api.wires.WireFace;

public class ItemWire extends ItemBlockMultipart {
    public ItemWire(Block block) {
        super(block, (IMultipart) block);
        setHasSubtypes(true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Wire wire = fromStack(new IWireContainer.Dummy(), stack, EnumFacing.DOWN);
        String tr = "tile.wire.null";
        if (wire != null) {
            String name = wire.getDisplayName();
            tr = "tile." + (wire.getLocation() == WireFace.CENTER ? name + ".freestanding" : name) + ".name";
        }
        return I18n.translateToLocal(tr);
    }

    public Wire fromStack(IWireContainer container, ItemStack stack, EnumFacing facing) {
        WireProvider factory = WireManager.REGISTRY.getValue(stack.getMetadata() >> 1);
        if (factory != null) {
            WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing);
            return factory.create(container, location);
        } else {
            return null;
        }
    }

    public ItemStack toStack(WireProvider provider, boolean freestanding, int amount) {
        return new ItemStack(this, amount, (WireManager.REGISTRY.getID(provider) << 1) | (freestanding ? 1 : 0));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        return place(player, world, pos, hand, facing, hitX, hitY, hitZ, this, this.block::getStateForPlacement, multipartBlock,
                this::placeBlockAtTested, this::placeWirePartAt);
    }

    public boolean placeWirePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                      float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
        WireProvider factory = WireManager.REGISTRY.getValue(stack.getMetadata() >> 1);
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        if (!factory.canPlace(world, pos, location)) {
            return false;
        }

        return ItemBlockMultipart.placePartAt(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, multipartBlock, state);
    }

    @Override
    public boolean placeBlockAtTested(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX,
                                      float hitY, float hitZ, IBlockState newState) {
        WireProvider factory = WireManager.REGISTRY.getValue(stack.getMetadata() >> 1);
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
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInCreativeTab(tab)) {
            for (WireProvider provider : WireManager.REGISTRY.getValues()) {
                int id = WireManager.REGISTRY.getID(provider);
                if (provider.hasSidedWire()) {
                    subItems.add(new ItemStack(this, 1, id * 2));
                }
                if (provider.hasFreestandingWire()) {
                    subItems.add(new ItemStack(this, 1, id * 2 + 1));
                }
            }
        }
    }
}
