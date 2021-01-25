package meldexun.magicalconvergence.event;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import meldexun.magicalconvergence.network.packet.server.SPacketSyncUnlockedEnchantmentLevels;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EnchantingConvergence.MOD_ID)
public class CapabilityEventHandler {

	public static void registerCapabilities() {
		CapabilityUnlockedEnchantmentLevelsProvider.register();
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			event.addCapability(CapabilityUnlockedEnchantmentLevelsProvider.REGISTRY_NAME, new CapabilityUnlockedEnchantmentLevelsProvider(player));
		}
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.world.isRemote) {
			return;
		}
		player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(cap -> {
			EnchantingConvergence.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SPacketSyncUnlockedEnchantmentLevels(player));
		});
	}

	@SubscribeEvent
	public static void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.world.isRemote) {
			return;
		}
		player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(cap -> {
			EnchantingConvergence.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SPacketSyncUnlockedEnchantmentLevels(player));
		});
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.world.isRemote) {
			return;
		}
		player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(cap -> {
			EnchantingConvergence.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SPacketSyncUnlockedEnchantmentLevels(player));
		});
	}

}
