package pl.asie.charset.lib.wires;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import pl.asie.charset.api.wires.WireFace;

public abstract class WireFactory implements IForgeRegistryEntry<WireFactory>, IPartFactory {
    private ResourceLocation name;

    public abstract PartWire createPart(ItemStack stack);

    public abstract boolean canPlace(IBlockAccess access, BlockPos pos, WireFace face);
    public abstract float getWidth();
    public abstract float getHeight();
    public abstract ResourceLocation getTexturePrefix();

    @Override
    public WireFactory setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<? super WireFactory> getRegistryType() {
        return WireFactory.class;
    }
}
