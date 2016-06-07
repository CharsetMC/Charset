package pl.asie.charset.lib.items;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.ModCharsetLib;

import java.util.List;

// @Optional.Interface(iface = "mods.railcraft.api.core.items.IMinecartItem", modid = "Railcraft")
public abstract class ItemMinecartCharset extends ItemMinecart {
    public ItemMinecartCharset() {
        super(EntityMinecart.Type.RIDEABLE); // yeah, right
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setHasSubtypes(true);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World w, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!BlockRailBase.isRailBlock(w.getBlockState(pos))) {
            return EnumActionResult.FAIL;
        } else {
            if (!w.isRemote) {
                placeCart(null, stack, w, pos);
            }

            stack.stackSize--;
            return EnumActionResult.SUCCESS;
        }
    }

    /* NORELEASE: Railcraft?
    @Override
    public boolean canBePlacedByNonPlayer(ItemStack cart) {
        return true;
    }
    */

    protected abstract EntityMinecart createCart(GameProfile owner, ItemStack cart, World world, double x, double y, double z);

    public EntityMinecart placeCart(GameProfile owner, ItemStack cart, World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        EntityMinecart minecart = createCart(owner, cart, world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
        cart.stackSize--;
        world.spawnEntityInWorld(minecart);
        return minecart;
    }
}
