package com.elenai.feathers.client.gui;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.overflowingbars.client.handler.RowCountRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class FeathersHudOverlay {

	public final static ResourceLocation ICONS = new ResourceLocation(Feathers.MODID, "textures/gui/icons.png");
	public final static int FULL = 34;
	public final static int HALF = 25;

	private static GuiIconCoord NORMAL_BACKGROUND = new GuiIconCoord(16, 0, 9, 9);
	private static GuiIconCoord NORMAL_HALF_FEATHER = new GuiIconCoord(25, 0, 9, 9);
	private static GuiIconCoord NORMAL_FULL_FEATHER = new GuiIconCoord(34, 0, 9, 9);

	private static GuiIconCoord ENDURANCE_HALF_FEATHER = new GuiIconCoord(25, 9, 9, 9);
	private static GuiIconCoord ENDURANCE_FULL_FEATHER = new GuiIconCoord(34, 9, 9, 9);

	private static GuiIconCoord HALF_RED_FEATHER = new GuiIconCoord(61, 9, 9, 9);
	private static GuiIconCoord FULL_RED_FEATHER = new GuiIconCoord(70, 9, 9, 9);


	private static GuiIconCoord ARMORED_HALF_FEATHER = new GuiIconCoord(43, 0, 9, 9);
	private static GuiIconCoord ARMORED_FULL_FEATHER = new GuiIconCoord(52, 0, 9, 9);


	private static GuiIconCoord REGEN_BACKGROUND = new GuiIconCoord(16, 9, 9, 9);
	private static GuiIconCoord ENERGIZED_HALF_FEATHER = new GuiIconCoord(43, 27, 9, 9);
	private static GuiIconCoord ENERGIZED_FULL_FEATHER = new GuiIconCoord(52, 27, 9, 9);


	private static GuiIconCoord COLD_BACKGROUND = new GuiIconCoord(16, 18, 9, 9);
	private static GuiIconCoord COLD_HALF_FEATHER = new GuiIconCoord(25, 18, 9, 9);
	private static GuiIconCoord COLD_FULL_FEATHER = new GuiIconCoord(34, 18, 9, 9);

	private static GuiIconCoord ALTER_FULL_FEATHER = new GuiIconCoord(34, 27, 9, 9);
	private static GuiIconCoord ALTER_HALF_FEATHER = new GuiIconCoord(25, 27, 9, 9);

	private static GuiIconCoord HOT_HALF_FEATHER = new GuiIconCoord(61, 0, 9, 9);
	private static GuiIconCoord HOT_FULL_FEATHER = new GuiIconCoord(70, 0, 9, 9);


	private static GuiIconCoord OVERFLOW_HALF_FEATHER = new GuiIconCoord(61, 18, 9, 9);
	private static GuiIconCoord OVERFLOW_FULL_FEATHER = new GuiIconCoord(70, 18, 9, 9);



	public static int k = 0;
	static float alpha = 1.0f;

	public static final int ICON_WIDTH = 9;
	public static final int ICON_HEIGHT = 9;
	/**
	 * Renders the Feathers to the hotbar
	 */
	public static final IGuiOverlay FEATHERS = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

		if (ClientFeathersData.getMaxFeathers() <= 0 & ClientFeathersData.getEnduranceFeathers() == 0) return;

		int fadeCooldown = FeathersClientConfig.FADE_COOLDOWN.get();
		int fadeIn = FeathersClientConfig.FADE_IN_COOLDOWN.get();
		int fadeOut = FeathersClientConfig.FADE_OUT_COOLDOWN.get();
		int xOffset = FeathersClientConfig.X_OFFSET.get();
		int yOffset = FeathersClientConfig.Y_OFFSET.get();
		Minecraft minecraft = Minecraft.getInstance();

		int x = screenWidth / 2;
		int y = screenHeight;

		int rightOffset = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get() ? gui.rightHeight : 0;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
		RenderSystem.setShaderTexture(0, ICONS);

		if (!minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
			/*
			 * If enabled, decrease the overlay's alpha value relative to the fade in/out duration
			 */
			if (FeathersClientConfig.FADE_WHEN_FULL.get()) {
				if (ClientFeathersData.getFeathers() == ClientFeathersData.getMaxFeathers()) {
					if (ClientFeathersData.getFadeCooldown() == fadeCooldown && alpha > 0) {
						alpha = alpha <= 0.025 ? 0 : alpha - 1.0f / fadeOut;
					}
				} else {
					alpha = alpha >= 1.0f ? 1.0f : alpha + 1.0f / fadeIn;
				}
			}
			if (alpha <= 0) return;

			/*
			 * Always render the background up to the maximum feather amount
			 */

			for (int i = 0; i < 10; i++) {
				if ((i + 1 <= Math.ceil((double) ClientFeathersData.getMaxFeathers() / 2.0d))) {

					GuiIconCoord icon = ((ClientFeathersData.isCold()) ? COLD_BACKGROUND : NORMAL_BACKGROUND);

					int height = getHeight(i);

					guiGraphics.blit(ICONS, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset),
							icon.x, icon.y, icon.width, icon.height, 256, 256);
				}
			}

			/*
			 * Only render the currently active feathers
			 */
			double halvedFeathers = Math.ceil((double) ClientFeathersData.getFeathers() / 2.0d);
			for (int i = 0; i < 10; i++) {
				if ((i + 1 <= halvedFeathers) && ClientFeathersData.getFeathers() > 0) {

					GuiIconCoord icon = (i + 1 == Math.ceil((double) ClientFeathersData.getFeathers() / 2.0d)
							&& (ClientFeathersData.getFeathers() % 2 != 0)) ? getIconHalf() : getIconFull();

					int height = getHeight(i);

					guiGraphics.blit(ICONS, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset),
							icon.x, icon.y, icon.width, icon.height, 256, 256);
				} else {
					break;
				}
			}

			/*
			 * Only render the currently worn armor
			 */
			//if (Math.ceil(ClientFeathersData.getFeathers() / 20.0d) <= Math.ceil(ClientFeathersData.getWeight() / 20.0d)) {
			for (int i = 0; i < 10; i++) {
				if ((i + 1 <= Math.ceil((double) ClientFeathersData.getWeight() / 2.0d)) && (i + 1 <= halvedFeathers)) {

					GuiIconCoord icon = (i + 1 == Math.ceil((double) ClientFeathersData.getWeight() / 2.0d)
							&& (ClientFeathersData.getWeight() % 2 != 0)) ? ARMORED_HALF_FEATHER : ARMORED_FULL_FEATHER;

					int height = getHeight(i);

					guiGraphics.blit(ICONS, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset),
							icon.x, icon.y, icon.width, icon.height, 256, 256);
				} else {
					break;
				}
			}


			/*
			 * Render feathers past 20 in a different color
			 */
			if (ClientFeathersData.isOverflowing()) {
				for (int i = 0; i < 10; i++) {
					if (i + 1 <= Math.ceil((double) (ClientFeathersData.getFeathers() - 20) / 2.0d)) {

						GuiIconCoord icon = (i + 1 == Math.ceil((double) (ClientFeathersData.getFeathers() - 20) / 2.0d)
								&& ClientFeathersData.getFeathers() % 2 != 0) ? OVERFLOW_HALF_FEATHER : OVERFLOW_FULL_FEATHER;

						int height = getHeight(i);

						guiGraphics.blit(ICONS, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset),
								icon.x, icon.y, icon.width, icon.height, 256, 256);
					} else {
						break;
					}
				}
			}

			/*
			 * Render the Regeneration effect
			 */
			for (int i = 0; i < 10; i++) {
				if (ClientFeathersData.getAnimationCooldown() >= 18|| ClientFeathersData.getAnimationCooldown() == 10) {
					if ((i + 1 <= Math.ceil((double) ClientFeathersData.getMaxFeathers() / 2.0d))) {
						int height = getHeight(i);
						GuiIconCoord icon = REGEN_BACKGROUND;
						guiGraphics.blit(ICONS, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset),
								icon.x, icon.y, icon.width, icon.height, 256, 256);
					}
				}
			}

			if (ClientFeathersData.isEnergized()) {
				if (k == 100) {
					k = -40;
				} else {
					k += 2;
				}
			} else if (k != 0) {
				k = 0;
			}

			if(FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
				rightOffset += 10;
			}

			int lines = 0;

			/*
			 * Only render the currently active endurance feathers by line
			 */
			for (int i = 0; i < Math.ceil((double) ClientFeathersData.getEnduranceFeathers() / 20.0d); i++) { //TODO: fix half feathers
				lines += 10;
				for (int j = 0; j < 10; j++) {
					if ((((i) * 10.0d) + (j + 1) <= Math.ceil((double) ClientFeathersData.getEnduranceFeathers() / 2.0d))
							&& ClientFeathersData.getEnduranceFeathers() > 0) {

						GuiIconCoord icon = (((j + 1) + (10 * i) == Math.ceil((double) ClientFeathersData.getEnduranceFeathers() / 2.0d)
								&& (ClientFeathersData.getEnduranceFeathers() % 2 != 0)) ? ENDURANCE_HALF_FEATHER : ENDURANCE_FULL_FEATHER);

						guiGraphics.blit(ICONS, getXPos(x, j, xOffset),
								y /*- 58*/ - rightOffset + yOffset - ((i) * 10),
								icon.x, icon.y, icon.width, icon.height, 256, 256);
					} else {
						break;
					}
				}
			}
			if(FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
				gui.rightHeight += 10 + lines;
			}

			if (Feathers.OB_LOADED) {
				RowCountRenderer.drawBarRowCount(guiGraphics, x + 100 + xOffset, y - rightOffset + 10 + yOffset,
						ClientFeathersData.getFeathers(), true, minecraft.font);
			}

		}

		RenderSystem.disableBlend();

	};

	private static int getYPos(int y, int rightOffset, int height, int yOffset) {
		return y - rightOffset - height + yOffset;
	}

	private static int getXPos(int x, int i, int xOffset) {
		return x + 81 - (i * 8) + xOffset;
	}

	private static int getHeight(int i) {
		return (k > i * 10 && k < (i + 1) * 10) ? 2 : 0;
	}

	private static GuiIconCoord getIconFull(){
		if (ClientFeathersData.isCold()) {
			return COLD_FULL_FEATHER;
		}
		if (ClientFeathersData.isHot()) {
			return HOT_FULL_FEATHER;
		}
		if(ClientFeathersData.isEnergized()){
			return ENERGIZED_FULL_FEATHER;
		}
		return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? ALTER_FULL_FEATHER : NORMAL_FULL_FEATHER;
	}

	private static GuiIconCoord getIconHalf(){
		if (ClientFeathersData.isCold()) {
			return COLD_HALF_FEATHER;
		}
		if (ClientFeathersData.isHot()) {
			return HOT_HALF_FEATHER;
		}
		if(ClientFeathersData.isEnergized()){
			return ENERGIZED_HALF_FEATHER;
		}
		return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? ALTER_HALF_FEATHER : NORMAL_HALF_FEATHER;
	}



}