package meldexun.magicalconvergence.init;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantingConvergenceContainers {

	private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, EnchantingConvergence.MOD_ID);

	public static final RegistryObject<ContainerType<ContainerEnchantingConvergence>> ENCHANTING_TABLE = CONTAINERS.register("enchanting_table", () -> new ContainerType<>(ContainerEnchantingConvergence::new));

	public static void registerContainers() {
		CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
