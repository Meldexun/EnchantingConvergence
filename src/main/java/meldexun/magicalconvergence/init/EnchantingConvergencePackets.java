package meldexun.magicalconvergence.init;

import java.util.function.Supplier;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.network.packet.IPacket;
import meldexun.magicalconvergence.network.packet.server.SPacketSyncUnlockedEnchantmentLevels;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel.MessageBuilder;

public class EnchantingConvergencePackets {

	private static int id = 1;

	public static void registerPackets() {
		registerPacket(SPacketSyncUnlockedEnchantmentLevels.class, NetworkDirection.PLAY_TO_CLIENT, SPacketSyncUnlockedEnchantmentLevels::new);
	}

	private static <T extends IPacket> void registerPacket(Class<T> packetClass, NetworkDirection direction, Supplier<T> packetSupplier) {
		MessageBuilder<T> builder = EnchantingConvergence.NETWORK.messageBuilder(packetClass, id++, direction);
		builder.encoder((packet, buffer) -> packet.encode(buffer));
		builder.decoder(buffer -> {
			T packet = packetSupplier.get();
			packet.decode(buffer);
			return packet;
		});
		builder.consumer((packet, ctxSupplier) -> {
			return packet.handle(ctxSupplier);
		});
		builder.add();
	}

}
