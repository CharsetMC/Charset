package pl.asie.charset.tweaks.noteblock;

import net.minecraft.item.ItemBlock;

import net.minecraftforge.fml.common.registry.ExistingSubstitutionException;
import net.minecraftforge.fml.common.registry.GameRegistry;

import pl.asie.charset.tweaks.Tweak;

public class TweakImprovedNoteblock extends Tweak {
	public TweakImprovedNoteblock() {
		super("replacements", "noteblock", "Adds an improved note block.", false);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public boolean preInit() {
		try {
			GameRegistry.addSubstitutionAlias("minecraft:noteblock", GameRegistry.Type.BLOCK, new BlockNoteCharset());
			GameRegistry.addSubstitutionAlias("minecraft:noteblock", GameRegistry.Type.ITEM, new ItemBlock(new BlockNoteCharset()));
		} catch (ExistingSubstitutionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
