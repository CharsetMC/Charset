package pl.asie.simplelogic.gates.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.client.model.animation.FastTESR;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.logic.GateLogic;

public class FastTESRGate extends FastTESR<PartGate> {
	@Override
	public void renderTileEntityFast(PartGate te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		GateLogic logic = te.logic;
	}
}
