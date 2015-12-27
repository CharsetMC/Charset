package pl.asie.charset.gates.render;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by asie on 12/27/15.
 */
public class GateRenderDefinitions {
    public static final GateRenderDefinitions INSTANCE = new GateRenderDefinitions();
    private static final Gson GSON = new Gson();

    public class BaseDefinition {
        public Map<String, Integer> colorMul;
        private Map<String, String> color = new HashMap<String, String>();

        public void onLoad() {
            colorMul = new HashMap<String, Integer>();

            for (String s : color.keySet()) {
                int c = Integer.parseInt(color.get(s), 16);
                colorMul.put(s, 0xFF000000 | (c & 0x00FF00) | ((c & 0xFF0000) >> 16) | ((c & 0x0000FF) << 16));
            }
        }
    }

    public class Definition {
        public List<Layer> layers = new ArrayList<Layer>();
        public List<Torch> torches = new ArrayList<Torch>();
    }

    public class Layer {
        public String type, texture;
    }

    public class Torch {
        public final int[] pos = new int[2];
    }

    public BaseDefinition base;
    private final Map<String, Definition> definitionMap = new HashMap<String, Definition>();

    public Definition getGateDefinition(String type) {
        return definitionMap.get(type);
    }

    public void load(String baseLoc, Map<String, ResourceLocation> definitions) {
        try {
            base = GSON.fromJson(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(baseLoc)).getInputStream()
            ), BaseDefinition.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (base != null) {
            base.onLoad();
        }

        definitionMap.clear();

        for (String s : definitions.keySet()) {
            try {
                definitionMap.put(s, GSON.fromJson(new InputStreamReader(
                        Minecraft.getMinecraft().getResourceManager().getResource(definitions.get(s)).getInputStream()
                ), Definition.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
