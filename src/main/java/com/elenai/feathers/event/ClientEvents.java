package com.elenai.feathers.event;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.client.gui.FeathersHudOverlay;
import com.elenai.feathers.config.FeathersClientConfig;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.RequestWeightCTSPacket;
import com.elenai.feathers.util.ArmorHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public class ClientEvents {

    public static int currentWeight;

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
            if (event.phase == TickEvent.Phase.START) {
                if (Minecraft.getInstance().level != null) {
                    ClientFeathersData.overflowing = ClientFeathersData.feathers > 20;

                    if (ClientFeathersData.animationCooldown > 0) {// TODO: improve this animation
                        ClientFeathersData.animationCooldown = ClientFeathersData.animationCooldown - 1;
                    }

                    if (ClientFeathersData.feathers != ClientFeathersData.previousFeathers) {
                        if (ClientFeathersData.feathers > ClientFeathersData.previousFeathers
                                && FeathersClientConfig.REGEN_EFFECT.get()) {
                            ClientFeathersData.animationCooldown = 18;
                        }
                        ClientFeathersData.previousFeathers = ClientFeathersData.feathers;
                    }

                    if (FeathersClientConfig.FADE_WHEN_FULL.get()) {
                        int cooldown = ClientFeathersData.fadeCooldown;
                        if (ClientFeathersData.feathers == ClientFeathersData.maxFeathers
                                || ClientFeathersData.enduranceFeathers > 0) {
                            if (cooldown < FeathersClientConfig.FADE_COOLDOWN.get()) {
                                ClientFeathersData.fadeCooldown = ClientFeathersData.fadeCooldown + 1;
                            }
                        } else {ClientFeathersData.fadeCooldown = 0;}
                    }
                }
            }
        }

        @SubscribeEvent
        public static void tooltipRenderer(ItemTooltipEvent event) {
            if (Minecraft.getInstance().level != null) {
                if (!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof ArmorItem
                        && FeathersClientConfig.DISPLAY_WEIGHTS.get()) { // Surprisingly easy way to render feathers using
                    // fonts

                    FeathersMessages.sendToServer(new RequestWeightCTSPacket(Item.getId(event.getItemStack().getItem()),
                            ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.LIGHTWEIGHT.get(), event.getItemStack()),
                            ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.HEAVY.get(), event.getItemStack())));
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
                            tooltip.add(Component.translatable("text.feathers.tooltip", currentWeight).withStyle(ChatFormatting.BLUE));
                        }
                    }
                }
            }
        }

    }
}