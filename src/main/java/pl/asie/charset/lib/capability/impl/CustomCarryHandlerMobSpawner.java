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

package pl.asie.charset.lib.capability.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.carry.ICarryHandler;
import pl.asie.charset.api.carry.CustomCarryHandler;

public class CustomCarryHandlerMobSpawner extends CustomCarryHandler {
    protected MobSpawnerBaseLogic spawnerLogic;

    public CustomCarryHandlerMobSpawner(ICarryHandler handler) {
        super(handler);

        spawnerLogic = new MobSpawnerBaseLogic() {
            @Override
            public void broadcastEvent(int id) {

            }

            @Override
            public World getSpawnerWorld() {
                return owner.getCarrier().getEntityWorld();
            }

            @Override
            public BlockPos getSpawnerPosition() {
                return owner.getCarrier().getPosition();
            }
        };
        spawnerLogic.readFromNBT(owner.getTileNBT());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderTileCustom(float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0, 0.5F);
        TileEntityMobSpawnerRenderer.renderMob(spawnerLogic, 0, 0, 0, partialTicks);
        GlStateManager.popMatrix();
        return true;
    }

    @Override
    public void tick() {
        if (spawnerLogic != null) {
            spawnerLogic.updateSpawner();
        }
    }
}
