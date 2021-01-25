package meldexun.magicalconvergence.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import meldexun.magicalconvergence.EnchantingConvergence;
import meldexun.magicalconvergence.inventory.container.ContainerEnchantingConvergence;
import meldexun.magicalconvergence.inventory.container.EnchantingMode;
import meldexun.magicalconvergence.util.EnchantingConvergenceHelper;
import meldexun.magicalconvergence.util.UnlockedEnchantmentHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Copied from {@link EnchantmentScreen}
 */
@SuppressWarnings("deprecation")
public class ScreenEnchantingConvergence extends ContainerScreen<ContainerEnchantingConvergence> {

	/** The ResourceLocation containing the Enchantment GUI texture location */
	private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation(EnchantingConvergence.MOD_ID, "textures/gui/container/enchanting_table.png");
	/** The ResourceLocation containing the texture for the Book rendered above the enchantment table */
	private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
	/** The ModelBook instance used for rendering the book on the Enchantment table */
	private static final BookModel MODEL_BOOK = new BookModel();
	/** A Random instance for use with the enchantment gui */
	private final Random random = new Random();
	public int ticks;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	private ItemStack last = ItemStack.EMPTY;

	public ScreenEnchantingConvergence(ContainerEnchantingConvergence container, PlayerInventory playerInventory, ITextComponent textComponent) {
		super(container, playerInventory, textComponent);
		this.xSize = 275;
		this.ySize = 187;
		this.playerInventoryTitleY = 94;
	}

	@Override
	public void tick() {
		super.tick();
		this.tickBook();
		this.updateScrollbar();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isPointInRegion(261, 14, 6, 76, mouseX, mouseY)) {
			this.clickedScrollbar = true;
			return true;
		} else {
			this.clickedScrollbar = false;
		}

		// TODO disabled for now
		/*
		 * if (this.isPointInRegion(0, 0, 10, 10, mouseX, mouseY)) {
		 * this.minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		 * this.enchantingMode = this.enchantingMode == EnchantingMode.NORMAL ? EnchantingMode.BOOK : EnchantingMode.NORMAL;
		 * return true;
		 * }
		 */

		ItemStack stack = this.container.getSlot(0).getStack();
		for (int i = 0; i < this.enchantButtons; i++) {
			if (!this.isPointInRegion(60, 14 + 19 * i, 200, 19, mouseX, mouseY)) {
				continue;
			}
			if (this.enchantingMode == EnchantingMode.NORMAL) {
				if (this.scrollStartIndex + i >= this.container.getEnchantmentList().size()) {
					continue;
				}
				Enchantment ench = this.container.getEnchantmentList().get(this.scrollStartIndex + i);
				int level = EnchantmentHelper.getEnchantmentLevel(ench, stack);
				if (level >= ench.getMaxLevel()) {
					continue;
				}
				if (!this.minecraft.player.isCreative()) {
					if (!UnlockedEnchantmentHelper.isUnlocked(this.minecraft.player, ench, level + 1)) {
						continue;
					}
					int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, ench, level + 1);
					if (levelCost > this.minecraft.player.experienceLevel) {
						continue;
					}
					int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, ench, level + 1);
					if (lapisCost > this.container.getLapisAmount()) {
						continue;
					}
					int powerCost = EnchantingConvergenceHelper.getPowerCost(ench, level + 1);
					if (powerCost > this.container.getEnchantingPower()) {
						continue;
					}
				}
				this.minecraft.playerController.sendEnchantPacket(this.container.windowId, this.scrollStartIndex + i);
				return true;
			} else if (this.enchantingMode == EnchantingMode.BOOK) {
				if (!this.minecraft.player.isCreative()) {
					int levelCost = (i + 1) * 10;
					if (levelCost > this.minecraft.player.experienceLevel) {
						continue;
					}
					int lapisCost = (i + 1) * 5;
					if (lapisCost > this.container.getLapisAmount()) {
						continue;
					}
					int powerCost = i * 4 + 3;
					if (powerCost > this.container.getEnchantingPower()) {
						continue;
					}
				}
				this.minecraft.playerController.sendEnchantPacket(this.container.windowId, (1 << 31) | (this.scrollStartIndex + i));
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderHelper.setupGuiFlatDiffuseLighting();

		// render background
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
		blit(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 512, 256);

		// render scroll bar
		boolean scrollbarEnabled = this.enchantingMode == EnchantingMode.NORMAL && this.container.getEnchantmentList().size() > this.enchantButtons;
		int offset = scrollbarEnabled ? (int) ((double) this.scrollStartIndex / (double) (Math.max(this.container.getEnchantmentList().size() - this.enchantButtons, 0)) * 55.0D) : 0;
		blit(matrixStack, this.guiLeft + 261, this.guiTop + 14 + offset, 219 + (scrollbarEnabled ? 0 : 6), 187, 6, 21, 512, 256);

		// render book
		RenderSystem.matrixMode(5889);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		int k = (int) this.minecraft.getMainWindow().getGuiScaleFactor();
		RenderSystem.viewport((this.width - 320) / 2 * k, (this.height - 240) / 2 * k, 320 * k, 240 * k);
		RenderSystem.translatef(-0.65F, 0.321F, 0.0F);
		RenderSystem.multMatrix(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
		RenderSystem.matrixMode(5888);
		matrixStack.push();
		MatrixStack.Entry matrixstack$entry = matrixStack.getLast();
		matrixstack$entry.getMatrix().setIdentity();
		matrixstack$entry.getNormal().setIdentity();
		matrixStack.translate(0.0D, (double) 3.3F, 1984.0D);
		matrixStack.scale(5.0F, 5.0F, 5.0F);
		matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
		matrixStack.rotate(Vector3f.XP.rotationDegrees(20.0F));
		float f1 = MathHelper.lerp(partialTicks, this.oOpen, this.open);
		matrixStack.translate((double) ((1.0F - f1) * 0.2F), (double) ((1.0F - f1) * 0.1F), (double) ((1.0F - f1) * 0.25F));
		float f2 = -(1.0F - f1) * 90.0F - 90.0F;
		matrixStack.rotate(Vector3f.YP.rotationDegrees(f2));
		matrixStack.rotate(Vector3f.XP.rotationDegrees(180.0F));
		float f3 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.25F;
		float f4 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.75F;
		f3 = (f3 - (float) MathHelper.fastFloor((double) f3)) * 1.6F - 0.3F;
		f4 = (f4 - (float) MathHelper.fastFloor((double) f4)) * 1.6F - 0.3F;
		if (f3 < 0.0F) {
			f3 = 0.0F;
		}

		if (f4 < 0.0F) {
			f4 = 0.0F;
		}

		if (f3 > 1.0F) {
			f3 = 1.0F;
		}

		if (f4 > 1.0F) {
			f4 = 1.0F;
		}

		RenderSystem.enableRescaleNormal();
		MODEL_BOOK.setBookState(0.0F, f3, f4, f1);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(MODEL_BOOK.getRenderType(ENCHANTMENT_TABLE_BOOK_TEXTURE));
		MODEL_BOOK.render(matrixStack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		irendertypebuffer$impl.finish();
		matrixStack.pop();
		RenderSystem.matrixMode(5889);
		RenderSystem.viewport(0, 0, this.minecraft.getMainWindow().getFramebufferWidth(), this.minecraft.getMainWindow().getFramebufferHeight());
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderHelper.setupGui3DDiffuseLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		EnchantmentNameParts.getInstance().reseedRandomGenerator((long) this.minecraft.player.getXPSeed());
		int level = this.minecraft.player.experienceLevel;
		int lapis = this.container.getLapisAmount();
		int power = this.container.getEnchantingPower();
		boolean creative = this.minecraft.player.isCreative();
		ItemStack stack = this.container.getSlot(0).getStack();
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

		for (int i = 0; i < this.enchantButtons; i++) {
			int j = this.scrollStartIndex + i;
			int x1 = this.guiLeft + 60;
			int y1 = this.guiTop + 14;
			this.setBlitOffset(0);
			this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (this.enchantingMode == EnchantingMode.NORMAL) {
				if (j >= this.container.getEnchantmentList().size()) {
					blit(matrixStack, x1, y1 + 19 * i, 0, 187 + 19, 200, 19, 512, 256);
				} else {
					Enchantment ench = this.container.getEnchantmentList().get(j);
					int enchLevel = enchantments.getOrDefault(ench, 0);

					if (!creative && enchLevel < ench.getMaxLevel() && !UnlockedEnchantmentHelper.isUnlocked(this.minecraft.player, ench, enchLevel + 1)) {
						// render button
						blit(matrixStack, x1, y1 + 19 * i, 0, 187 + 19, 200, 19, 512, 256);

						// render enchantment description
						ITextComponent text = this.getDisplayName(ench);
						this.font.drawString(matrixStack, text.getString(), x1 + 6, y1 + 19 * i + 6, 0x342F25);
					} else {
						boolean selected = this.isPointInRegion(60, 14 + 19 * i, 200, 19, x, y);
						// render button
						blit(matrixStack, x1, y1 + 19 * i, 0, 187 + (selected ? 38 : 0), 200, 19, 512, 256);

						if (enchLevel >= ench.getMaxLevel()) {
							// render enchantment description
							ITextComponent text = this.getDisplayName(ench);
							this.font.drawString(matrixStack, text.getString() + " MAX", x1 + 6, y1 + 19 * i + 6, selected ? 0xFFFF80 : 0x685E4A);
						} else if (!enchantments.keySet().contains(ench) && !EnchantmentHelper.areAllCompatibleWith(enchantments.keySet(), ench)) {
							// render enchantment description
							ITextComponent text = this.getDisplayName(ench);
							this.font.drawString(matrixStack, TextFormatting.RED.toString() + TextFormatting.STRIKETHROUGH.toString() + text.getString(), x1 + 6, y1 + 19 * i + 6, selected ? 0xFFFF80 : 0x685E4A);
						} else {
							int levelCost = EnchantingConvergenceHelper.getLevelCost(stack, ench, enchLevel + 1);
							int lapisCost = EnchantingConvergenceHelper.getLapisCost(stack, ench, enchLevel + 1);
							int powerCost = EnchantingConvergenceHelper.getPowerCost(ench, enchLevel + 1);
							boolean enoughLevels = level >= levelCost || creative;
							boolean enoughLapis = lapis >= lapisCost || creative;
							boolean enoughPower = power >= powerCost || creative;

							RenderSystem.pushMatrix();
							RenderSystem.translated(0.0D, 0.0D, -200.0D);
							// render xp
							blit(matrixStack, x1 + 143, y1 + 19 * i, 200, 187, 19, 19, 512, 256);

							// render lapis
							this.drawItemStack(new ItemStack(Items.LAPIS_LAZULI), x1 + 162, y1 + 19 * i + 1, null);

							// render bookshelf
							this.drawItemStack(new ItemStack(Blocks.BOOKSHELF), x1 + 181, y1 + 19 * i + 1, null);
							RenderSystem.popMatrix();

							matrixStack.translate(0.0D, 0.0D, 200.0D);

							String requiredLevels = Integer.toString(levelCost);
							drawString(matrixStack, this.font, requiredLevels, x1 - this.font.getStringWidth(requiredLevels) + 143 + 18, y1 + 19 * i + 10, enoughLevels ? 0xF0F0F0 : 0xA50000);

							String requiredLapis = Integer.toString(lapisCost);
							drawString(matrixStack, this.font, requiredLapis, x1 - this.font.getStringWidth(requiredLapis) + 162 + 18, y1 + 19 * i + 10, enoughLapis ? 0xF0F0F0 : 0xA50000);

							String requiredPower = Integer.toString(powerCost);
							drawString(matrixStack, this.font, requiredPower, x1 - this.font.getStringWidth(requiredPower) + 181 + 18, y1 + 19 * i + 10, enoughPower ? 0xF0F0F0 : 0xA50000);

							ITextComponent text = ench.getDisplayName(enchLevel + 1);
							this.font.drawString(matrixStack, text.getString(), x1 + 6, y1 + 19 * i + 6, selected ? 0xFFFF80 : 0x685E4A);
							// drawString(matrixStack, this.font, text.getString(), x1 + 42, y1 + 19 * i + 6, selected ? 0xFFFF80 : 0x685E4A);

							matrixStack.translate(0.0D, 0.0D, -200.0D);
						}
					}
				}
			} else if (this.enchantingMode == EnchantingMode.BOOK) {
				if (stack.isEmpty() || stack.getItem() != Items.BOOK) {
					blit(matrixStack, x1, y1 + 19 * i, 0, 187 + 19, 200, 19, 512, 256);
				} else {
					boolean selected = this.isPointInRegion(60, 14 + 19 * i, 200, 19, x, y);
					// render button
					blit(matrixStack, x1, y1 + 19 * i, 0, 187 + (selected ? 38 : 0), 200, 19, 512, 256);

					int levelCost = (i + 1) * 10;
					int lapisCost = (i + 1) * 5;
					int powerCost = i * 4 + 3;
					boolean enoughLevels = level >= levelCost || creative;
					boolean enoughLapis = lapis >= lapisCost || creative;
					boolean enoughPower = power >= powerCost || creative;

					RenderSystem.pushMatrix();
					RenderSystem.translated(0.0D, 0.0D, -200.0D);
					// render xp
					blit(matrixStack, x1 + 143, y1 + 19 * i, 200, 187, 19, 19, 512, 256);

					// render lapis
					this.drawItemStack(new ItemStack(Items.LAPIS_LAZULI), x1 + 162, y1 + 19 * i + 1, null);

					// render bookshelf
					this.drawItemStack(new ItemStack(Blocks.BOOKSHELF), x1 + 181, y1 + 19 * i + 1, null);
					RenderSystem.popMatrix();

					matrixStack.translate(0.0D, 0.0D, 200.0D);

					String requiredLevels = Integer.toString(levelCost);
					drawString(matrixStack, this.font, requiredLevels, x1 - this.font.getStringWidth(requiredLevels) + 143 + 18, y1 + 19 * i + 10, enoughLevels ? 0xF0F0F0 : 0xA50000);

					String requiredLapis = Integer.toString(lapisCost);
					drawString(matrixStack, this.font, requiredLapis, x1 - this.font.getStringWidth(requiredLapis) + 162 + 18, y1 + 19 * i + 10, enoughLapis ? 0xF0F0F0 : 0xA50000);

					String requiredPower = Integer.toString(powerCost);
					drawString(matrixStack, this.font, requiredPower, x1 - this.font.getStringWidth(requiredPower) + 181 + 18, y1 + 19 * i + 10, enoughPower ? 0xF0F0F0 : 0xA50000);

					ITextProperties itextproperties = EnchantmentNameParts.getInstance().getGalacticEnchantmentName(this.font, 86);
					this.font.func_238418_a_(itextproperties, x1 + 6, y1 + 19 * i + 6, 86, selected ? 0xFFFF80 : 0x685E4A);

					matrixStack.translate(0.0D, 0.0D, -200.0D);
				}
			}
		}

		InventoryScreen.drawEntityOnScreen(this.guiLeft + 216 + 17, this.guiTop + 111 + 40, 20, (float) (this.guiLeft + 216 + 17 - x), (float) (this.guiTop + 111 + 40 - 30 - y), this.minecraft.player);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = this.minecraft.getRenderPartialTicks();
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

		// TODO render tooltips
	}

	public void tickBook() {
		ItemStack itemstack = this.container.getSlot(0).getStack();
		if (!ItemStack.areItemStacksEqual(itemstack, this.last)) {
			this.last = itemstack;

			do {
				this.flipT += (float) (this.random.nextInt(4) - this.random.nextInt(4));
			} while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
		}

		++this.ticks;
		this.oFlip = this.flip;
		this.oOpen = this.open;

		if (!this.container.getEnchantmentList().isEmpty()) {
			this.open += 0.2F;
		} else {
			this.open -= 0.2F;
		}

		this.open = MathHelper.clamp(this.open, 0.0F, 1.0F);
		float f1 = (this.flipT - this.flip) * 0.4F;
		f1 = MathHelper.clamp(f1, -0.2F, 0.2F);
		this.flipA += (f1 - this.flipA) * 0.9F;
		this.flip += this.flipA;
	}

	// ---------- Magical Convergence Start ---------- //

	private final int enchantButtons = 4;
	private final List<Enchantment> prevEnchList = new ArrayList<>();
	private int scrollStartIndex = 0;
	private boolean clickedScrollbar;
	private EnchantingMode enchantingMode = EnchantingMode.NORMAL;

	private void updateScrollbar() {
		List<Enchantment> enchList = this.container.getEnchantmentList();
		boolean flag = this.prevEnchList.size() == enchList.size();

		if (flag) {
			for (int i = 0; i < enchList.size(); i++) {
				Enchantment ench = enchList.get(i);
				if (this.prevEnchList.get(i) != ench) {
					this.prevEnchList.set(i, ench);
					flag = false;
				}
			}
		} else {
			this.prevEnchList.clear();
			this.prevEnchList.addAll(enchList);
		}

		if (this.enchantingMode != EnchantingMode.NORMAL || !flag) {
			this.scrollStartIndex = 0;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (this.enchantingMode == EnchantingMode.NORMAL && this.isPointInRegion(59, 13, 209, 78, mouseX, mouseY)) {
			this.scrollStartIndex = MathHelper.clamp((int) (this.scrollStartIndex - delta), 0, Math.max(this.container.getEnchantmentList().size() - this.enchantButtons, 0));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.enchantingMode == EnchantingMode.NORMAL && this.clickedScrollbar) {
			double y1 = this.guiTop + 14 + 10.5D;
			double y2 = this.guiTop + 14 + 76 - 10.5D;
			double scrollLength = Math.max(this.container.getEnchantmentList().size() - this.enchantButtons, 0);
			this.scrollStartIndex = MathHelper.clamp((int) ((mouseY - y1) / (y2 - y1) * scrollLength + 0.5D), 0, Math.max(this.container.getEnchantmentList().size() - this.enchantButtons, 0));
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	protected void drawItemStack(ItemStack stack, int x, int y, String altText) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.0F, 0.0F, 32.0F);
		this.setBlitOffset(200);
		this.itemRenderer.zLevel = 200.0F;
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) {
			font = this.font;
		}
		this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
		this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, altText);
		this.setBlitOffset(0);
		this.itemRenderer.zLevel = 0.0F;
		RenderSystem.popMatrix();
	}

	protected ITextComponent getDisplayName(Enchantment ench) {
		IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(ench.getName());
		if (ench.isCurse()) {
			iformattabletextcomponent.mergeStyle(TextFormatting.RED);
		} else {
			iformattabletextcomponent.mergeStyle(TextFormatting.GRAY);
		}
		return iformattabletextcomponent;
	}

	@Override
	protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
		return mouseX >= this.guiLeft + x && mouseX < this.guiLeft + x + width && mouseY >= this.guiTop + y && mouseY < this.guiTop + y + height;
	}

}
