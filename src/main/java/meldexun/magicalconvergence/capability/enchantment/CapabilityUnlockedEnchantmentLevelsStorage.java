package meldexun.magicalconvergence.capability.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CapabilityUnlockedEnchantmentLevelsStorage implements IStorage<CapabilityUnlockedEnchantmentLevels> {

	@Override
	public INBT writeNBT(Capability<CapabilityUnlockedEnchantmentLevels> capability, CapabilityUnlockedEnchantmentLevels instance, Direction side) {
		CompoundNBT compound = new CompoundNBT();
		for (Object2IntMap.Entry<ResourceLocation> entry : instance.getUnlockedEnchantmentLevels().object2IntEntrySet()) {
			compound.putInt(entry.getKey().toString(), entry.getIntValue());
		}
		return compound;
	}

	@Override
	public void readNBT(Capability<CapabilityUnlockedEnchantmentLevels> capability, CapabilityUnlockedEnchantmentLevels instance, Direction side, INBT nbt) {
		if (nbt instanceof CompoundNBT) {
			Object2IntMap<ResourceLocation> map = instance.getUnlockedEnchantmentLevels();
			for (String key : ((CompoundNBT) nbt).keySet()) {
				map.put(new ResourceLocation(key), ((CompoundNBT) nbt).getInt(key));
			}
		}
	}

}
