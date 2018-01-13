package pl.asie.charset.module.tablet.format.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordImage;
import scala.Int;

public class CommandImg implements ICommand {
	@Override
	public void call(ITypesetter out, ITokenizer tokenizer) throws TruthError {
		String imgName = tokenizer.getParameter("domain:path/to/image.png");
		String scaleOrWidth = tokenizer.getOptionalParameter();
		String heightS = tokenizer.getOptionalParameter();

		ResourceLocation rl = new ResourceLocation(imgName);
		try {
			Minecraft mc = Minecraft.getMinecraft();
			IResource r = mc.getResourceManager().getResource(rl);
			if (r == null) {
				throw new TruthError("Not found: " + imgName);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new TruthError(e.getMessage());
		}

		WordImage img;
		if (heightS != null) {
			int width = Integer.parseInt(scaleOrWidth);
			int height = Integer.parseInt(heightS);
			img = new WordImage(rl, width, height);
		} else {
			img = new WordImage(rl);
			if (scaleOrWidth != null) {
				img.scale(Double.parseDouble(scaleOrWidth));
			}
		}

		if (out.hasFixedWidth()) {
			img.fitToPage(out.getWidth(), Integer.MAX_VALUE);
		}

		out.write(img);
	}
}
