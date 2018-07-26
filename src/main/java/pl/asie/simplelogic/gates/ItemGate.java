/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.simplelogic.gates;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.charset.lib.item.ItemBlockBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemGate extends ItemBlockBase {
	public ItemGate(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	public static ItemStack getStack(PartGate gate) {
		return getStack(gate, false);
	}

	public static ItemStack getStack(PartGate gate, boolean silky) {
		if (!SimpleLogicGates.logicClasses.containsValue(gate.logic.getClass())) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = new ItemStack(SimpleLogicGates.itemGate);
		stack.setTagCompound(new NBTTagCompound());
		gate.writeItemNBT(stack.getTagCompound(), silky);
		return stack;
	}

	public static Optional<PartGate> getPartGate(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("logic", Constants.NBT.TAG_STRING)) {
			Optional<PartGate> gate = getPartGate(new ResourceLocation(stack.getTagCompound().getString("logic")));
			gate.ifPresent((a) -> a.loadFromStack(stack));
			return gate;
		} else {
			return Optional.empty();
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		Optional<PartGate> gate = getPartGate(stack);
		if (!gate.isPresent()) {
			return "[UNKNOWN GATE]";
		}
		ResourceLocation loc = SimpleLogicGates.logicClasses.inverse().get(gate.get().logic.getClass());
		if (loc == null) {
			return "[UNKNOWN GATE]";
		}
		String name = SimpleLogicGates.logicUns.get(loc);
		if (name == null) {
			return "[UNKNOWN GATE]";
		}

		if (gate.get().logic.isSideInverted(EnumFacing.NORTH) && I18n.canTranslate(name + ".i.name")) {
			name += ".i";
		}
		return I18n.translateToLocal(name + ".name");
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.isSideSolid(pos.offset(side.getOpposite()), side)) {
			return false;
		}
		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}

	public static Optional<GateLogic> getGateLogic(ResourceLocation rs) {
		try {
			Class<? extends GateLogic> c = SimpleLogicGates.logicClasses.get(rs);
			if (c != null) {
				return Optional.of(c.newInstance());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static Optional<PartGate> getPartGate(ResourceLocation rs) {
		try {
			Class<? extends GateLogic> c = SimpleLogicGates.logicClasses.get(rs);
			if (c != null) {
				return Optional.of(new PartGate(c.newInstance()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
}
