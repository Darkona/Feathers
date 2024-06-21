package com.darkona.feathers.client;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.client.gui.FeathersHudOverlay;
import com.darkona.feathers.config.FeathersClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public class ClientEventsManager {


    @Mod.EventBusSubscriber(modid = Feathers.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "feathers", FeathersHudOverlay.FEATHERS);
        }

    }

    @Mod.EventBusSubscriber(modid = Feathers.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {


        @SubscribeEvent
        public static void clientTickEvents(ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
                ClientFeathersData.getInstance().tick();
            }
        }

        @SubscribeEvent
        public static void tooltipRenderer(ItemTooltipEvent event) {
            int currentWeight =  ClientFeathersData.getInstance().getWeight();
            if (Minecraft.getInstance().level != null) {
                if (!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof ArmorItem
                        && FeathersClientConfig.DISPLAY_WEIGHTS.get()) {
                    // Surprisingly easy way to render feathers using
                    // fonts

                    if (currentWeight > 0) {
                        StringBuilder s = new StringBuilder();
                        List<Component> tooltip = event.getToolTip();
                        if (FeathersClientConfig.VISUAL_WEIGHTS.get()) {
                            for (int i = 2; i <= currentWeight + 1; i += 2) {
                                if (i - 1 == currentWeight) {
                                    s.append("b");
                                } else {
                                    s.append("a ");
                                }
                            }
                            s.reverse();
                            tooltip.add(Component.literal(s.toString())
                                                 .withStyle(Style.EMPTY.withFont(new ResourceLocation(Feathers.MODID, "feather_font"))));
                        } else {
                            tooltip.add(Component.translatable("text.green_feathers.tooltip", currentWeight).withStyle(ChatFormatting.BLUE));
                        }
                    }
                }
            }
        }

    }
}