package pl.asie.charset.module.power.steam.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.steam.TileMirror;

import javax.annotation.Nullable;

public class MirrorColorHandler implements IBlockColor, IItemColor {
    public static final MirrorColorHandler INSTANCE = new MirrorColorHandler();

    private MirrorColorHandler() {

    }

    private int colorMultiplier(@Nullable ItemMaterial material) {
        if (material != null) {
            if (!material.getTypes().contains("block")) {
                ItemMaterial materialBlock = material.getRelated("block");
                if (materialBlock != null) {
                    material = materialBlock;
                }
            }

            return 0xFF000000 | ColorLookupHandler.INSTANCE.getColor(material.getStack(), RenderUtils.AveragingMode.FULL);
        } else {
            return 0xFFFFFFFF;
        }
    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileMirror) {
                return colorMultiplier(((TileMirror) tile).getMaterial());
            } else {
                return colorMultiplier(null);
            }
        } else {
            return -1;
        }
    }

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return colorMultiplier(ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material"));
        } else {
            return -1;
        }
    }
}
