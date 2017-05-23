package pl.asie.charset.module.transport.carts;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.transport.carts.link.Linkable;
import pl.asie.charset.module.transport.carts.link.TrainLinker;
import pl.asie.charset.module.transport.rails.BlockRailCharset;
import pl.asie.charset.module.transport.rails.CharsetTransportRails;

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
        name = "transport.carts",
        description = "Minecart rework. WIP"
)
public class CharsetTransportCarts {
    public static final Map<Class<? extends Entity>, Class<? extends EntityMinecart>> REPLACEMENT_MAP = new HashMap<>();
    public static final ResourceLocation LINKABLE_LOC = new ResourceLocation("charsetcarts:linkable");
    @CapabilityInject(Linkable.class)
    public static Capability<Linkable> LINKABLE;

    @CharsetModule.Instance
    public static CharsetTransportCarts instance;

    @CharsetModule.Configuration
    public static Configuration config;

    public static TrackCombiner combiner;
    public static TrainLinker linker;
    public static int minecartStackSize;

    public static Item itemLinker;

    private void register(Class<? extends EntityMinecart> minecart, String name) {
        RegistryUtils.register(minecart, name, 64, 1, true);
    }

    private void register(Class<? extends EntityMinecart> minecart, String name, Class<? extends Entity> from) {
        register(minecart, name);
        REPLACEMENT_MAP.put(from, minecart);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        minecartStackSize = config.getInt("minecartStackSize", "tweaks", 4, 1, 64, "Sets the minimum stack size for all minecarts.");
        CapabilityManager.INSTANCE.register(Linkable.class, Linkable.STORAGE, Linkable.class);

        if (ModCharset.INDEV) {
            linker = new TrainLinker();
            MinecraftForge.EVENT_BUS.register(linker);

            itemLinker = new Item().setCreativeTab(ModCharset.CREATIVE_TAB).setUnlocalizedName("linker").setMaxStackSize(1);
            RegistryUtils.register(itemLinker, "linker");
        }

        combiner = new TrackCombiner();
    }

    private void registerCombinerRecipeForDirs(Block railSrc, IProperty<BlockRailBase.EnumRailDirection> propSrc, Block railDst, IProperty<BlockRailBase.EnumRailDirection> propDst, ItemStack with) {
        for (BlockRailBase.EnumRailDirection direction : propSrc.getAllowedValues()) {
            if (propDst.getAllowedValues().contains(direction)) {
                combiner.register(railSrc.getDefaultState().withProperty(propSrc, direction),
                        railDst.getDefaultState().withProperty(propDst, direction),
                        with);
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (ModCharset.INDEV) {
            register(EntityMinecartImproved.class, "rminecart", EntityMinecart.class);
        }

        MinecraftForge.EVENT_BUS.register(this);

        if (combiner != null) {
            MinecraftForge.EVENT_BUS.register(combiner);

            // TODO: This needs a redesign... Possibly move the Combiner to Lib.
            if (ModCharset.isModuleLoaded("transport.rails")) {
                combiner.register(Blocks.RAIL, CharsetTransportRails.blockRailCross.getDefaultState(), new ItemStack(Blocks.RAIL));
                combiner.register(Blocks.RAIL, CharsetTransportRails.blockRailCross.getDefaultState().withProperty(BlockRailCharset.DIRECTION, BlockRailBase.EnumRailDirection.EAST_WEST), new ItemStack(Blocks.RAIL));
            }
            registerCombinerRecipeForDirs(Blocks.RAIL, BlockRail.SHAPE, Blocks.DETECTOR_RAIL, BlockRailDetector.SHAPE, new ItemStack(Blocks.STONE_PRESSURE_PLATE));
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (Item item : Item.REGISTRY) {
            if (item instanceof ItemMinecart && item.getItemStackLimit() < minecartStackSize) {
                item.setMaxStackSize(minecartStackSize);
            }
        }
    }

    private final Map<EntityPlayer, EntityMinecart> linkMap = new HashMap<>();

    @SubscribeEvent
    public void onNothingInteract(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getEntityPlayer().getEntityWorld().isRemote
                && event.getItemStack().getItem() == itemLinker) {
            if (linkMap.containsKey(event.getEntityPlayer())) {
                linkMap.remove(event.getEntityPlayer());
                event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlinked"));
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMinecart
                && !event.getTarget().getEntityWorld().isRemote
                && event.getItemStack().getItem() == itemLinker) {
            EntityMinecart cart = (EntityMinecart) event.getTarget();

            if (linkMap.containsKey(event.getEntityPlayer())) {
                EntityMinecart cartOther = linkMap.remove(event.getEntityPlayer());
                Linkable link = linker.get(cart);
                Linkable linkOther = linker.get(cartOther);
                if (event.getEntityPlayer().isSneaking()) {
                    if (linker.unlink(link, linkOther)) {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlinked2"));
                    } else {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlink2_failed"));
                    }
                } else {
                    if (link.next == null && linkOther.previous == null) {
                        linker.link(link, linkOther);
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked2"));
                    } else if (link.previous == null && linkOther.next == null) {
                        linker.link(linkOther, link);
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked2"));
                    } else {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_link2_failed"));
                    }
                }
            } else {
                linkMap.put(event.getEntityPlayer(), cart);
                event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked1"));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        Class<? extends Entity> classy = event.getEntity().getClass();
        if (REPLACEMENT_MAP.containsKey(classy)) {
            try {
                event.setCanceled(true);
                EntityMinecart painting = REPLACEMENT_MAP.get(classy).getConstructor(World.class, double.class, double.class, double.class).newInstance(
                        event.getWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ
                );
                event.getWorld().spawnEntity(painting);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
