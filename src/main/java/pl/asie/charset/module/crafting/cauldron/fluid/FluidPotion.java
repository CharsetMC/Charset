package pl.asie.charset.module.crafting.cauldron.fluid;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.charset.api.lib.IFluidExtraInformation;
import pl.asie.charset.lib.misc.FluidBase;
import scala.xml.dtd.EMPTY;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FluidPotion extends FluidBase implements IFluidExtraInformation {
	public static final ResourceLocation TEXTURE_STILL = new ResourceLocation("charset:blocks/dyed_water_still");
	public static final ResourceLocation TEXTURE_FLOWING = new ResourceLocation("charset:blocks/dyed_water_flow");

	public FluidPotion(String fluidName) {
		super(fluidName, TEXTURE_STILL, TEXTURE_FLOWING);
	}

	@Override
	public String getUnlocalizedName() {
		return "item.potion.name";
	}

	public static List<PotionEffect> getEffectsFromStack(FluidStack stack) {
		return stack.tag != null ? PotionUtils.getEffectsFromTag(stack.tag) : Collections.emptyList();
	}

	public static PotionType getPotion(FluidStack stack) {
		return PotionUtils.getPotionTypeFromNBT(stack.tag);
	}

	public static void copyFromPotionItem(FluidStack stack, ItemStack itemStack) {
		setPotion(stack, PotionUtils.getPotionTypeFromNBT(itemStack.getTagCompound()));
		if (stack.tag != null && itemStack.hasTagCompound()) {
			if (itemStack.getTagCompound().hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
				stack.tag.setTag("CustomPotionColor", itemStack.getTagCompound().getTag("CustomPotionColor"));
			}

			if (itemStack.getTagCompound().hasKey("CustomPotionEffects", Constants.NBT.TAG_LIST)) {
				stack.tag.setTag("CustomPotionEffects", itemStack.getTagCompound().getTag("CustomPotionEffects"));
			}
		}
	}

	public static void copyToPotionItem(ItemStack itemStack, FluidStack stack) {
		if (stack.tag != null) {
			if (!itemStack.hasTagCompound()) {
				itemStack.setTagCompound(new NBTTagCompound());
			}

			if (stack.tag.hasKey("Potion", Constants.NBT.TAG_STRING)) {
				itemStack.getTagCompound().setTag("Potion", stack.tag.getTag("Potion"));
			}

			if (stack.tag.hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
				itemStack.getTagCompound().setTag("CustomPotionColor", stack.tag.getTag("CustomPotionColor"));
			}

			if (stack.tag.hasKey("CustomPotionEffects", Constants.NBT.TAG_LIST)) {
				itemStack.getTagCompound().setTag("CustomPotionEffects", stack.tag.getTag("CustomPotionEffects"));
			}
		}
	}

	public static FluidStack setPotion(FluidStack stack, PotionType potionType) {
		ResourceLocation loc = ForgeRegistries.POTION_TYPES.getKey(potionType);

		if (potionType == PotionTypes.EMPTY && stack.tag != null) {
			stack.tag = null;
		} else {
			if (stack.tag == null) {
				stack.tag = new NBTTagCompound();
			}

			stack.tag.setString("Potion", loc.toString());
		}

		return stack;
	}

	@Override
	public int getColor(FluidStack stack) {
		if (stack.tag != null && stack.tag.hasKey("CustomPotionColor", Constants.NBT.TAG_ANY_NUMERIC)) {
			return 0xFF000000 | stack.tag.getInteger("CustomPotionColor");
		}

		return getPotion(stack) == PotionTypes.EMPTY ? 0xFFF800F8 : 0xFF000000 | PotionUtils.getPotionColorFromEffectList(getEffectsFromStack(stack));
	}

	@Override
	public void addInformation(FluidStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		ItemStack itemStack = new ItemStack(Items.POTIONITEM, 1, 0);
		itemStack.setTagCompound(stack.tag);
		PotionUtils.addPotionTooltip(itemStack, tooltip, 1.0F);
	}
}
