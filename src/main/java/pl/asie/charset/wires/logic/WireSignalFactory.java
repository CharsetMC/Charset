package pl.asie.charset.wires.logic;

import mcmultipart.multipart.IMultipart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.wires.PartWire;
import pl.asie.charset.lib.wires.WireFactory;
import pl.asie.charset.wires.WireUtils;

public class WireSignalFactory extends WireFactory {
    public final WireType type;
    public final int color;

    public WireSignalFactory(WireType type, int color) {
        this.type = type;
        this.color = color;
    }

    protected PartWire create() {
        PartWireSignalBase wire = null;
        switch (type) {
            case NORMAL:
                wire = new PartWireNormal();
                wire.setFactory(this);
                break;
            case INSULATED:
                wire = new PartWireInsulated();
                wire.setColor(color);
                wire.setFactory(this);
                break;
            case BUNDLED:
                wire = new PartWireBundled();
                wire.setFactory(this);
                break;
        }

        return wire;
    }

    @Override
    public IMultipart createPart(ResourceLocation loc, boolean client) {
        return create();
    }

    @Override
    public PartWire createPart(ItemStack stack) {
        return create();
    }

    @Override
    public boolean canPlace(IBlockAccess access, BlockPos pos, WireFace face) {
        return WireUtils.canPlaceWire(access, pos, face.facing != null ? face.facing.getOpposite() : null);
    }

    @Override
    public float getWidth() {
        return WireUtils.width(type) / 16.0F;
    }

    @Override
    public float getHeight() {
        return WireUtils.height(type) / 16.0F;
    }

    @Override
    public ResourceLocation getTexturePrefix() {
        return new ResourceLocation("charsetwires:blocks/wire_" + type.name().toLowerCase());
    }
}
