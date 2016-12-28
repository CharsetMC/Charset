package pl.asie.charset.crafting;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.items.ItemBase;

import java.util.List;

public class ItemPocketTable extends ItemBase {
    
    public ItemPocketTable() {
        super();
        setUnlocalizedName("charset.pocket_crafting_table");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return activateTable(worldIn, playerIn, handIn);
    }
    
    ActionResult<ItemStack> activateTable(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            if (!world.isRemote) {
                player.openGui(ModCharsetCrafting.instance, 1, player.getEntityWorld(),
                        player.inventory.currentItem, 0, 0);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List infoList, boolean verbose) {
        if (player.getEntityWorld().isRemote) {
            /* String key = Core.proxy.getPocketCraftingTableKey();
            if (key != null && key != "") {
                final String prefix = "item.factorization:tool/pocket_crafting_table.";
                infoList.add(StatCollector.translateToLocalFormatted(prefix + "yesNEI", key));
            } */
        }
    }
}
