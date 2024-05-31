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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static com.elenai.feathers.client.gui.Icons.*;

@OnlyIn(Dist.CLIENT)
public class FeathersHudOverlay {

    public final static ResourceLocation ICONS = new ResourceLocation(Feathers.MODID, "textures/gui/icons.png");
    public static int k = 0;
    static float alpha = 1.0f;

    private static final ClientFeathersData clientData = ClientFeathersData.getInstance();

    /**
     * Renders the Feathers to the hotbar
     */
    public static final IGuiOverlay FEATHERS = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        int fadeCooldown = FeathersClientConfig.FADE_COOLDOWN.get();
        int fadeIn = FeathersClientConfig.FADE_IN_COOLDOWN.get();
        int fadeOut = FeathersClientConfig.FADE_OUT_COOLDOWN.get();
        int xOffset = FeathersClientConfig.X_OFFSET.get();
        int yOffset = FeathersClientConfig.Y_OFFSET.get();

        int x = screenWidth / 2;

        int rightOffset = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get() ? gui.rightHeight : 0;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.setShaderTexture(0, ICONS);

        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            /*
             * If enabled, decrease the overlay's alpha value relative to the fade in/out duration
             */
            determineFadeEffect(fadeCooldown, fadeOut, fadeIn);

            if (alpha <= 0) return;

            double halfFeathers = Math.ceil(clientData.getFeathers() / 2.0d);
            Icons.Set icons = getIconSet();

            /*
             * Always render the background up to the maximum feather amount
             */
            drawBackground(guiGraphics, screenHeight, halfFeathers, x, xOffset, rightOffset, yOffset, icons);

            /*
             * Render the currently active feathers
             */
            drawFeathers(guiGraphics, screenHeight, halfFeathers, icons, x, xOffset, rightOffset, yOffset);

            /*
             * Only render the currently worn armor
             */
            drawWeight(guiGraphics, screenHeight, halfFeathers, icons, x, xOffset, rightOffset, yOffset);

            /*
             * Render feathers past 20 in a different color
             */
            drawOverflow(guiGraphics, screenHeight, icons, x, xOffset, rightOffset, yOffset);

            /*
             * Render the Regeneration effect
             */
            drawOverlay(guiGraphics, screenHeight, x, xOffset, rightOffset, yOffset);

            if (clientData.isEnergized()) {
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
            lines = drawEndurance(guiGraphics, screenHeight, lines, icons, x, xOffset, rightOffset, yOffset);


            if (FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
                gui.rightHeight += 10 + lines;
            }

            if (Feathers.OB_LOADED) {
                RowCountRenderer.drawBarRowCount(guiGraphics, x + 100 + xOffset, screenHeight - rightOffset + 10 + yOffset,
                        clientData.getFeathers(), true, Minecraft.getInstance().font);
            }

        }

        RenderSystem.disableBlend();

    };

    private static void determineFadeEffect(int fadeCooldown, int fadeOut, int fadeIn) {
        if (FeathersClientConfig.FADE_WHEN_FULL.get()) {
            if (clientData.hasFullStamina()) {
                if (clientData.getFadeCooldown() == fadeCooldown && alpha > 0) {
                    alpha = alpha <= 0.025 ? 0 : alpha - 1.0f / fadeOut;
                }
            } else {
                alpha = alpha >= 1.0f ? 1.0f : alpha + 1.0f / fadeIn;
            }
        }
    }

    private static int drawEndurance(GuiGraphics guiGraphics, int screenHeight, int lines, Set icons, int x, int xOffset, int rightOffset, int yOffset) {
        if (clientData.getEnduranceFeathers() > 0) {

            for (int i = 0; i < Math.ceil((double) clientData.getEnduranceFeathers() / FeathersConstants.STAMINA_PER_FEATHER); i++) { //TODO: fix half feathers
                lines += 10;
                for (int j = 0; j < 10; j++) {
                    var halfEndurance = Math.ceil((double) clientData.getEnduranceFeathers() / 2.0d);
                    if ((((i) * 10.0d) + (j + 1) <= halfEndurance)) {

                        GuiIcon icon = getHalfOrFull(icons, i, (((j + 1) + (10 * i) == halfEndurance)
                                && isEven(clientData.getEnduranceFeathers())));

                        draw(guiGraphics, getXPos(x, j, xOffset), screenHeight - rightOffset + yOffset - ((i) * 10), icon);

                    } else {
                        break;
                    }
                }
            }

        }
        return lines;
    }

    private static void drawOverlay(GuiGraphics guiGraphics, int screenHeight, int x, int xOffset, int rightOffset, int yOffset) {
        for (int i = 0; i < 10; i++) {
            if (clientData.getAnimationCooldown() >= 18 || clientData.getAnimationCooldown() == 10) {
                if ((i + 1 <= Math.ceil((double) clientData.getMaxStamina() / FeathersConstants.STAMINA_PER_FEATHER))) {
                    draw(guiGraphics, getXPos(x, i, xOffset), screenHeight - rightOffset + yOffset, REGEN_OVERLAY);
                }
            }
        }
    }

    private static void drawOverflow(GuiGraphics guiGraphics, int screenHeight, Set icons, int x, int xOffset, int rightOffset, int yOffset) {
        if (clientData.isOverflowing()) {
            var excessFeathers = Math.ceil((double) (clientData.getFeathers() - clientData.getMaxFeathers()));
            for (int i = 0; i < 10; i++) {
                if (i + 1 <= excessFeathers) {

                    GuiIcon icon = getHalfOrFull(icons, i, (i + 1 == excessFeathers) && isEven(clientData.getStamina()));


                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(screenHeight, rightOffset, getHeight(i), yOffset), icon);

                } else {
                    break;
                }
            }
        }
    }

    private static void drawWeight(GuiGraphics guiGraphics, int screenHeight, double halfFeathers, Set icons, int x, int xOffset, int rightOffset, int yOffset) {
        if (clientData.hasWeight()) {
            for (int i = 0; i < 10; i++) {
                var halfWeight = Math.ceil((double) clientData.getWeight() / 2.0d);
                if ((i + 1 <= halfWeight) && (i + 1 <= halfFeathers)) {

                    var icon = getHalfOrFull(icons, i, (i + 1 == halfWeight) && isEven(clientData.getWeight()));

                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(screenHeight, rightOffset, getHeight(i), yOffset), icon);
                } else {
                    break;
                }
            }
        }
    }

    private static void drawFeathers(GuiGraphics guiGraphics, int screenHeight, double halfFeathers, Set icons, int x, int xOffset, int rightOffset, int yOffset) {
        if (clientData.hasFeathers()) {
            for (int i = 0; i < 10; i++) {

                if ((i + 1 <= halfFeathers)) {

                    var icon = getHalfOrFull(icons, i, (i + 1 == halfFeathers && isEven(clientData.getFeathers())));

                    draw(guiGraphics, getXPos(x, i, xOffset), getYPos(screenHeight, rightOffset, getHeight(i), yOffset), icon);

                } else {
                    break;
                }
            }
        }
    }

    private static void drawBackground(GuiGraphics guiGraphics, int screenHeight, double halfFeathers, int x, int xOffset, int rightOffset, int yOffset, Set icons) {
        for (int i = 0; i < 10; i++) {
            if ((i + 1 <= halfFeathers)) {

                draw(guiGraphics, getXPos(x, i, xOffset), getYPos(screenHeight, rightOffset, getHeight(i), yOffset), icons.background());

            }
        }
    }

    private static boolean isEven(int i) {
        return i % 2 == 0;
    }


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

    private static GuiIcon getHalfOrFull(Icons.Set set, int i, boolean isHalf) {
        return isHalf ? set.half() : set.full();
    }

    private static Icons.Set getIconSet() {

        if (clientData.isCold()) {
            return COLD;
        }
        if (clientData.isHot()) {
            return HOT;
        }
        if (clientData.isOverflowing()) {
            return OVERFLOW;
        }

        if (clientData.isEnergized()) {
            return ENERGY;
        }

        if (clientData.isMomentum()) {
            return MOMENTUM;
        }

        if (clientData.isEndurance()) {
            return ENDURANCE;
        }

        if (clientData.isFatigued()) {
            return STRAINED;
        }

        return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? GREEN : NORMAL;
    }


}