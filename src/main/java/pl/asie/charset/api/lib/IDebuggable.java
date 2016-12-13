package pl.asie.charset.api.lib;

import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public interface IDebuggable {
	void addDebugInformation(List<String> stringList, Side side);
}
