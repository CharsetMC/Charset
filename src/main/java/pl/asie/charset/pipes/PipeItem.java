package pl.asie.charset.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.lib.ItemUtils;
import pl.asie.charset.lib.inventory.InventoryUtils;
import pl.asie.charset.pipes.api.IShifter;

public class PipeItem {
	public static final int MAX_PROGRESS = 128;
	public static final int CENTER_PROGRESS = MAX_PROGRESS / 2;
	public static final int SPEED = MAX_PROGRESS / 16;
	private static short nextId;

	public final short id;
	private int activeShifterDistance;
	private TilePipe owner;
	private boolean stuck;

	protected ForgeDirection input, output = ForgeDirection.UNKNOWN;
	protected boolean reachedCenter;
	protected ItemStack stack;
	protected int progress;
	protected int blocksSinceSync;

	public PipeItem(TilePipe owner, ItemStack stack, ForgeDirection side) {
		this.id = nextId++;
		this.owner = owner;
		this.stack = stack;
		initializeFromEntrySide(side);
	}

	public PipeItem(TilePipe owner, NBTTagCompound nbt) {
		this.id = nextId++;
		this.owner = owner;
		readFromNBT(nbt);
	}

	protected PipeItem(TilePipe tile, short id) {
		this.owner = tile;
		this.id = id;
	}

	public boolean isStuck() {
		return stuck;
	}

	public boolean isValid() {
		return stack != null && stack.getItem() != null && input != null;
	}

	private float getTranslatedCoord(int offset) {
		if (progress >= CENTER_PROGRESS) {
			return 0.5F + (float) offset * (progress - CENTER_PROGRESS) / MAX_PROGRESS;
		} else {
			switch (offset) {
				case -1:
					return 1.0F + (float) offset * progress / MAX_PROGRESS;
				case 0:
				default:
					return 0.5F;
				case 1:
					return (float) offset * progress / MAX_PROGRESS;
			}
		}
	}

	public float getX() {
		return getTranslatedCoord(getDirection().offsetX);
	}

	public float getY() {
		return getTranslatedCoord(getDirection().offsetY);
	}

	public float getZ() {
		return getTranslatedCoord(getDirection().offsetZ);
	}

	public ItemStack getStack() {
		return stack;
	}

	public ForgeDirection getDirection() {
		return reachedCenter ? output : input.getOpposite();
	}

	private boolean isCentered() {
		return progress == CENTER_PROGRESS;
	}

	// This version takes priority into account (filtered shifters are
	// prioritized over unfiltered shifters at the same distance).
	private int getInternalShifterStrength(IShifter shifter, ForgeDirection dir) {
		if (shifter == null) {
			return 0;
		} else {
			return owner.getShifterStrength(dir) * 2 + (shifter.hasFilter() ? 0 : 1);
		}
	}

	private void updateStuckFlag() {
		boolean foundShifter = false;
		int minimumShifterDistance = Integer.MAX_VALUE;

		// First, find the closest shifter affecting the item.
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			IShifter p = owner.getNearestShifter(dir);
			int ps = getInternalShifterStrength(p, dir);
			if (ps > 0 && ps < minimumShifterDistance
					&& isShifterPushing(p, output)) {
				minimumShifterDistance = ps;
				foundShifter = true;
			}
		}

		if (!isValidDirection(output)) {
			if (progress <= CENTER_PROGRESS) {
				calculateOutputDirection();
			} else {
				output = ForgeDirection.UNKNOWN;
			}
		} else if (isCentered() && ( // This tries to detect change in shifter "air stream".
					(!foundShifter && activeShifterDistance > 0)
				||	(foundShifter && activeShifterDistance != minimumShifterDistance)
				||	(foundShifter && activeShifterDistance != getInternalShifterStrength(owner.getNearestShifter(output), output))
			)) {

			TileEntity shifterTile = owner.getWorldObj().getTileEntity(
					owner.xCoord - output.offsetX * activeShifterDistance,
					owner.yCoord - output.offsetY * activeShifterDistance,
					owner.zCoord - output.offsetZ * activeShifterDistance
			);

			if (!(shifterTile instanceof IShifter) || !isShifterPushing((IShifter) shifterTile, output)) {
				calculateOutputDirection();
			}
		}

		if (output == ForgeDirection.UNKNOWN) {
			// Never stuck when UNKNOWN, because the item will drop anyway.
			stuck = false;
		} else {
			if (isCentered() && activeShifterDistance > 0
					&& owner.getShifterStrength(output.getOpposite()) == owner.getShifterStrength(output)
					&& isShifterPushing(owner.getNearestShifter(output.getOpposite()), output.getOpposite())) {
				// Handle the "equal-distance opposite shifters" scenario for stopping items.
				// This does not take filtering into account!
				stuck = true;
			} else {
				stuck = !canMoveDirection(output, false);
			}
		}
	}

	public boolean move() {
		if (!reachedCenter) {
			boolean atCenter = (progress + SPEED) >= CENTER_PROGRESS;

			if (atCenter) {
				onReachedCenter();
			} else {
				progress += SPEED;
			}
		} else {
			if (owner.getWorldObj().isRemote) {
				if (!stuck) {
					progress += SPEED;
				}

				if (progress >= MAX_PROGRESS) {
					onItemEnd();
					return false;
				}
			} else {
				ForgeDirection oldOutput = output;
				boolean oldStuck = stuck;

				updateStuckFlag();

				if (!stuck) {
					progress += SPEED;
				}

				if (progress >= MAX_PROGRESS) {
					onItemEnd();
					return false;
				}

				if (oldStuck != stuck || oldOutput != output) {
					ModCharsetPipes.packet.sendToAllAround(new PacketItemUpdate(owner, this, false), owner, ModCharsetPipes.PIPE_TESR_DISTANCE);
				}
			}
		}

		return true;
	}

	private void onItemEnd() {
		TileEntity tile = owner.getNeighbourTile(output);
		boolean foundInventory = false;

		if (owner.getWorldObj().isRemote) {
			// Last resort security mechanism for stray packets.
			blocksSinceSync++;

			if (blocksSinceSync < 2) {
				passToPipe(tile, output, false);
			}
			return;
		}

		if (output != ForgeDirection.UNKNOWN) {
			if (passToPipe(tile, output, false)) {
				foundInventory = true;
			} else {
				if (addToInventory(tile, output, false)) {
					foundInventory = true;
				}
			}
		}

		if (!foundInventory && stack != null && stack.stackSize > 0) {
			dropItem(true);
		}
	}

	private boolean isValidDirection(ForgeDirection dir) {
		if (dir == ForgeDirection.UNKNOWN || dir == null || !owner.connects(dir)) {
			return false;
		}

		TileEntity tile = owner.getNeighbourTile(dir);

		if (tile instanceof TilePipe) {
			return ((TilePipe) tile).connects(dir.getOpposite());
		} else if (tile instanceof IInventory) {
			return InventoryUtils.connects((IInventory) tile, dir.getOpposite());
		}

		return false;
	}

	private boolean canMoveDirection(ForgeDirection dir, boolean isPickingDirection) {
		if (dir == ForgeDirection.UNKNOWN) {
			return activeShifterDistance == 0;
		}

		if (dir == null) {
			return false;
		}

		TileEntity tile = owner.getNeighbourTile(dir);

		if (isPickingDirection) {
			// If we're picking the direction, only check for pipe *connection*,
			// so that clogging mechanics (pipes which can't take in items) work
			// as intended.
			if (tile instanceof TilePipe) {
				if (((TilePipe) tile).connects(dir.getOpposite())) {
					return true;
				}
			}
		} else {
			if (passToPipe(tile, dir, true)) {
				return true;
			}
		}

		if (addToInventory(tile, dir, true)) {
			return true;
		}

		return false;
	}

	private boolean isShifterPushing(IShifter p, ForgeDirection direction) {
		return p != null
				&& p.getDirection() == direction
				&& p.isShifting()
				&& p.matches(stack);
	}

	private void calculateOutputDirection() {
		List<ForgeDirection> directionList = new ArrayList<ForgeDirection>();
		TObjectIntMap<ForgeDirection> directionStrength = new TObjectIntHashMap<ForgeDirection>();

		activeShifterDistance = 0;

		// Step 1: Make a list of all valid directions, as well as all shifters.
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (isValidDirection(direction)) {
				directionList.add(direction);
			}

			IShifter p = owner.getNearestShifter(direction);

			if (isShifterPushing(p, direction)) {
				directionStrength.put(direction, getInternalShifterStrength(p, direction));
			}
		}

		// Step 2: Find the strongest shifter considered valid.
		// A valid shifter is either one which pushes into a valid direction
		// or one which has a shifter at equal strength pushing in the same
		// axis (which lets you stop items).
 		ForgeDirection pressureDir = ForgeDirection.UNKNOWN;
		int pressurePower = Integer.MAX_VALUE;

		for (ForgeDirection direction : directionStrength.keySet()) {
			int strength = directionStrength.get(direction);
			if (strength < pressurePower) {
				// We shift by one to have no distinction between filtered and unfiltered shifters
				// for the "equal strength, same axis" scenario.
				if (directionList.contains(direction) || (directionStrength.get(direction.getOpposite()) >> 1) == (strength >> 1)) {
					pressureDir = direction;
					pressurePower = strength;
				}
			}
		}

		// Step 3: Pick the next path.
		if (pressureDir != ForgeDirection.UNKNOWN) {
			// Step 3a: If there is a valid shifter, that becomes the
			// "forced" direction.
			activeShifterDistance = pressurePower;
			this.output = pressureDir;
			return;
		} else {
			ForgeDirection dir;
			directionList.remove(input);
			int i = 0;

			// Step 3b: Pick the first "unforced" direction to scan.
			// A direction opposite to input (aka. "straight line")
			// takes priority.
			if (directionList.contains(input.getOpposite())) {
				dir = input.getOpposite();
				directionList.remove(input.getOpposite());
			} else if (directionList.size() > 0) {
				Collections.shuffle(directionList);
				dir = directionList.get(0);
				i = 1;
			} else {
				this.output = ForgeDirection.UNKNOWN;
				return;
			}

			ForgeDirection firstOutput = dir;

			while (!canMoveDirection(dir, true) && i < directionList.size()) {
				dir = directionList.get(i);
				i++;
			}

			// Step 3c: If a valid, free direction has been found, use that.
			// Otherwise, set it to the first output direction selected to
			// prioritize that, for some reason.
			if (canMoveDirection(dir, true)) {
				this.output = dir;
			} else {
				this.output = firstOutput;
			}
		}
	}

	private void onReachedCenter() {
		progress = CENTER_PROGRESS;
		this.reachedCenter = true;

		if (owner.getWorldObj().isRemote) {
			return;
		}

		calculateOutputDirection();
		updateStuckFlag();
		ModCharsetPipes.packet.sendToAllAround(new PacketItemUpdate(owner, this, false), owner, ModCharsetPipes.PIPE_TESR_DISTANCE);
	}

	protected void reset(TilePipe owner, ForgeDirection input) {
		this.owner = owner;
		initializeFromEntrySide(input);

		// Do an early calculation to aid the server side.
		// Won't always be right, might be sometimes right.
		calculateOutputDirection();
	}

	private boolean passToPipe(TileEntity tile, ForgeDirection dir, boolean simulate) {
		if (tile instanceof TilePipe) {
			if (((TilePipe) tile).injectItemInternal(this, dir.getOpposite(), simulate)) {
				return true;
			}
		}

		return false;
	}

	private boolean addToInventory(TileEntity tile, ForgeDirection dir, boolean simulate) {
		if (tile instanceof IInventory) {
			int added = InventoryUtils.addStack((IInventory) tile, dir.getOpposite(), stack, simulate);
			if (added > 0) {
				if (!simulate) {
					stack.stackSize -= added;
				}
				return true;
			}
		}

		return false;
	}

	protected void dropItem(boolean useOutputDirection) {
		ForgeDirection dir = ForgeDirection.UNKNOWN;

		if (useOutputDirection) {
			// Decide output direction
			int directions = 0;
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				if (owner.connects(d)) {
					directions++;
					dir = d.getOpposite();
					if (directions >= 2) {
						break;
					}
				}
			}

			if (directions >= 2) {
				dir = ForgeDirection.UNKNOWN;
			}
		}

		ItemUtils.spawnItemEntity(owner.getWorldObj(),
				(double) owner.xCoord + 0.5 + dir.offsetX * 0.75,
				(double) owner.yCoord + 0.5 + dir.offsetY * 0.75,
				(double) owner.zCoord + 0.5 + dir.offsetZ * 0.75,
				stack, 0, 0, 0);

		stack = null;
	}

	private void initializeFromEntrySide(ForgeDirection side) {
		this.input = side;
		this.output = ForgeDirection.UNKNOWN;
		this.reachedCenter = false;
		this.stuck = false;
		this.progress = 0;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		stack = ItemStack.loadItemStackFromNBT(nbt);
		progress = nbt.getShort("p");
		input = ForgeDirection.getOrientation(nbt.getByte("in"));
		output = ForgeDirection.getOrientation(nbt.getByte("out"));
		reachedCenter = nbt.getBoolean("reachedCenter");
		if (nbt.hasKey("stuck")) {
			stuck = nbt.getBoolean("stuck");
		}
		if (nbt.hasKey("activePD")) {
			activeShifterDistance = nbt.getInteger("activePD");
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		stack.writeToNBT(nbt);
		nbt.setShort("p", (short) progress);
		nbt.setByte("in", (byte) input.ordinal());
		nbt.setByte("out", (byte) output.ordinal());
		nbt.setBoolean("reachedCenter", reachedCenter);
		if (stuck) {
			nbt.setBoolean("stuck", stuck);
		}
		if (activeShifterDistance > 0) {
			nbt.setInteger("activePD", activeShifterDistance);
		}
	}

	public boolean hasReachedCenter() {
		return reachedCenter;
	}

	public void setStuckFlagClient(boolean stuck) {
		if (owner.getWorldObj().isRemote) {
			this.stuck = stuck;
		}
	}

	public TilePipe getOwner() {
		return owner;
	}
}
