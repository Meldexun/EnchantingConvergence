package meldexun.magicalconvergence.network.packet.server;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meldexun.magicalconvergence.capability.enchantment.CapabilityUnlockedEnchantmentLevelsProvider;
import meldexun.magicalconvergence.client.ClientEnchantingConvergence;
import meldexun.magicalconvergence.network.packet.AbstractPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SPacketSyncUnlockedEnchantmentLevels extends AbstractPacket {

	private Object2IntMap<ResourceLocation> map = new Object2IntOpenHashMap<>();

	public SPacketSyncUnlockedEnchantmentLevels() {

	}

	public SPacketSyncUnlockedEnchantmentLevels(PlayerEntity player) {
		player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(cap -> {
			this.map = new Object2IntOpenHashMap<>(cap.getUnlockedEnchantmentLevels());
		});
	}

	@Override
	public void encode(PacketBuffer buffer) {
		buffer.writeInt(this.map.size());
		for (Object2IntMap.Entry<ResourceLocation> entry : this.map.object2IntEntrySet()) {
			buffer.writeString(entry.getKey().toString());
			buffer.writeInt(entry.getIntValue());
		}
	}

	@Override
	public void decode(PacketBuffer buffer) {
		this.map.clear();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			this.map.put(new ResourceLocation(buffer.readString()), buffer.readInt());
		}
	}

	@Override
	public boolean handle(Supplier<Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			PlayerEntity player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientEnchantingConvergence::getPlayer);
			player.getCapability(CapabilityUnlockedEnchantmentLevelsProvider.CAPABILITY).ifPresent(cap -> {
				cap.setUnlockedEnchantmentLevels(this.map);
			});
		});
		return true;
	}

}
