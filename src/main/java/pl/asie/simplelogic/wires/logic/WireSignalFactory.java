package pl.asie.simplelogic.wires.logic;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.simplelogic.wires.OldWireUtils;

public class WireSignalFactory extends WireProvider {
    public final WireType type;
    public final int color;

    public WireSignalFactory(WireType type, int color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public Wire create(IWireContainer container, WireFace location) {
        PartWireSignalBase wire = null;
        switch (type) {
            case NORMAL:
                wire = new PartWireNormal(container, this, location);
                break;
            case INSULATED:
                wire = new PartWireInsulated(container, this, location);
                wire.setColor(color);
                break;
            case BUNDLED:
                wire = new PartWireBundled(container, this, location);
                break;
        }

        return wire;
    }

    @Override
    public boolean canPlace(IBlockAccess access, BlockPos pos, WireFace face) {
        return face == WireFace.CENTER || OldWireUtils.canPlaceWire(access, pos.offset(face.facing), face.facing != null ? face.facing.getOpposite() : null);
    }

    @Override
    public float getWidth() {
        return OldWireUtils.width(type) / 16.0F;
    }

    @Override
    public float getHeight() {
        return OldWireUtils.height(type) / 16.0F;
    }

    @Override
    public ResourceLocation getTexturePrefix() {
        return new ResourceLocation("simplelogic:blocks/wire/wire_" + type.name().toLowerCase());
    }
}
