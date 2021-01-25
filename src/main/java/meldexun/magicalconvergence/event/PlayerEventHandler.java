package meldexun.magicalconvergence.event;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.INameable;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantingConvergence.MOD_ID)
public class PlayerEventHandler {

	// TODO disabled for now
	// @SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onXpChangedEvent(PlayerXpEvent.XpChange event) {
		// copied from PlayerEntity#giveExperiencePoints
		PlayerEntity player = event.getPlayer();
		int amount = event.getAmount();

		player.addScore(amount);
		player.experience += (float) amount / (float) xpBarCap(player);
		player.experienceTotal = MathHelper.clamp(player.experienceTotal + amount, 0, Integer.MAX_VALUE);

		while (player.experience < 0.0F) {
			float f = player.experience * (float) xpBarCap(player);
			if (player.experienceLevel > 0) {
				player.addExperienceLevel(-1);
				player.experience = 1.0F + f / (float) xpBarCap(player);
			} else {
				player.addExperienceLevel(-1);
				player.experience = 0.0F;
			}
		}

		while (player.experience >= 1.0F) {
			player.experience = (player.experience - 1.0F) * (float) xpBarCap(player);
			player.addExperienceLevel(1);
			player.experience /= (float) xpBarCap(player);
		}

		event.setCanceled(true);
	}

	private static int xpBarCap(PlayerEntity player) {
		return player.experienceLevel / 5 + 50;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		TileEntity tile = world.getTileEntity(pos);
		if (state.getBlock() == Blocks.ENCHANTING_TABLE && tile instanceof EnchantingTableTileEntity) {
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
			event.getPlayer().openContainer(new SimpleNamedContainerProvider((id, inventory, player) -> {
				return new ContainerEnchantingConvergence(id, inventory, IWorldPosCallable.of(world, pos));
			}, ((INameable) tile).getDisplayName()));
		}
	}

}
