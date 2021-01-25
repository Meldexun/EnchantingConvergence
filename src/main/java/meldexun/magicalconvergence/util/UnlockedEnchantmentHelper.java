package meldexun.magicalconvergence.util;

import meldexun.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevels;
import meldexun.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

// TODO diabled for now
public class UnlockedEnchantmentHelper {

	public static boolean isUnlocked(PlayerEntity player, Enchantment enchantment, int level) {
		if (true) {
			return true;
		}
		return isUnlocked(player, enchantment.getRegistryName(), level);
	}

	public static boolean isUnlocked(PlayerEntity player, ResourceLocation enchantment, int level) {
		if (true) {
			return true;
		}
		LazyOptional<CapabilityUnlockedEnchantmentLevels> cap = player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY);
		if (!cap.isPresent()) {
			return false;
		}
		return cap.orElse(null).isUnlocked(enchantment, level);
	}

	public static boolean unlock(PlayerEntity player, Enchantment enchantment, int level) {
		if (true) {
			return true;
		}
		return unlock(player, enchantment.getRegistryName(), level);
	}

	public static boolean unlock(PlayerEntity player, ResourceLocation enchantment, int level) {
		if (true) {
			return true;
		}
		LazyOptional<CapabilityUnlockedEnchantmentLevels> cap = player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY);
		if (!cap.isPresent()) {
			return false;
		}
		return cap.orElse(null).unlock(enchantment, level);
	}

}
