package meldexun.magicalconvergence.inventory.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.mojang.datafixers.util.Pair;

import meldexun.magicalconvergence.init.EnchantingConvergenceContainers;
import meldexun.magicalconvergence.util.EnchantingConvergenceHelper;
import meldexun.magicalconvergence.util.MutableEnchantmentData;
import meldexun.magicalconvergence.util.UnlockedEnchantmentHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

public class ContainerEnchantingConvergence extends Container {

	public static final ResourceLocation LOCATION_BLOCKS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
	private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] { EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET };
	private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[] { EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET };
	private final IInventory tableInventory = new Inventory(2) {
		/**
		 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
		 * it hasn't changed and skip it.
		 */
		@Override
		public void markDirty() {
			super.markDirty();
			ContainerEnchantingConvergence.this.onCraftMatrixChanged(this);
		}
	};
	private final PlayerEntity player;
	private final IWorldPosCallable worldPosCallable;
	private final List<Enchantment> enchList = new ArrayList<>();
	private final IntReferenceHolder power = IntReferenceHolder.single();

	public ContainerEnchantingConvergence(int id, PlayerInventory playerInventory) {
		this(id, playerInventory, IWorldPosCallable.DUMMY);
	}

	public ContainerEnchantingConvergence(int id, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable) {
		super(EnchantingConvergenceContainers.ENCHANTING_TABLE.get(), id);
		this.player = playerInventory.player;
		this.worldPosCallable = worldPosCallable;
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			/**
			 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return true;
			}

			/**
			 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
			 * case of armor slots)
			 */
			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {
			/**
			 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 105 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 163));
		}

		for (int k = 0; k < 4; ++k) {
			final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(playerInventory, 39 - k, 195, 107 + k * 18) {
				/**
				 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
				 * the case of armor slots)
				 */
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				/**
				 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
				 */
				@Override
				public boolean isItemValid(ItemStack stack) {
					return stack.canEquip(equipmentslottype, ContainerEnchantingConvergence.this.player);
				}

				/**
				 * Return whether this slot's stack can be taken from this slot.
				 */
				@Override
				public boolean canTakeStack(PlayerEntity playerIn) {
					ItemStack itemstack = this.getStack();
					return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
				}

				@OnlyIn(Dist.CLIENT)
				@Override
				public Pair<ResourceLocation, ResourceLocation> getBackground() {
					return Pair.of(LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
				}
			});
		}

		this.addSlot(new Slot(playerInventory, 40, 217, 161) {
			@OnlyIn(Dist.CLIENT)
			@Override
			public Pair<ResourceLocation, ResourceLocation> getBackground() {
				return Pair.of(LOCATION_BLOCKS_TEXTURE, EMPTY_ARMOR_SLOT_SHIELD);
			}
		});

		this.trackInt(this.power);
		this.calcEnchantingPower();
	}

	public int getEnchantingPower() {
		return this.power.get();
	}

	private int calcEnchantingPower() {
		return this.worldPosCallable.applyOrElse((world, pos) -> {
			int power = 0;

			BlockPos.Mutable mutable = new BlockPos.Mutable();
			for (int k = -1; k <= 1; ++k) {
				for (int l = -1; l <= 1; ++l) {
					if ((k != 0 || l != 0) && world.isAirBlock(mutable.setAndOffset(pos, l, 0, k)) && world.isAirBlock(mutable.setAndOffset(pos, l, 1, k))) {
						power += this.getPower(world, mutable.setAndOffset(pos, l * 2, 0, k * 2));
						power += this.getPower(world, mutable.setAndOffset(pos, l * 2, 1, k * 2));

						if (l != 0 && k != 0) {
							power += this.getPower(world, mutable.setAndOffset(pos, l * 2, 0, k));
							power += this.getPower(world, mutable.setAndOffset(pos, l * 2, 1, k));
							power += this.getPower(world, mutable.setAndOffset(pos, l, 0, k * 2));
							power += this.getPower(world, mutable.setAndOffset(pos, l, 1, k * 2));
						}
					}
				}
			}

			this.power.set(power);
			return power;
		}, 0);
	}

	private float getPower(World world, BlockPos pos) {
		return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		if (inventoryIn != this.tableInventory) {
			return;
		}

		/*
		 * debug
		 * EnchantingConvergence.LOGGER.info("");
		 * for (Enchantment e : ForgeRegistries.ENCHANTMENTS.getValues()) {
		 * int[] arr = new int[e.getMaxLevel()];
		 * for (int i = 1; i <= e.getMaxLevel(); i++) {
		 * int p1 = EnchantingConvergenceHelper.getPowerCost(e, i);
		 * arr[i - 1] = p1;
		 * }
		 * String s = new TranslationTextComponent(e.getName()).getString();
		 * StringBuilder sb = new StringBuilder(s);
		 * for (int j = MathHelper.ceil((24.0D - s.length()) / 8.0D); j > 0; j--) {
		 * sb.append('\t');
		 * }
		 * sb.append('\t');
		 * int diff = 0;
		 * if (arr.length > 1) {
		 * int last = arr[1] - arr[0];
		 * for (int i = 1; i < arr.length - 1; i++) {
		 * if (arr[i + 1] - arr[i] != last) {
		 * diff = Math.max(Math.abs(last - (arr[i + 1] - arr[i])), diff);
		 * last = arr[i + 1] - arr[i];
		 * }
		 * }
		 * }
		 * sb.append(diff);
		 * sb.append('\t');
		 * for (int i = 0; i < arr.length; i++) {
		 * if (arr[i] < 10) {
		 * sb.append(' ');
		 * }
		 * sb.append(arr[i]);
		 * sb.append(' ');
		 * }
		 * // if (diff > 1)
		 * if (arr[arr.length - 1] > 11)
		 * EnchantingConvergence.LOGGER.info("\t{}", sb);
		 * }
		 */

		this.calcEnchantingPower();

		ItemStack stack = inventoryIn.getStackInSlot(0);
		this.enchList.clear();

		if (stack.isEmpty()) {
			return;
		}

		this.enchList.addAll(EnchantingConvergenceHelper.getValidEnchantments(stack, this.player));
	}

	/**
	 * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
	 */
	@Override
	public boolean enchantItem(PlayerEntity playerIn, int id) {
		ItemStack stack = this.tableInventory.getStackInSlot(0);
		ItemStack stack1 = this.tableInventory.getStackInSlot(1);
		EnchantingMode mode = (id >>> 31) == 1 ? EnchantingMode.BOOK : EnchantingMode.NORMAL;
		int enchantmentId = id & 0x7FFFFFFF;
		int level = playerIn.experienceLevel;
		int lapis = stack1.isEmpty() ? 0 : stack1.getCount();
		int power = this.power.get();

		if (stack.isEmpty()) {
			return false;
		}

		if (mode == EnchantingMode.NORMAL) {
			if (enchantmentId >= this.enchList.size()) {
				return false;
			}

			Enchantment enchantment = this.enchList.get(enchantmentId);
			int enchantmentLevel = EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0) + 1;

			if (enchantmentLevel > enchantment.getMaxLevel()) {
				return false;
			}

			Collection<Enchantment> enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
			if (!enchantments.contains(enchantment) && !EnchantmentHelper.areAllCompatibleWith(enchantments, enchantment)) {
				return false;
			}

			int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, enchantment, enchantmentLevel);
			int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, enchantment, enchantmentLevel);
			int powerCost = EnchantingConvergenceHelper.getPowerCost(enchantment, enchantmentLevel);

			if ((!UnlockedEnchantmentHelper.isUnlocked(playerIn, enchantment, enchantmentLevel) || levelCost > level || lapisCost > lapis || powerCost > power) && !playerIn.isCreative()) {
				return false;
			}

			playerIn.onEnchant(stack, levelCost);

			if (!playerIn.abilities.isCreativeMode) {
				stack1.shrink(lapisCost);
				if (stack1.isEmpty()) {
					this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
				}
			}

			if (stack.getItem() == Items.BOOK) {
				CompoundNBT tag = stack.getTag();
				stack = new ItemStack(Items.ENCHANTED_BOOK);

				if (tag != null) {
					stack.setTag(tag.copy());
				}

				setOrAddEnchantmentLevel(stack, enchantment, enchantmentLevel);
				this.tableInventory.setInventorySlotContents(0, stack);
			} else {
				setOrAddEnchantmentLevel(stack, enchantment, enchantmentLevel);
			}

			playerIn.addStat(Stats.ENCHANT_ITEM);
			if (playerIn instanceof ServerPlayerEntity) {
				CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) playerIn, stack, 3);
			}

			this.tableInventory.markDirty();
			this.onCraftMatrixChanged(this.tableInventory);
			this.worldPosCallable.consume((world, pos) -> {
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
			});
		} else if (mode == EnchantingMode.BOOK) {
			if (stack.getItem() != Items.BOOK) {
				return false;
			}

			int levelCost = (enchantmentId + 1) * 10;
			int lapisCost = (enchantmentId + 1) * 5;
			int powerCost = enchantmentId * 4 + 3;

			if ((levelCost > level || lapisCost > lapis || powerCost > power) && !playerIn.isCreative()) {
				return false;
			}

			playerIn.onEnchant(stack, levelCost);

			if (!playerIn.abilities.isCreativeMode) {
				stack1.shrink(lapisCost);
				if (stack1.isEmpty()) {
					this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
				}
			}

			CompoundNBT tag = stack.getTag();
			stack = new ItemStack(Items.ENCHANTED_BOOK);

			if (tag != null) {
				stack.setTag(tag.copy());
			}

			List<MutableEnchantmentData> list = new ArrayList<>();
			double totalWeight = 0;
			for (Enchantment e : EnchantingConvergenceHelper.getValidEnchantments(stack, playerIn)) {
				for (int i = 1; i <= e.getMaxLevel(); i++) {
					int p = EnchantingConvergenceHelper.getPowerCost(e, i);
					if (p <= (enchantmentId - 1) * 4 + 3) {
						continue;
					}
					if (p > enchantmentId * 4 + 3) {
						continue;
					}
					double d = 1.0D;
					if (UnlockedEnchantmentHelper.isUnlocked(playerIn, e, i)) {
						d *= 0.25D;
					}
					MutableEnchantmentData enchData = new MutableEnchantmentData(e, i, d);
					list.add(enchData);
					totalWeight += enchData.weight;
				}
			}
			double weight = new Random().nextDouble() * totalWeight;
			MutableEnchantmentData selectedEnchData = null;
			for (MutableEnchantmentData enchData : list) {
				if ((weight -= enchData.weight) < 0) {
					selectedEnchData = enchData;
					break;
				}
			}
			if (selectedEnchData == null) {
				return false;
			}

			UnlockedEnchantmentHelper.unlock(playerIn, selectedEnchData.enchantment, selectedEnchData.level);
			EnchantedBookItem.addEnchantment(stack, new EnchantmentData(selectedEnchData.enchantment, selectedEnchData.level));
			this.tableInventory.setInventorySlotContents(0, stack);

			playerIn.addStat(Stats.ENCHANT_ITEM);
			if (playerIn instanceof ServerPlayerEntity) {
				CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) playerIn, stack, 3);
			}

			this.tableInventory.markDirty();
			this.onCraftMatrixChanged(this.tableInventory);
			this.worldPosCallable.consume((world, pos) -> {
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
			});
		}

		return true;
	}

	private static void setOrAddEnchantmentLevel(ItemStack stack, Enchantment enchantment, int level) {
		boolean flag = false;
		ResourceLocation registryName = enchantment.getRegistryName();
		ListNBT enchantmentList = stack.getEnchantmentTagList();

		for (int i = 0; i < enchantmentList.size(); i++) {

		}
		for (INBT nbt : enchantmentList) {
			ResourceLocation resourcelocation1 = ResourceLocation.tryCreate(((CompoundNBT) nbt).getString("id"));
			if (resourcelocation1 != null && resourcelocation1.equals(registryName)) {
				((CompoundNBT) nbt).putInt("lvl", level);
				flag = true;
				break;
			}
		}

		if (!flag) {
			if (stack.getItem() == Items.ENCHANTED_BOOK) {
				EnchantedBookItem.addEnchantment(stack, new EnchantmentData(enchantment, level));
			} else {
				stack.addEnchantment(enchantment, level);
			}
		}
	}

	public List<Enchantment> getEnchantmentList() {
		return this.enchList;
	}

	public int getLapisAmount() {
		ItemStack stack = this.tableInventory.getStackInSlot(1);
		return stack.isEmpty() ? 0 : stack.getCount();
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.worldPosCallable.consume((world, pos) -> {
			this.clearContainer(playerIn, playerIn.world, this.tableInventory);
		});
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return isWithinUsableDistance(this.worldPosCallable, playerIn, Blocks.ENCHANTING_TABLE);
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index == 1) {
				if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
					return ItemStack.EMPTY;
				}
			} else if (itemstack1.getItem() == Items.LAPIS_LAZULI) {
				if (!this.mergeItemStack(itemstack1, 1, 2, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (this.inventorySlots.get(0).getHasStack() || !this.inventorySlots.get(0).isItemValid(itemstack1)) {
					return ItemStack.EMPTY;
				}

				ItemStack itemstack2 = itemstack1.copy();
				itemstack2.setCount(1);
				itemstack1.shrink(1);
				this.inventorySlots.get(0).putStack(itemstack2);
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

}
