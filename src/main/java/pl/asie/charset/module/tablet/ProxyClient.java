/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tablet;

import com.google.common.base.Charsets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class ProxyClient extends ProxyCommon {
	@Override
	public void onTabletRightClick(World world, EntityPlayer player, EnumHand hand) {
		super.onTabletRightClick(world, player, hand);
		if (world.isRemote) {
			GuiTablet tablet = new GuiTablet(player);
			RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
			if (result != null) {
				try {
					switch (result.typeOfHit) {
						case BLOCK:
							IBlockState state = world.getBlockState(result.getBlockPos());
							ItemStack stack = state.getBlock().getPickBlock(state, result, world, result.getBlockPos(), player);
							ResourceLocation loc = state.getBlock().getRegistryName();
							if (!tablet.openURI(new URI("item://" + loc.getResourceDomain() + "/" + loc.getResourcePath()))) {
								String key = stack.getUnlocalizedName() + ".name";
								String name = I18n.translateToFallback(key);
								if (name.equals(key)) {
									name = I18n.translateToLocal(key);
								}
								tablet.openURI(new URI("about://search/" + URLEncoder.encode(name, Charsets.UTF_8.name())));
							}
							break;
					}
				} catch (URISyntaxException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			FMLCommonHandler.instance().showGuiScreen(tablet);
			player.swingArm(hand);
		}
	}
}