package meldexun.magicalconvergence.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class AbstractPacket {

	public abstract void encode(PacketBuffer buffer);

	public abstract void decode(PacketBuffer buffer);

	public abstract boolean handle(Supplier<NetworkEvent.Context> ctxSupplier);

}
