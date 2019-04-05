/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.decoration.signs;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.TraitMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponentTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileSign extends TileBase {
	protected TraitMaterial material;
	private final List<ITextComponent> lines = new ArrayList<>();
	private final CommandResultStats stats = new CommandResultStats();

	public TileSign() {
		registerTrait("material", material = new TraitMaterial("material", ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank")));
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		lines.clear();
		if (compound.hasKey("lines", Constants.NBT.TAG_LIST)) {
			NBTTagList list = compound.getTagList("lines", Constants.NBT.TAG_STRING);
			for (int i = 0; i < list.tagCount(); i++) {
				ITextComponent component = ITextComponent.Serializer.jsonToComponent(list.getStringTagAt(i));

				try {
					component = TextComponentUtils.processComponent(new CommandSender(), component, null);
				} catch (CommandException e) {
					// pass
				}

				lines.add(component);
			}
		}

		if (!isClient) {
			stats.readStatsFromNBT(compound);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		NBTTagList list = new NBTTagList();
		for (ITextComponent component : lines) {
			list.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(component)));
		}
		compound.setTag("lines", list);

		if (!isClient) {
			stats.writeStatsToNBT(compound);
		}

		return compound;
	}

	@Override
	public boolean onlyOpsCanSetNbt() {
		return true;
	}

	public boolean executeCommand(EntityPlayer player) {
		boolean executed = false;
		ICommandSender icommandsender = new CommandSender(player);

		for (ITextComponent component : lines) {
			if (component.getStyle().getClickEvent() != null) {
				ClickEvent clickevent = component.getStyle().getClickEvent();

				if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					Objects.requireNonNull(player.getServer()).getCommandManager().executeCommand(icommandsender, clickevent.getValue());
					executed = true;
				}
			}
		}

		return executed;
	}

	public CommandResultStats getStats() {
		return stats;
	}

	public class CommandSender implements ICommandSender {
		private final EntityPlayer player;

		public CommandSender() {
			this.player = null;
		}

		public CommandSender(EntityPlayer player) {
			this.player = player;
		}

		@Override
		public String getName() {
			return player != null ? player.getName() : "Sign";
		}

		@Override
		public ITextComponent getDisplayName() {
			return player != null ? player.getDisplayName() : new TextComponentString("Sign");
		}

		@Override
		public void sendMessage(ITextComponent component) {
			// TODO: Is this a good idea?
			new Notice(TileSign.this, new NotificationComponentTextComponent(component)).sendToAll();
		}

		@Override
		public Entity getCommandSenderEntity() {
			return player;
		}

		@Override
		public boolean canUseCommand(int permLevel, String commandName) {
			return permLevel <= 2;
		}

		@Override
		public World getEntityWorld() {
			return getWorld();
		}

		@Override
		public BlockPos getPosition() {
			return getPos();
		}

		@Override
		public Vec3d getPositionVector() {
			return new Vec3d(getPosition()).add(0.5D, 0.5D, 0.5D);
		}

		@Override
		public void setCommandStat(CommandResultStats.Type type, int amount) {
			if (TileSign.this.world != null && !TileSign.this.world.isRemote) {
				TileSign.this.stats.setCommandStatForSender(TileSign.this.world.getMinecraftServer(), this, type, amount);
			}
		}

		@Nullable
		@Override
		public MinecraftServer getServer() {
			return player != null ? player.getServer() : getWorld().getMinecraftServer();
		}
	}
}
