package pl.asie.charset.lib.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.IConnectable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ModelPipeLike<T extends IConnectable> extends BaseBakedModel {
    private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
            {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
    };

    private final ModelRotation[] ROTATIONS = new ModelRotation[]{
            ModelRotation.X0_Y0,
            ModelRotation.X180_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X270_Y180,
            ModelRotation.X270_Y270,
            ModelRotation.X270_Y90
    };

    private final List<BakedQuad>[] lists = new List[65];
    private final CharsetFaceBakery faceBakery = new CharsetFaceBakery();
    private final IUnlistedProperty<T> property;

    public ModelPipeLike(IUnlistedProperty<T> property) {
        this.property = property;

        for (int i = 0; i < 64; i++) {
            boolean[] connections = new boolean[6];
            for (int j = 0; j < 6; j++) {
                if ((i & (1 << j)) != 0) {
                    connections[5 - j] = true;
                }
            }

            lists[i] = ImmutableList.copyOf(generateQuads(connections));
        }

        lists[64] = lists[48];
        addDefaultBlockTransforms();
    }

    public int getOutsideColor(EnumFacing facing) {
        return -1;
    }

    public int getInsideColor(EnumFacing facing) {
        return -1;
    }

    public abstract float getThickness();
    public abstract boolean isOpaque();
    public abstract TextureAtlasSprite getTexture(EnumFacing side, int connectionMatrix);

    @Override
    public abstract TextureAtlasSprite getParticleTexture();

    protected List<BakedQuad> generateQuads(boolean[] connections) {
        List<BakedQuad> quads = new ArrayList<>();
        Vector3f from, to;

        for (EnumFacing facing : EnumFacing.VALUES) {
            EnumFacing[] neighbors = CONNECTION_DIRS[facing.ordinal()];
            int connectionMatrix = (connections[neighbors[0].ordinal()] ? 8 : 0) | (connections[neighbors[1].ordinal()] ? 4 : 0)
                    | (connections[neighbors[2].ordinal()] ? 2 : 0) | (connections[neighbors[3].ordinal()] ? 1 : 0);
            TextureAtlasSprite sprite = getTexture(facing, connectionMatrix);
            float min = 8 - (getThickness() / 2);
            float max = 8 + (getThickness() / 2);
            int outsideColor = getOutsideColor(facing);
            int insideColor = getInsideColor(facing);
            if (!isOpaque() && connections[facing.ordinal()]) {
                // Connected; render up to four quads.
                if (connections[neighbors[2].ordinal()]) {
                    from = new Vector3f(0, min, min);
                    to = new Vector3f(min, min, max);
                    quads.add(faceBakery.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    quads.add(faceBakery.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[0].ordinal()]) {
                    from = new Vector3f(min, min, 0);
                    to = new Vector3f(max, min, min);
                    quads.add(faceBakery.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    quads.add(faceBakery.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[3].ordinal()]) {
                    from = new Vector3f(max, min, min);
                    to = new Vector3f(16, min, max);
                    quads.add(faceBakery.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    quads.add(faceBakery.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[1].ordinal()]) {
                    from = new Vector3f(min, min, max);
                    to = new Vector3f(max, min, 16);
                    quads.add(faceBakery.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    quads.add(faceBakery.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }
            } else {
                // Not connected; render one quad.
                from = new Vector3f(connections[neighbors[2].ordinal()] ? 0 : min, min, connections[neighbors[0].ordinal()] ? 0 : min);
                to = new Vector3f(connections[neighbors[3].ordinal()] ? 16 : max, min, connections[neighbors[1].ordinal()] ? 16 : max);
                quads.add(faceBakery.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                if (!isOpaque()) {
                    quads.add(faceBakery.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        T target = null;
        if (state instanceof IExtendedBlockState) {
            target = ((IExtendedBlockState) state).getValue(property);
        }

        if (target == null) {
            return lists[64];
        }

        int pointer = 0;
        for (EnumFacing f : EnumFacing.VALUES) {
            pointer = (pointer << 1) | (target.connects(f) ? 1 : 0);
        }

        return lists[pointer];
    }
}