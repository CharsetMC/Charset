package pl.asie.charset.module.decoration.stacks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.lib.utils.colorspace.Colorspaces;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RenderTileEntityStacks implements IBakedModel, IStateParticleBakedModel {
	private static final Vec3d[] INGOT_POSITIONS_X, INGOT_POSITIONS_Z;
	private static final Vec3d[][] INGOT_POSITIONS;
	private static final int[][][] QUAD_ORDERS;
	private static final EnumFacing[][] QUAD_FACES;
	private static final int[][] QUAD_UVS;

	static {
		double TOTAL_WIDTH = 4;
		double TOTAL_HEIGHT = 8;
		double Y_SIZE = 2;
		double TRAPEZOID_WIDTH_BOTTOM = 3.5;
		double TRAPEZOID_WIDTH_TOP = 3;
		double TRAPEZOID_HEIGHT_BOTTOM = 8;
		double TRAPEZOID_HEIGHT_TOP = 7;

		double TRAPEZOID_X_NEG_BOT = (TOTAL_WIDTH - TRAPEZOID_WIDTH_BOTTOM) / 2;
		double TRAPEZOID_Y_NEG_BOT = (TOTAL_HEIGHT - TRAPEZOID_HEIGHT_BOTTOM) / 2;
		double TRAPEZOID_X_NEG_TOP = (TOTAL_WIDTH - TRAPEZOID_WIDTH_TOP) / 2;
		double TRAPEZOID_Y_NEG_TOP = (TOTAL_HEIGHT - TRAPEZOID_HEIGHT_TOP) / 2;

		double TRAPEZOID_X_POS_BOT = TRAPEZOID_X_NEG_BOT + TRAPEZOID_WIDTH_BOTTOM;
		double TRAPEZOID_Y_POS_BOT = TRAPEZOID_Y_NEG_BOT + TRAPEZOID_HEIGHT_BOTTOM;
		double TRAPEZOID_X_POS_TOP = TRAPEZOID_X_NEG_TOP + TRAPEZOID_WIDTH_TOP;
		double TRAPEZOID_Y_POS_TOP = TRAPEZOID_Y_NEG_TOP + TRAPEZOID_HEIGHT_TOP;

		INGOT_POSITIONS_X = new Vec3d[]{
				new Vec3d(TRAPEZOID_X_NEG_BOT, 0, TRAPEZOID_Y_NEG_BOT),
				new Vec3d(TRAPEZOID_X_POS_BOT, 0, TRAPEZOID_Y_NEG_BOT),
				new Vec3d(TRAPEZOID_X_POS_BOT, 0, TRAPEZOID_Y_POS_BOT),
				new Vec3d(TRAPEZOID_X_NEG_BOT, 0, TRAPEZOID_Y_POS_BOT),
				new Vec3d(TRAPEZOID_X_NEG_TOP, Y_SIZE, TRAPEZOID_Y_NEG_TOP),
				new Vec3d(TRAPEZOID_X_POS_TOP, Y_SIZE, TRAPEZOID_Y_NEG_TOP),
				new Vec3d(TRAPEZOID_X_POS_TOP, Y_SIZE, TRAPEZOID_Y_POS_TOP),
				new Vec3d(TRAPEZOID_X_NEG_TOP, Y_SIZE, TRAPEZOID_Y_POS_TOP)
		};
		INGOT_POSITIONS_Z = new Vec3d[8];
		for (int i = 0; i < 8; i++) {
			INGOT_POSITIONS_Z[i] = new Vec3d(INGOT_POSITIONS_X[i].z, INGOT_POSITIONS_X[i].y, INGOT_POSITIONS_X[i].x);
		}

		INGOT_POSITIONS = new Vec3d[64][];
		for (int i = 0; i < 64; i++) {
			Vec3d[] base;
			int y = (i >> 2) & (~1);
			int x, z;
			if ((y & 2) == 2) {
				base = INGOT_POSITIONS_Z;
				z = ((i & 1) | ((i >> 1) & 2)) * 4;
				x = (i & 2) * 4;
			} else {
				base = INGOT_POSITIONS_X;
				x = ((i & 1) | ((i >> 1) & 2)) * 4;
				z = (i & 2) * 4;
			}
			INGOT_POSITIONS[i] = new Vec3d[8];
			for (int j = 0; j < 8; j++) {
				INGOT_POSITIONS[i][j] = base[j].addVector(x, y, z);
			}
		}

		QUAD_ORDERS = new int[][][] {
				{
						{0, 4, 5, 1},
						{1, 5, 6, 2},
						{2, 6, 7, 3},
						{3, 7, 4, 0},
						{0, 1, 2, 3},
						{4, 7, 6, 5}
				},
				{
						{0, 1, 5, 4},
						{1, 2, 6, 5},
						{2, 3, 7, 6},
						{3, 0, 4, 7},
						{0, 3, 2, 1},
						{4, 5, 6, 7}
				}
		};

		QUAD_UVS = new int[][] {
				{0, 16},
				{16, 16},
				{16, 0},
				{0, 0}
		};

		QUAD_FACES = new EnumFacing[][] {
				{
						EnumFacing.NORTH,
						EnumFacing.EAST,
						EnumFacing.SOUTH,
						EnumFacing.WEST,
						EnumFacing.DOWN,
						EnumFacing.UP
				},
				{
						EnumFacing.WEST,
						EnumFacing.SOUTH,
						EnumFacing.EAST,
						EnumFacing.NORTH,
						EnumFacing.DOWN,
						EnumFacing.UP
				}
		};
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side != null || !(state instanceof IExtendedBlockState)) {
			return Collections.emptyList();
		}

		TileEntityStacks stacks = ((IExtendedBlockState) state).getValue(BlockStacks.PROPERTY_TILE);
		if (stacks == null || stacks.stacks.isEmpty()) {
			return Collections.emptyList();
		}

		List<BakedQuad> list = new ArrayList<>();

		int i = 0;
		for (ItemMaterial material : stacks.stacks) {
			Vec3d[] vecs = INGOT_POSITIONS[i];

			ItemMaterial blockMaterial = material.getRelated("block");
			TextureAtlasSprite sprite;
			int c;

			if (blockMaterial == null) {
				ItemStack ingotStack = material.getStack();
				sprite = RenderUtils.getItemSprite(new ItemStack(Blocks.IRON_BLOCK));
				c = ColorLookupHandler.INSTANCE.getColor(ingotStack, RenderUtils.AveragingMode.FULL) | 0xFF000000;
			} else {
				sprite = RenderUtils.getItemSprite(blockMaterial.getStack());
				c = Minecraft.getMinecraft().getItemColors().colorMultiplier(blockMaterial.getStack(), 0);
			}

			float[] color = new float[] {
					MathHelper.clamp(((c >> 16) & 0xFF) / 255.0f, 0, 1),
					MathHelper.clamp(((c >> 8) & 0xFF) / 255.0f, 0, 1),
					MathHelper.clamp(((c) & 0xFF) / 255.0f, 0, 1),
					1.0f
			};

			int j = 0;
			int yOff = (i >> 3) & 1;

			for (int[] vecOrder : QUAD_ORDERS[yOff]) {
				UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
				EnumFacing face = QUAD_FACES[yOff][j];

				builder.setTexture(sprite);
				builder.setApplyDiffuseLighting(isAmbientOcclusion());
				builder.setContractUVs(false);
				builder.setQuadOrientation(face);

				int uv_offset = j < 4 ? 1 : 0;
				if (yOff == 1) uv_offset = 1 - uv_offset;

				for (int k = 0; k < vecOrder.length; k++) {
					Vec3d vec = vecs[vecOrder[k]];
					int[] uv = QUAD_UVS[(k + uv_offset) & 3];
					for (int e = 0; e < builder.getVertexFormat().getElementCount(); e++) {
						VertexFormatElement el = builder.getVertexFormat().getElement(e);
						switch (el.getUsage()) {
							case POSITION:
								builder.put(e, (float) vec.x / 16f, (float) vec.y / 16f, (float) vec.z / 16f, 1);
								break;
							case COLOR:
								builder.put(e, color);
								break;
							case NORMAL:
								builder.put(e, face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ(), 0);
								break;
							case UV:
								float u = sprite.getInterpolatedU(uv[0]);
								float v = sprite.getInterpolatedV(uv[1]);
								builder.put(e, u, v, 0, 1);
								break;
							default:
								builder.put(e);
						}
					}
				}

				list.add(builder.build());
				j++;
			}

			i++;
		}

		return list;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return ModelLoader.defaultTextureGetter().apply(TextureMap.LOCATION_MISSING_TEXTURE);
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IBlockState state, @Nullable EnumFacing facing) {
		TextureAtlasSprite sprite = null;

		TileEntityStacks stacks = ((IExtendedBlockState) state).getValue(BlockStacks.PROPERTY_TILE);
		if (stacks != null && !stacks.stacks.isEmpty()) {
			ItemMaterial material = stacks.stacks.get(stacks.stacks.size() - 1);
			sprite = RenderUtils.getItemSprite(material.getStack());
		}

		return sprite != null ? sprite : getParticleTexture();
	}
}
