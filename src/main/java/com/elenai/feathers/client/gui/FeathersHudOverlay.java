package com.elenai.feathers.client.gui;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.overflowingbars.client.handler.RowCountRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class FeathersHudOverlay {

    public final static ResourceLocation ICONS = new ResourceLocation(Feathers.MODID, "textures/gui/icons.png");
    public static int k = 0;
    static float alpha = 1.0f;


    private static final GuiIcon BACK_NORMAL = GuiIcon.featherIcon(0,0);
    private static final GuiIcon BACK_COLD = GuiIcon.featherIcon(1,0);
    private static final GuiIcon BACK_HOT = GuiIcon.featherIcon(2,0);
    private static final GuiIcon BACK_GREEN = GuiIcon.featherIcon(3,0);


    private static final GuiIcon NORMAL_HALF = GuiIcon.featherIcon(0, 1);
    private static final GuiIcon NORMAL_FULL = GuiIcon.featherIcon(0, 2);

    private static final GuiIcon COLD_HALF = GuiIcon.featherIcon(1, 1);
    private static final GuiIcon COLD_FULL = GuiIcon.featherIcon(1, 2);

    private static final GuiIcon HOT_HALF = GuiIcon.featherIcon(2, 1);
    private static final GuiIcon HOT_FULL = GuiIcon.featherIcon(2, 2);

    private static final GuiIcon GREEN_HALF = GuiIcon.featherIcon(3, 1);
    private static final GuiIcon GREEN_FULL = GuiIcon.featherIcon(3, 2);

    private static final GuiIcon ENDUR_HALF = GuiIcon.featherIcon(10, 1);
    private static final GuiIcon ENDUR_FULL = GuiIcon.featherIcon(10, 2);

    private static final GuiIcon STRAIN_HALF = GuiIcon.featherIcon(9, 1);
    private static final GuiIcon STRAIN_FULL = GuiIcon.featherIcon(9, 2);

    private static final GuiIcon ARMOR_HALF = GuiIcon.featherIcon(3, 4);
    private static final GuiIcon ARMOR_FULL = GuiIcon.featherIcon(3, 3);

    private static final GuiIcon ENERGY_HALF = GuiIcon.featherIcon(5, 1);
    private static final GuiIcon ENERGY_FULL = GuiIcon.featherIcon(5, 2);

    private static final GuiIcon OVERFLOW_HALF = GuiIcon.featherIcon(4, 1);
    private static final GuiIcon OVERFLOW_FULL = GuiIcon.featherIcon(4, 2);

    private static final GuiIcon MOMENTUM_HALF = GuiIcon.featherIcon(6, 1);
    private static final GuiIcon MOMENTUM_FULL = GuiIcon.featherIcon(6, 2);


    private static final GuiIcon REGEN_OVERLAY = GuiIcon.featherIcon(0, 3);
    private static final GuiIcon COLD_OVERLAY = GuiIcon.featherIcon(1, 3);
    private static final GuiIcon HOT_OVERLAY = GuiIcon.featherIcon(2, 3);


    /**
     * Renders the Feathers to the hotbar
     */
    public static final IGuiOverlay FEATHERS = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        if (ClientFeathersData.maxStamina <= 0 & ClientFeathersData.enduranceFeathers == 0) return;

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
                if (ClientFeathersData.stamina == ClientFeathersData.maxStamina) {
                    if (ClientFeathersData.fadeCooldown == fadeCooldown && alpha > 0) {
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
                if ((i + 1 <= Math.ceil((double) ClientFeathersData.getMaxFeathers() / 2))) {

                    GuiIcon icon = ((ClientFeathersData.isCold()) ? BACK_COLD : BACK_NORMAL);

                    int height = getHeight(i);
                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset), icon);
                }
            }

            /*
             * Only render the currently active feathers
             */
            double halvedFeathers = Math.ceil(ClientFeathersData.getFeathers() / 2.0d);
            for (int i = 0; i < 10; i++) {
                if ((i + 1 <= halvedFeathers) && ClientFeathersData.getFeathers() > 0) {

                    GuiIcon icon = (i + 1 == halvedFeathers
                            && (ClientFeathersData.getFeathers() % 2 != 0)) ?  getIconHalf() : getIconFull() ;

                    int height = getHeight(i);
                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset), icon);
                } else {
                    break;
                }
            }

            /*
             * Only render the currently worn armor
             */
            for (int i = 0; i < 10; i++) {
                if ((i + 1 <= Math.ceil((double) ClientFeathersData.weight / FeathersConstants.STAMINA_PER_FEATHER)) && (i + 1 <= halvedFeathers)) {

                    GuiIcon icon = (i + 1 == Math.ceil((double) ClientFeathersData.weight / FeathersConstants.STAMINA_PER_FEATHER)
                            && (ClientFeathersData.weight % FeathersConstants.STAMINA_PER_FEATHER != 0)) ? ARMOR_HALF : ARMOR_FULL;

                    int height = getHeight(i);
                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset), icon);
                } else {
                    break;
                }
            }


            /*
             * Render feathers past 20 in a different color
             */
            if (ClientFeathersData.overflowing) {
                for (int i = 0; i < 10; i++) {
                    if (i + 1 <= Math.ceil((double) (ClientFeathersData.getFeathers() - ClientFeathersData.getMaxFeathers()) / FeathersConstants.STAMINA_PER_FEATHER)) {

                        GuiIcon icon = (i + 1 == Math.ceil((double) (ClientFeathersData.stamina - ClientFeathersData.maxStamina) / FeathersConstants.STAMINA_PER_FEATHER)
                                && ClientFeathersData.stamina % FeathersConstants.STAMINA_PER_FEATHER != 0) ? OVERFLOW_HALF : OVERFLOW_FULL;

                        int height = getHeight(i);

                        draw(guiGraphics, getXPos(x, i, xOffset), getYPos(y, rightOffset, height, yOffset), icon);

                    } else {
                        break;
                    }
                }
            }

            /*
             * Render the Regeneration effect
             */
            for (int i = 0; i < 10; i++) {
                if (ClientFeathersData.animationCooldown >= 18 || ClientFeathersData.animationCooldown == 10) {
                    if ((i + 1 <= Math.ceil((double) ClientFeathersData.maxStamina / FeathersConstants.STAMINA_PER_FEATHER))) {
                        int height = getHeight(i);
                        GuiIcon icon = REGEN_OVERLAY;
                        draw(guiGraphics, getXPos(x, i, xOffset), y - rightOffset + yOffset, icon);
                    }
                }
            }

            if (ClientFeathersData.energized) {
                if (k == 100) {
                    k = -40;
                } else {
                    k += 2;
                }
            } else if (k != 0) {
                k = 0;
            }

            if (FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
                rightOffset += 10;
            }

            int lines = 0;

            /*
             * Only render the currently active endurance feathers by line
             */
            for (int i = 0; i < Math.ceil((double) ClientFeathersData.enduranceFeathers / FeathersConstants.STAMINA_PER_FEATHER); i++) { //TODO: fix half feathers
                lines += 10;
                for (int j = 0; j < 10; j++) {
                    if ((((i) * 10.0d) + (j + 1) <= Math.ceil((double) ClientFeathersData.enduranceFeathers / FeathersConstants.STAMINA_PER_FEATHER))
                            && ClientFeathersData.enduranceFeathers > 0) {

                        GuiIcon icon = (((j + 1) + (10 * i) == Math.ceil((double) ClientFeathersData.enduranceFeathers / FeathersConstants.STAMINA_PER_FEATHER)
                                && (ClientFeathersData.enduranceFeathers % 2 != 0)) ? ENDUR_HALF : ENDUR_FULL);

                        draw(guiGraphics, getXPos(x, j, xOffset), y - rightOffset + yOffset - ((i) * 10), icon);

                    } else {
                        break;
                    }
                }
            }

            if (FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
                gui.rightHeight += 10 + lines;
            }

            if (Feathers.OB_LOADED) {
                RowCountRenderer.drawBarRowCount(guiGraphics, x + 100 + xOffset, y - rightOffset + 10 + yOffset,
                        ClientFeathersData.stamina, true, minecraft.font);
            }

        }

        RenderSystem.disableBlend();

    };

    private static void draw(GuiGraphics guiGraphics, int xPos, int yPos, GuiIcon icon) {
        guiGraphics.blit(ICONS, xPos, yPos, icon.x(), icon.y(), icon.width(), icon.height(), 256, 256);
    }

    private static int getYPos(int y, int rightOffset, int height, int yOffset) {
        return y - rightOffset - height + yOffset;
    }

    private static int getXPos(int x, int i, int xOffset) {
        return x + 81 - (i * 8) + xOffset;
    }

    private static int getHeight(int i) {
        return (k > i * 10 && k < (i + 1) * 10) ? 2 : 0;
    }

    private static GuiIcon getIconFull() {
        if (ClientFeathersData.isCold()) {
            return COLD_FULL;
        }
        if (ClientFeathersData.hot) {
            return HOT_FULL;
        }
        if (ClientFeathersData.energized) {
            return ENERGY_FULL;
        }
        return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? GREEN_FULL : NORMAL_FULL;
    }

    private static GuiIcon getIconHalf() {
        if (ClientFeathersData.isCold()) {
            return COLD_HALF;
        }
        if (ClientFeathersData.hot) {
            return HOT_HALF;
        }
        if (ClientFeathersData.energized) {
            return ENERGY_HALF;
        }
        return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? GREEN_HALF : NORMAL_HALF;
    }

}