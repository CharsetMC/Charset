package pl.asie.charset.pipes;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import pl.asie.charset.pipes.client.RendererPipeTile;
import pl.asie.charset.pipes.client.RendererShifterTile;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerItemModels() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModCharsetPipes.shifterBlock), 0,
				new ModelResourceLocation("charsetpipes:shifter", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModCharsetPipes.pipeBlock), 0,
				new ModelResourceLocation("charsetpipes:pipe", "inventory"));
	}

	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TilePipe.class, new RendererPipeTile());
		ClientRegistry.bindTileEntitySpecialRenderer(TileShifter.class, new RendererShifterTile());
	}
}
