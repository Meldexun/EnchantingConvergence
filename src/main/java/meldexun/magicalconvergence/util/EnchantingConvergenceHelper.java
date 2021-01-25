package meldexun.magicalconvergence.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import meldexun.magicalconvergence.config.EnchantingConvergenceConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantingConvergenceHelper {

	public static List<Enchantment> getValidEnchantments(ItemStack stack, PlayerEntity player) {
		if (stack.isEmpty()) {
			return Collections.emptyList();
		}
		if (EnchantingConvergenceConfig.config.checkIfItemHasEnchantability.get() && stack.getItemEnchantability() <= 0 && stack.getItem() != Items.ENCHANTED_BOOK) {
			return Collections.emptyList();
		}
		List<Enchantment> list = new ArrayList<>();
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
			if (EnchantingConvergenceConfig.config.checkIfEnchantmentIsTreasureEnchantment.get() && enchantment.isTreasureEnchantment()) {
				continue;
			}
			if (EnchantingConvergenceConfig.config.checkIfEnchantmentCanGenerateInLoot.get() && !enchantment.canGenerateInLoot()) {
				continue;
			}
			if (!enchantment.canApplyAtEnchantingTable(stack) && ((stack.getItem() != Items.BOOK && stack.getItem() != Items.ENCHANTED_BOOK) || !enchantment.isAllowedOnBooks())) {
				continue;
			}
			list.add(enchantment);
		}
		return list;
	}

	public static int getLevelCost(ItemStack stack, Enchantment enchantment, int level) {
		level = MathHelper.clamp(level, 0, enchantment.getMaxLevel());
		double d1 = calcRequiredEnchantabilityModifier(enchantment, level);
		double d2 = calcRarityModifier(enchantment);
		double d3 = calcItemEnchantabilityModifier(stack);
		double d4 = (d1 + d2) * d3;
		return (int) Math.max(Math.round(d4), 1);
	}

	public static int getLapisCost(ItemStack stack, Enchantment enchantment, int level) {
		level = MathHelper.clamp(level, 0, enchantment.getMaxLevel());
		double d1 = calcRequiredEnchantabilityModifier(enchantment, level);
		double d2 = calcRarityModifier(enchantment);
		double d3 = calcItemEnchantabilityModifier(stack);
		double d4 = (d1 + d2) * d3;
		return (int) Math.max(Math.round(d4), 1);
	}

	public static int getPowerCost(Enchantment enchantment, int level) {
		level = MathHelper.clamp(level, 0, enchantment.getMaxLevel());
		int min = enchantment.getMinEnchantability(level);
		int i = Math.min(enchantment.getMinEnchantability(1), 5);
		double d1 = (min - i * 0.5D) / 35.0D;
		double d2 = ((double) level - 1.0D) / (double) enchantment.getMaxLevel();
		return MathHelper.clamp((int) Math.round((d1 + d2) / 2.0D * 15.0D), 0, 15);
	}

	private static double C = 16.0D;
	private static double F = 0.12D;
	private static double W = 8.0D;
	private static double E = 25.0D;

	public static double calcRequiredEnchantabilityModifier(Enchantment enchantment, int level) {
		level = MathHelper.clamp(level, 0, enchantment.getMaxLevel());
		int i = MathHelper.ceil(1.0D + C - (C * C) / (level + C - 1.0D));
		return F * (double) enchantment.getMinEnchantability(i);
	}

	public static double calcRarityModifier(Enchantment enchantment) {
		int i = enchantment.getRarity().getWeight();
		return (10.0D + W) / ((double) i + W);
	}

	public static double calcItemEnchantabilityModifier(ItemStack stack) {
		int i = stack.getItemEnchantability();
		return 1.0D / (1.0D + ((double) i / E));
	}

}
