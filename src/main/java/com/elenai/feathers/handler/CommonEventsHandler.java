package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class CommonEventsHandler {


    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!level.isClientSide && (event.getEntity() instanceof ServerPlayer player)) {

            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                var attr = player.getAttribute(FeathersAttributes.BASE_FEATHERS_PER_SECOND.get());

                if (attr != null) {
                    attr.setBaseValue(FeathersCommonConfig.REGENERATION.get());
                }

                f.setShouldRecalculate(true);
                f.recalculateStaminaDelta(player);
                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
        }
    }


    @SubscribeEvent
    public static void onPlayerSleep(PlayerWakeUpEvent event) {
        if (!FeathersCommonConfig.SLEEPING_ALWAYS_RESTORES_FEATHERS.get()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                f.setStamina(f.getMaxStamina());
                f.setStrainFeathers(0);
                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
        }

    }

    @SubscribeEvent
    public static void onPlayerChangeArmor(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).isPresent()) {

                event.addCapability(new ResourceLocation(Feathers.MODID, "properties"), new PlayerFeathersProvider());
            }
        }
    }

    //TODO: Repair this
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(newStore -> {

                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerFeathers.class);
    }



}