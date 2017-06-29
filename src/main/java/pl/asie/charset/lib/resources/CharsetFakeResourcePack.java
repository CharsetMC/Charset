package pl.asie.charset.lib.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CharsetFakeResourcePack implements IResourcePack, IResourceManagerReloadListener {
    public static final CharsetFakeResourcePack INSTANCE = new CharsetFakeResourcePack();

    private final Map<ResourceLocation, byte[]> data = new HashMap<>();
    private final Map<ResourceLocation, Consumer<DataOutputStream>> entries = new HashMap<>();
    private final Set<String> domains = ImmutableSet.of("charset_generated");

    private CharsetFakeResourcePack() {

    }

    public void registerEntry(ResourceLocation location, Consumer<DataOutputStream> provider) {
        entries.put(location, provider);
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        if (!data.containsKey(location)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            entries.get(location).accept(dataOutputStream);
            dataOutputStream.close();
            byteArrayOutputStream.close();

            byte[] out = byteArrayOutputStream.toByteArray();
            data.put(location, out);
            return new ByteArrayInputStream(out);
        } else {
            return new ByteArrayInputStream(data.get(location));
        }
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return entries.containsKey(location);
    }

    @Override
    public Set<String> getResourceDomains() {
        return domains;
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "CharsetFakePack";
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        data.clear();
    }
}
