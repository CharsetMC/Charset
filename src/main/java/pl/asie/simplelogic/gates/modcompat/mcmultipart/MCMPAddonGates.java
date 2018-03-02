package pl.asie.simplelogic.gates.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.module.misc.scaffold.TileScaffold;
import pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart.MultipartScaffold;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

// @CharsetMCMPAddon("simplelogic.gates")
public class MCMPAddonGates extends MCMPAddonBase {
	private static final ResourceLocation LOC = new ResourceLocation("simplelogic:mcmp");

	public MCMPAddonGates() {
		super(SimpleLogicGates.blockGate, SimpleLogicGates.itemGate,
				MultipartGate::new, (tile) -> tile instanceof PartGate);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onAttachCaps(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof PartGate) {
			final PartGate gate = (PartGate) event.getObject();
			final IMultipartTile mpartTile = new IMultipartTile() {
				@Override
				public TileEntity getTileEntity() {
					return gate;
				}
			};

			event.addCapability(LOC, new ICapabilityProvider() {
				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
					return capability == MCMPCapabilities.MULTIPART_TILE;
				}

				@Nullable
				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
					return capability == MCMPCapabilities.MULTIPART_TILE ? MCMPCapabilities.MULTIPART_TILE.cast(mpartTile) : null;
				}
			});
		}
	}
}
