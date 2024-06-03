package com.darkona.feathers.client.gui;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.FeathersConstants;
import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.config.FeathersClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.overflowingbars.client.handler.RowCountRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static com.darkona.feathers.client.gui.Icons.*;

@OnlyIn(Dist.CLIENT)
public class FeathersHudOverlay {

    public final static ResourceLocation ICONS = new ResourceLocation(Feathers.MODID, "textures/gui/icons.png");
    public static final int ICONS_PER_ROW = 10;
    private static final ClientFeathersData clientData = ClientFeathersData.getInstance();
    public static int k = 0;
    static float alpha = 1.0f;
    /**
     * Renders the Feathers to the hotbar
     */
    public static final IGuiOverlay FEATHERS = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        int fadeCooldown = FeathersClientConfig.FADE_COOLDOWN.get();
        int fadeIn = FeathersClientConfig.FADE_IN_COOLDOWN.get();
        int fadeOut = FeathersClientConfig.FADE_OUT_COOLDOWN.get();
        int yOffset = FeathersClientConfig.Y_OFFSET.get();

        int x = screenWidth / 2;

        int rightOffset = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get() ? gui.rightHeight : 0;

        if (Minecraft.getInstance().options.hideGui || !gui.shouldDrawSurvivalElements()) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.setShaderTexture(0, ICONS);
        /*
         * If enabled, decrease the overlay's alpha value relative to the fade in/out duration
         */
        determineFadeEffect(fadeCooldown, fadeOut, fadeIn);

        if (alpha <= 0) return;


        Icons.Set icons = getIconSet();

        /*
         * Always render the background up to the maximum feather amount
         */
        drawBackground(guiGraphics, screenHeight, x, rightOffset, icons);


        if (clientData.hasFeathers()) {
            int totalIcons = (int) (Math.ceil(clientData.getFeathers() / 2.0d));
            drawFeathers(guiGraphics, screenHeight, totalIcons, icons, x, rightOffset, clientData.getFeathers());
        }
        if (clientData.isStrained()) {
            int halfStrainedFeathers = (int) Math.ceil(clientData.getStrainFeathers() / 2.0d);
            drawFeathers(guiGraphics, screenHeight, halfStrainedFeathers, STRAINED, x, rightOffset, clientData.getStrainFeathers());
        }

        /*
         * Render feathers past 20 in a different color
         */
        drawOverflow(guiGraphics, screenHeight, x, rightOffset);
        /*
         * Only render the currently worn armor
         */
        drawWeight(guiGraphics, screenHeight, x, rightOffset);
        /*
         * Render the Regeneration effect
         */
        drawOverlay(guiGraphics, screenHeight, x, rightOffset);

        energizedK();

        if (FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
            rightOffset += ICONS_PER_ROW;
        }

        int lines = 0;
        /*
         * Only render the currently active endurance feathers by line
         */
        lines = drawEndurance(guiGraphics, screenHeight, lines, x, rightOffset);

        if (FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get()) {
            gui.rightHeight += ICONS_PER_ROW + lines;
        }

        if (Feathers.OB_LOADED) {
            RowCountRenderer.drawBarRowCount(guiGraphics, x + 100 + FeathersClientConfig.X_OFFSET.get(), screenHeight - rightOffset + ICONS_PER_ROW + yOffset,
                    clientData.getFeathers(), true, Minecraft.getInstance().font);
        }

        RenderSystem.disableBlend();
    };

    private static void drawBackground(GuiGraphics guiGraphics, int screenHeight, int x, int rightOffset, Set icons) {
        for (int i = 0; i < Math.min(ICONS_PER_ROW, clientData.getMaxFeathers() / 2); i++) {
            int xPos = getXPos(x, i);
            int yPos = getYPos(screenHeight, rightOffset, getHeight(i));
            draw(guiGraphics, xPos, yPos, icons.background());
        }

    }

    private static void drawFeathers(GuiGraphics guiGraphics, int screenHeight, int totalIcons, Set icons, int x, int rightOffset, int feathers) {
        for (int i = 0; i < Math.min(ICONS_PER_ROW, totalIcons); i++) {
            if (i + 1 <= totalIcons) {
                var icon = getHalfOrFull(icons, (i + 1 == totalIcons) && !isEven(feathers));
                var xPos = getXPos(x, i);
                var yPos = getYPos(screenHeight, rightOffset, getHeight(i));
                draw(guiGraphics, xPos, yPos, icon);
            } else {
                break;
            }
        }
    }

    private static void drawOverflow(GuiGraphics guiGraphics, int screenHeight, int x, int rightOffset) {
        var halfFeathers = Math.ceil((double) clientData.getFeathers() / 2.0d);
        if (halfFeathers > ICONS_PER_ROW) {
            for (int i = 0; i < ICONS_PER_ROW; i++) {
                if (i + 1 <= halfFeathers - 10) {
                    GuiIcon icon = getHalfOrFull(Icons.OVERFLOW, (i + 1 == halfFeathers) && isEven(clientData.getStamina()));
                    var xPos = getXPos(x, i);
                    var yPos = getYPos(screenHeight, rightOffset, getHeight(i));
                    draw(guiGraphics, xPos, yPos, icon);
                } else {
                    break;
                }
            }
        }
    }

    private static void drawWeight(GuiGraphics guiGraphics, int screenHeight, int x, int rightOffset) {
        if (clientData.hasWeight()) {
            var halfWeight = Math.ceil((double) clientData.getWeight() / 2.0d);
            for (int i = 0; i < Math.min(ICONS_PER_ROW, halfWeight); i++) {
                if (i + 1 <= halfWeight) {

                    var icon = getHalfOrFull(Icons.ARMOR, (i + 1 == halfWeight) && !isEven(clientData.getWeight()));
                    var xPos = getXPos(x, i);
                    var yPos = getYPos(screenHeight, rightOffset, getHeight(i));
                    draw(guiGraphics, xPos, yPos, icon);
                } else {
                    break;
                }
            }
        }
    }

    private static void drawOverlay(GuiGraphics guiGraphics, int screenHeight, int x, int rightOffset) {
        for (int i = 0; i < ICONS_PER_ROW; i++) {
            if (clientData.getAnimationCooldown() >= 18 || clientData.getAnimationCooldown() == ICONS_PER_ROW) {
                if ((i + 1 <= Math.ceil((double) clientData.getMaxStamina() / FeathersConstants.STAMINA_PER_FEATHER))) {
                    var xPos = getXPos(x, i);
                    var yPos = screenHeight - rightOffset;
                    draw(guiGraphics, xPos, yPos, REGEN_OVERLAY);
                }
            }
        }
    }

    private static int drawEndurance(GuiGraphics guiGraphics, int screenHeight, int lines, int x, int rightOffset) {
        if (clientData.isEndurance()) {
            var halfEndurance = Math.ceil((double) clientData.getEnduranceFeathers() / 2.0d);

            for (int i = 0; i < halfEndurance / 10; i++) {
                lines += ICONS_PER_ROW;
                for (int j = 0; j < halfEndurance; j++) {
                    var idk = i * 10.0d + j + 1;
                    if (idk <= halfEndurance) {
                        GuiIcon icon = getHalfOrFull(Icons.ENDURANCE, idk == halfEndurance && !isEven(clientData.getEnduranceFeathers()));
                        var xPos = getXPos(x, j);
                        var yPos = getYPos(screenHeight, rightOffset, getHeight(j));
                        draw(guiGraphics, xPos, yPos - (i * ICONS_PER_ROW), icon);

                    }
                }
            }

        }
        return lines;
    }

    private static void energizedK() {
        if (clientData.isEnergized()) {
            if (k == 100) {
                k = -40;
            } else {
                k += 2;
            }
        } else if (k != 0) {
            k = 0;
        }
    }

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

    private static boolean isEven(int i) {
        return i % 2 == 0;
    }

    private static void draw(GuiGraphics guiGraphics, int xPos, int yPos, GuiIcon icon) {
        guiGraphics.blit(ICONS, xPos, yPos, icon.x(), icon.y(), icon.width(), icon.height(), 256, 256);
    }

    private static int getYPos(int y, int rightOffset, int height) {
        return y - rightOffset - height + FeathersClientConfig.X_OFFSET.get();
    }

    private static int getXPos(int x, int i) {
        return x + 81 - (i * 8) + FeathersClientConfig.X_OFFSET.get();
    }

    private static int getHeight(int i) {
        return (k > i * ICONS_PER_ROW && k < (i + 1) * ICONS_PER_ROW) ? 2 : 0;
    }

    private static GuiIcon getHalfOrFull(Icons.Set set, boolean isHalf) {
        return isHalf ? set.half() : set.full();
    }

    private static Icons.Set getIconSet() {
        if (clientData.isCold()) return COLD;
        if (clientData.isHot()) return HOT;
        if (clientData.isEnergized()) return ENERGY;
        if (clientData.isMomentum()) return MOMENTUM;
        return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? GREEN : NORMAL;
    }


}