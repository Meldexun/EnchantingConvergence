package meldexun.magicalconvergence.client;

import meldexun.magicalconvergence.client.gui.screen.ScreenEnchantingConvergence;
import meldexun.magicalconvergence.init.EnchantingConvergenceContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEnchantingConvergence {

	public static void setupClient(FMLClientSetupEvent event) {
		ScreenManager.registerFactory(EnchantingConvergenceContainers.ENCHANTING_TABLE.get(), ScreenEnchantingConvergence::new);
	}

	@SuppressWarnings("resource")
	@OnlyIn(Dist.CLIENT)
	public static PlayerEntity getPlayer() {
		return Minecraft.getInstance().player;
	}

}
