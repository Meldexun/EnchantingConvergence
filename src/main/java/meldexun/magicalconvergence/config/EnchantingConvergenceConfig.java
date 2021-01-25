package meldexun.magicalconvergence.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class EnchantingConvergenceConfig {

	public static final Config config;
	public static final ForgeConfigSpec spec;
	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		config = specPair.getLeft();
		spec = specPair.getRight();
	}

	public static class Config {
		public final ForgeConfigSpec.BooleanValue checkIfItemHasEnchantability;
		public final ForgeConfigSpec.BooleanValue checkIfEnchantmentIsTreasureEnchantment;
		public final ForgeConfigSpec.BooleanValue checkIfEnchantmentCanGenerateInLoot;

		public Config(ForgeConfigSpec.Builder builder) {
			this.checkIfItemHasEnchantability = builder.comment("").define("checkIfItemHasEnchantability", false);
			this.checkIfEnchantmentIsTreasureEnchantment = builder.comment("").define("checkIfEnchantmentIsTreasureEnchantment", true);
			this.checkIfEnchantmentCanGenerateInLoot = builder.comment("").define("checkIfEnchantmentCanGenerateInLoot", true);
		}

	}

}
