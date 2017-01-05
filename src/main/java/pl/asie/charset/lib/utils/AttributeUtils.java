package pl.asie.charset.lib.utils;

import com.google.common.base.Charsets;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public final class AttributeUtils {
	public enum Operation {
		ADD,
		ADD_MULTIPLIED,
		MULTIPLY_PLUS_ONE
	}

	private AttributeUtils() {

	}

	public static AttributeModifier newModifier(String name, double amount, Operation operation) {
		return new AttributeModifier(name, amount, operation.ordinal());
	}

	public static AttributeModifier newModifierSingleton(String name, double amount, Operation operation) {
		return new AttributeModifier(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name, amount, operation.ordinal());
	}
}
