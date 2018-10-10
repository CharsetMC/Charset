package pl.asie.charset.module.audio.microphone;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;

public class BlockWirelessReceiver extends BlockBase implements ITileEntityProvider {
	public BlockWirelessReceiver() {
		super(Material.CIRCUITS);
		setTranslationKey("charset.audio_wireless_receiver");
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileWirelessReceiver();
	}
}
