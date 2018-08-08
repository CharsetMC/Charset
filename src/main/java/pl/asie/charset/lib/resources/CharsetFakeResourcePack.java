/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO: FakeResourcePack class with in-ModCharset/in-CharsetLib? instance
public class CharsetFakeResourcePack implements IResourcePack, ISelectiveResourceReloadListener {
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
        invalidate();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        Iterator<ResourceLocation> keys = data.keySet().iterator();
        while (keys.hasNext()) {
            ResourceLocation key = keys.next();
            String kp = key.getPath();
            boolean remove = true;

            if (kp.startsWith("lang/") || kp.startsWith("texts/")) {
                remove = resourcePredicate.test(VanillaResourceType.LANGUAGES);
            } else if (kp.startsWith("sound")) {
                remove = resourcePredicate.test(VanillaResourceType.SOUNDS);
            } else if (kp.startsWith("textures/") || kp.startsWith("models/") || kp.startsWith("blockstates/")) {
                remove = resourcePredicate.test(VanillaResourceType.MODELS) || resourcePredicate.test(VanillaResourceType.TEXTURES);
            } else if (kp.startsWith("shaders/")) {
                remove = resourcePredicate.test(VanillaResourceType.SHADERS);
            }

            if (remove) {
                keys.remove();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void clearTexturesFromManager() {
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if (manager == null) {
            return;
        }

        for (ResourceLocation location : entries.keySet()) {
            ITextureObject object;
            if (location.getPath().startsWith("textures/") && (object = manager.getTexture(location)) != null) {
                manager.loadTexture(location, object);
            }
        }
    }

    public void invalidate() {
        data.clear();
    }
}
