package pl.asie.charset.wires.logic;

import java.util.HashSet;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IRedstoneUpdatable;

public class PostWorldTickWireUpdater {
    public static class Packet {
        public final PartWireBase caller;
        public final BlockPos pos;
        public final EnumFacing facing;

        public Packet(PartWireBase caller, BlockPos pos, EnumFacing facing) {
            this.caller = caller;
            this.pos = pos;
            this.facing = facing;
        }

        @Override
        public boolean equals(Object o2) {
            if (o2 == null || !(o2 instanceof Packet)) {
                return false;
            }

            Packet p2 = (Packet) o2;
            return p2.caller == caller && p2.pos.equals(pos) && p2.facing.equals(facing);
        }

        @Override
        public int hashCode() {
            return pos.hashCode() * 3 + facing.ordinal();
        }
    }
    
    public static final PostWorldTickWireUpdater instance = new PostWorldTickWireUpdater();
    private TIntObjectMap<Set<Packet>> propagationLocations = new TIntObjectHashMap<Set<Packet>>();

    protected PostWorldTickWireUpdater() {

    }

    public void clear() {
        propagationLocations.clear();
    }

    public void register(World world, Packet pos) {
        if (!world.isRemote) {
            Set<Packet> loc = propagationLocations.get(world.provider.getDimensionId());
            if (loc == null) {
                loc = new HashSet<Packet>();
                propagationLocations.put(world.provider.getDimensionId(), loc);
            }
            loc.add(pos);
        }
    }

    @SubscribeEvent
    public void postWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            Set<Packet> loc = propagationLocations.get(event.world.provider.getDimensionId());
            while (loc != null && loc.size() > 0) {
                Set<Packet> locNow = loc;
                loc = new HashSet<Packet>();
                propagationLocations.put(event.world.provider.getDimensionId(), loc);

                for (Packet p : locNow) {
                    TileEntity nt = event.world.getTileEntity(p.pos);
                    if (nt instanceof IBundledUpdatable) {
                        ((IBundledUpdatable) nt).onBundledInputChanged(p.facing);
                    } else if (nt instanceof IRedstoneUpdatable) {
                        ((IRedstoneUpdatable) nt).onRedstoneInputChanged(p.facing);
                    } else {
                        IMultipartContainer container = MultipartHelper.getPartContainer(event.world, p.pos);
                        if (container != null) {
                            for (IMultipart m : container.getParts()) {
                                if (m != null) {
                                    if (m instanceof IBundledUpdatable) {
                                        ((IBundledUpdatable) m).onBundledInputChanged(p.facing);
                                    } else if (m instanceof IRedstoneUpdatable) {
                                        ((IRedstoneUpdatable) m).onRedstoneInputChanged(p.facing);
                                    } else {
                                        m.onPartChanged(p.caller);
                                    }
                                }
                            }
                        } else {
                            event.world.notifyBlockOfStateChange(p.pos, MCMultiPartMod.multipart);
                        }
                    }
                }
            }
        }
    }
}
