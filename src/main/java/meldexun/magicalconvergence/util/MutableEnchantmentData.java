package meldexun.magicalconvergence.util;

import net.minecraft.enchantment.Enchantment;

public class MutableEnchantmentData {

	public final Enchantment enchantment;
	public final int level;
	public final double weight;

	public MutableEnchantmentData(Enchantment e, int level, double weightModifier) {
		this.enchantment = e;
		this.level = level;
		switch (e.getRarity()) {
		case COMMON:
			this.weight = 6.0D * weightModifier;
			break;
		case UNCOMMON:
			this.weight = 5.0D * weightModifier;
			break;
		case RARE:
			this.weight = 4.0D * weightModifier;
			break;
		case VERY_RARE:
			this.weight = 3.0D * weightModifier;
			break;
		default:
			this.weight = 1;
			break;
		}
	}

}
