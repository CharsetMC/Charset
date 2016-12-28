package pl.asie.charset.crafting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.container.ContainerBase;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

public class ContainerPocket extends ContainerBase {
    private final EntityPlayer player;
    private final InventoryPlayer playerInv;
    private InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    private IInventory craftResult = new InventoryCraftResult();

    private ArrayList<Slot> nonCraftingInventorySlots = new ArrayList<Slot>();
    private ArrayList<Slot> craftingSlots = new ArrayList<Slot>();
    private ArrayList<Slot> mainInvSlots = new ArrayList<Slot>();
    private ArrayList<Slot> hotbarSlots = new ArrayList<Slot>();
    private ArrayList<Slot> mainInvThenHotbarSlots = new ArrayList<Slot>();
    private RedirectedSlotCrafting craftResultSlot;

    private boolean isCrafting = false;
    private boolean dirty = false;

    public ContainerPocket(EntityPlayer player) {
        super(player.inventory);
        this.player = player;
        this.playerInv = player.inventory;
        craftResultSlot = (RedirectedSlotCrafting) addSlotToContainer(new RedirectedSlotCrafting(player, craftMatrix, craftResult, 208, 28));
        bindPlayerInventory(player.inventory, 8, 8);
        detectAndSendChanges();
        updateCraft();
    }

    @Override
    protected Slot addPlayerSlotToContainer(Slot slot) {
        if (slot.getSlotIndex() >= 9 && (slot.getSlotIndex() % 9) >= 6) {
            slot = new Slot(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    updateCraft();
                }
            };
            craftingSlots.add(slot);
        } else {
            nonCraftingInventorySlots.add(slot);
            if (slot.getSlotIndex() < 9) {
                hotbarSlots.add(slot);
            } else {
                mainInvSlots.add(slot);
            }

            if (slot.getSlotIndex() < 9 && slot.getHasStack() && slot.getStack().getItem() == ModCharsetCrafting.pocketTable) {
                slot = new Slot(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos) {
                    @Override
                    public boolean canTakeStack(EntityPlayer playerIn) {
                        return false;
                    }
                };
            }
        }

        return super.addPlayerSlotToContainer(slot);
    }

    class RedirectedSlotCrafting extends SlotCrafting {
        public RedirectedSlotCrafting(EntityPlayer player, InventoryCrafting craftMatrix, IInventory craftResult, int posX, int posY) {
            super(player, craftMatrix, craftResult, 0, posX, posY);
        }

        @Override
        public void onCrafting(ItemStack stack) {
            isCrafting = true;
            ItemStack faker = ItemStack.EMPTY;
            for (Slot slot : craftingSlots) {
                playerInv.setInventorySlotContents(slot.getSlotIndex(), faker);
            }
            super.onCrafting(stack);
            int i = 0;
            for (Slot slot : craftingSlots) {
                ItemStack repl = craftMatrix.getStackInSlot(i++);
                playerInv.setInventorySlotContents(slot.getSlotIndex(), repl);
            }
            isCrafting = false;
            updateCraft();
        }
    }
    
    boolean isWorking = false;
    
    void updateMatrix() {
        isWorking = true;
        int i = 0;
        for (Slot slot : craftingSlots) {
            craftMatrix.setInventorySlotContents(i++, slot.getStack());
        }
        isWorking = false;
    }

    public void updateCraft() {
        if (isWorking) {
            dirty = true;
            return;
        } 
        updateMatrix();
        ItemStack result = ItemStack.EMPTY;
        IRecipe match = RecipeUtils.findMatchingRecipe(craftMatrix, player.getEntityWorld());
        if (match != null) {
            result = match.getCraftingResult(craftMatrix);
        }
        craftResult.setInventorySlotContents(0, result);
        dirty = false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setAll(List<ItemStack> p_190896_1_) {
        isWorking = true;
        super.setAll(p_190896_1_);
        isWorking = false;
    }
    
    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        super.onCraftMatrixChanged(inv);
    }
    
    @Override
    public void detectAndSendChanges() {
        if (!isCrafting) {
            super.detectAndSendChanges();
        }
    }

    public void onAction(int action, int arg) {
        switch (action) {
            case PacketPTAction.BALANCE:
                craftBalance();
                break;
            case PacketPTAction.FILL:
                craftFill(arg);
                break;
            case PacketPTAction.CLEAR:
                craftClear();
                break;
            case PacketPTAction.SWIRL:
                craftSwirl();
                break;
            default:
                return;
        }
        updateCraft();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        Slot slot = inventorySlots.get(slotId);

        if (slot == craftResultSlot) {
            updateCraft();
            ItemStack res = craftResultSlot.getStack();
            if (res.isEmpty()) {
                return slot.getStack();
            } else {
                res = res.copy();
            }
            ItemStack held = ItemStack.EMPTY;
            for (int count = getCraftCount(res); count > 0; count--) {
                ItemStack craftedStack = craftResultSlot.getStack().copy();
                held = tryTransferStackInSlot(player, craftResultSlot, nonCraftingInventorySlots);
                craftResultSlot.onTake(player, craftedStack);
                if (!held.isEmpty()) {
                    break;
                }
                updateCraft();
                ItemStack newRes = craftResultSlot.getStack();
                if (newRes.isEmpty() || !ItemUtils.canMerge(res, newRes) || getCraftCount(newRes) <= 0) {
                    break;
                }
            }
            detectAndSendChanges();
        } else if (nonCraftingInventorySlots.contains(slot)) {
            tryTransferStackInSlot(player, slot, craftingSlots);
        } else if (craftingSlots.contains(slot)) {
            tryTransferStackInSlot(player, slot, nonCraftingInventorySlots);
        } else {
            slot.getStack();
        }

        return ItemStack.EMPTY;
    }
    
    int getCraftCount(ItemStack res) {
        boolean hasEmpty = false;
        int space_to_fill = 0;
        for (Slot slot : nonCraftingInventorySlots) {
            ItemStack is = slot.getStack();
            if (is.isEmpty()) {
                hasEmpty = true;
                continue;
            }
            if (ItemUtils.canMerge(res, is)) {
                space_to_fill += is.getMaxStackSize() - is.getCount();
            }
        }
        if (space_to_fill > 64) {
            space_to_fill = 64;
        }
        int ret = space_to_fill / res.getCount();
        if (ret == 0 && hasEmpty) {
            return 64 / res.getCount();
        }
        return ret;
    }

    void craftClear() {
        for (Slot slot : craftingSlots) {
            tryTransferStackInSlot(player, slot, mainInvSlots);
            if (slot.getHasStack()) {
                tryTransferStackInSlot(player, slot, hotbarSlots);
            }
        }
        updateMatrix();
    }
    
    //InventoryPlayer Slots:
    //09 10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //00 01 02 03 04 05 06 07 08
    private static final int slots[] = {
        15, 16, 17,
        26,
        35, 34, 33,
        24,
    };
    private static final int slotsTwice[] = {
        15, 16, 17, 26, 35, 34, 33, 24,
        15, 16, 17, 26, 35, 34, 33, 24,
    };

    void craftSwirl() {
        boolean anyAction = false;
        for (int n = 0; n < 8; n++) {
            //1. find a stack with > 1 item in it
            //2. find an empty slot
            //3. move 1 item from former into latter
            boolean any = false;
            for (int slotIndexIndex = 0; slotIndexIndex < slots.length; slotIndexIndex++) {
                ItemStack is = playerInv.getStackInSlot(slots[slotIndexIndex]);
                if (is.isEmpty() || is.getCount() <= 1) {
                    continue;
                }
                for (int probidex = slotIndexIndex; probidex < slotsTwice.length && probidex < slotIndexIndex + slots.length; probidex++) {
                    ItemStack empty = playerInv.getStackInSlot(slotsTwice[probidex]);
                    if (empty != null) {
                        continue;
                    }
                    playerInv.setInventorySlotContents(slotsTwice[probidex], is.splitStack(1));
                    any = true;
                    break;
                }
            }
            if (!any) {
                break;
            } else {
                anyAction = true;
            }
        }
        if (!anyAction) {
            //Did nothing. Shift the items around.
            ItemStack swapeh = playerInv.getStackInSlot(slots[slots.length - 1]);
            for (int i = 0; i < slots.length; i++) {
                ItemStack here = playerInv.getStackInSlot(slotsTwice[i]);
                playerInv.setInventorySlotContents(slotsTwice[i], swapeh);
                swapeh = here;
            }
            playerInv.setInventorySlotContents(slots[0], swapeh);
        }
        updateMatrix();
    }
    
    void craftBalance() {
        class Accumulator {
            ItemStack toMatch;
            int stackCount = 0;
            ArrayList<Integer> matchingSlots = new ArrayList<Integer>(9);

            public Accumulator(ItemStack toMatch, int slot) {
                this.toMatch = toMatch.copy();
                stackCount = toMatch.getCount();
                this.toMatch.setCount(1);
                toMatch.setCount(0);
                matchingSlots.add(slot);
            }

            boolean add(ItemStack ta, int slot) {
                if (ItemUtils.canMerge(toMatch, ta)) {
                    stackCount += ta.getCount();
                    ta.setCount(0);
                    matchingSlots.add(slot);
                    return true;
                }
                return false;
            }
        }
        ArrayList<Accumulator> list = new ArrayList<Accumulator>(9);
        for (Slot s : craftingSlots) {
            int slot = s.getSlotIndex();
            ItemStack here = playerInv.getStackInSlot(slot);
            if (here.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (Accumulator acc : list) {
                if (acc.add(here, slot)) {
                    found = true;
                }
            }
            if (!found) {
                list.add(new Accumulator(here, slot));
            }
        }

        for (Accumulator acc : list) {
            int delta = acc.stackCount / acc.matchingSlots.size();
            // this should be incapable of being 0
            delta = Math.min(delta, 1); // ...we'll make sure anyways.
            for (int slot : acc.matchingSlots) {
                if (acc.stackCount <= 0) {
                    break;
                }
                playerInv.getStackInSlot(slot).setCount(delta);
                acc.stackCount -= delta;
            }
            // we now may have a few left over, which we'll distribute
            while (acc.stackCount > 0) {
                for (int slot : acc.matchingSlots) {
                    if (acc.stackCount <= 0) {
                        break;
                    }
                    playerInv.getStackInSlot(slot).grow(1);
                    acc.stackCount--;
                }
            }
        }

        updateMatrix();
    }

    void craftFill(int slot) {
        final ItemStack toMove = playerInv.getStackInSlot(slot);
        for (Slot matrixSlot : craftingSlots) {
            if (toMove.isEmpty()) {
                break;
            }
            if (matrixSlot.getStack().isEmpty()) {
                matrixSlot.putStack(toMove.splitStack(1));
            }
        }
        playerInv.setInventorySlotContents(slot, toMove.isEmpty() ? ItemStack.EMPTY : toMove);
        updateMatrix();
    }
    
   /* @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        // In a pocket crafting table, use 2 stacks of iron & 1 stick to make a sword.
        // Put an iron sword in your 3rd hotbar slot.
        // Fill the rest of your inventory up with cobble.
        // Put your mouse over the crafting result, and press 3 to lose your sword.
        // Since it gets put in the crafting area and then gets wiped out.
        
        // So this is code to dance around that issue. There may be similar
        boolean bad_news = false;
        if (mode == 2 && clickedButton >= 0 && clickedButton < 9) {
            Slot slot2 = (Slot)this.inventorySlots.get(slotId);
            if (slot2.canTakeStack(player)) {
                bad_news = true;
            }
        }
        if (!bad_news) {
            ItemStack ret = super.slotClick(slotId, clickedButton, mode, player);
            if (dirty) {
                updateCraft();
            }
            return ret;
        }
        final InventoryPlayer realInventory = player.inventory;
        
        try {
            player.inventory = new InventoryPlayer(player) {
                {
                    for (Field field : InventoryPlayer.class.getFields()) {
                        field.set(this, field.get(realInventory));
                    }
                }
                
                @Override
                public int getFirstEmptyStack() {
                    foundCraftingSlot: for (int i = 0; i < mainInventory.length; ++i) {
                        if (mainInventory[i] != null) continue;
                        for (Slot slot : craftingSlots) {
                            if (i == slot.getSlotIndex()) {
                                continue foundCraftingSlot;
                            }
                        }
                        return i;
                    }
                    return -1;
                }
            };
            return super.slotClick(slotId, clickedButton, mode, player);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } finally {
            player.inventory = realInventory;
            if (dirty) {
                updateCraft();
            }
        }
        // (Could we just verify the crafting recipe instead of this nosense? Might not actually be possible. But if it is, it'd be less terrible.)
        
    } */
}
