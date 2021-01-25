package meldexun.magicalconvergence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import meldexun.magicalconvergence.client.ClientEnchantingConvergence;
import meldexun.magicalconvergence.init.EnchantingConvergenceCapabilities;
import meldexun.magicalconvergence.init.EnchantingConvergenceContainers;
import meldexun.magicalconvergence.init.EnchantingConvergencePackets;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(EnchantingConvergence.MOD_ID)
public class EnchantingConvergence {

	public static final String MOD_ID = "enchanting_convergence";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static final String NETWORK_VERSION = "1.0.0";
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "main"), () -> NETWORK_VERSION, NETWORK_VERSION::equals, NETWORK_VERSION::equals);

	public EnchantingConvergence() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(EnchantingConvergence::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEnchantingConvergence::setupClient);
		EnchantingConvergenceContainers.registerContainers();
	}

	public static void setup(FMLCommonSetupEvent event) {
		EnchantingConvergenceCapabilities.registerCapabilities();
		EnchantingConvergencePackets.registerPackets();
	}

}
