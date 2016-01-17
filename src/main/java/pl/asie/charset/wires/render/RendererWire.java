package pl.asie.charset.wires.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.property.IExtendedBlockState;

import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.ItemWire;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.wires.logic.PartWireBase;

public class RendererWire extends RendererWireBase {
	private final List<RendererWireBase> renderers = new ArrayList<RendererWireBase>();

	public RendererWire() {
		renderers.add(new RendererWireNormal("wire", 2, 2));
		renderers.add(new RendererWireInsulated("insulated_wire", 4, 3));
		renderers.add(new RendererWireBundled("bundled_wire", 6, 4));
	}

	@Override
	public IBakedModel handlePartState(IBlockState state) {
		super.handlePartState(state);
		return this;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		super.handleItemState(stack);
		return this;
	}

	private int getRendererId() {
		if (state != null) {
			PartWireBase wire = null;
			if (state instanceof IExtendedBlockState) {
				wire = ((IExtendedBlockState) state).getValue(PartWireBase.PROPERTY);
			}

			if (wire != null) {
				return wire.type.type().ordinal();
			}
		} else if (stack != null) {
			return WireKind.VALUES[stack.getItemDamage() >> 1].type().ordinal();
		}

		return 0;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		PartWireBase wire = null;
		if (state instanceof IExtendedBlockState) {
			wire = ((IExtendedBlockState) state).getValue(PartWireBase.PROPERTY);
		}

		if (wire != null) {
			renderers.get(getRendererId()).handlePartState(state);
			renderers.get(getRendererId()).addWire(wire, wire.location, wire.getRedstoneLevel() > 0, quads);
		} else if (stack != null) {
			if (ItemWire.isFreestanding(stack)) {
				renderers.get(getRendererId()).handleItemState(stack);
				renderers.get(getRendererId()).addWireFreestanding(null, false, quads);
			} else {
				renderers.get(getRendererId()).handleItemState(stack);
				renderers.get(getRendererId()).addWire(null, WireFace.DOWN, false, quads);
			}
		}

		return quads;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing face) {
		return Collections.emptyList();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return renderers.get(getRendererId()).getParticleTexture();
	}

	@Override
	public void loadTextures(TextureMap map) {
		for (RendererWireBase renderer : renderers) {
			renderer.loadTextures(map);
		}
	}
}
