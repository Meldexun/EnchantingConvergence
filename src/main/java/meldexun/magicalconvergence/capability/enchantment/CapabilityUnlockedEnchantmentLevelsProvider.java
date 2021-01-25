package meldexun.magicalconvergence.capability.enchantment;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.capability.BasicCapabilityProviderSerializable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityUnlockedEnchantmentLevelsProvider extends BasicCapabilityProviderSerializable<CapabilityUnlockedEnchantmentLevels> {

	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(EnchantingConvergence.MOD_ID, "unlocked_enchantment_levels");

	@CapabilityInject(value = CapabilityUnlockedEnchantmentLevels.class)
	public static final Capability<CapabilityUnlockedEnchantmentLevels> CAPABILITY = null;

	public CapabilityUnlockedEnchantmentLevelsProvider(PlayerEntity player) {
		super(CAPABILITY, () -> new CapabilityUnlockedEnchantmentLevels(player));
	}

	public static void register() {
		CapabilityManager.INSTANCE.register(CapabilityUnlockedEnchantmentLevels.class, new CapabilityUnlockedEnchantmentLevelsStorage(), () -> new CapabilityUnlockedEnchantmentLevels(null));
	}

}
