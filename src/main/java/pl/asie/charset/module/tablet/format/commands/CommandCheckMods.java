package pl.asie.charset.module.tablet.format.commands;

import net.minecraftforge.fml.common.Loader;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;

public class CommandCheckMods implements ICommand {
	@Override
	public void call(ITypesetter out, ITokenizer tokenizer) throws TruthError {
		String mode = tokenizer.getParameter("\\checkmods mod mode: all|none|any"); // all some none
		String modList = tokenizer.getParameter("\\checkmods list of mods"); //craftguide NotEnoughItems
		String content = tokenizer.getParameter("\\checkmods when mods installed");
		String other = tokenizer.getOptionalParameter();

		int count = 0;
		String[] mods = modList.split(" ");
		for (String modId : mods) {
			if (modId.startsWith("charset:")) {
				if (ModCharset.isModuleLoaded(modId.substring(8))) {
					count++;
				}
			} else if (Loader.isModLoaded(modId)) {
				count++;
			}
		}

		boolean good = false;
		if (mode.equalsIgnoreCase("all")) {
			good = count == mods.length;
		} else if (mode.equalsIgnoreCase("none")) {
			good = count == 0;
		} else if (mode.equalsIgnoreCase("any")) {
			good = count >= 1;
		} else {
			throw new TruthError("\\checkmods first parameter must be 'all', 'none', or 'any', not '" + mode + "'");
		}

		if (good) {
			out.write(content);
		} else if (other != null) {
			out.write(other);
		}
	}
}
