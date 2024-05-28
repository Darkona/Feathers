package com.elenai.feathers.event;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.effect.PlayerSituationProvider;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.Effect;
import com.elenai.feathers.networking.packet.EffectChangeSTCPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class EffectHandler {

    @SubscribeEvent
    public static void canApplyEffect(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getEffectInstance().getEffect() == FeathersEffects.HOT.get()) {
                event.setResult(PlayerSituationProvider.canBeHot(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }

            if (event.getEffectInstance().getEffect() == FeathersEffects.COLD.get()) {
                event.setResult(PlayerSituationProvider.canBeCold(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {


    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {

    }

    public static void handleEffects(TickEvent.PlayerTickEvent event) {
        applyColdEffect(event);
        handleHotEffect(event);
        handleEnduranceEffect(event);
    }

    /**
     * Handle the beta cold mechanic here
     */
    public static void applyColdEffect(TickEvent.PlayerTickEvent event) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;
        ServerPlayer player = (ServerPlayer) event.player;
        if (PlayerSituationProvider.isInColdSituation(player)) {

            if (!player.hasEffect(FeathersEffects.COLD.get())) {

                if (player.hasEffect(FeathersEffects.HOT.get()) &&
                        player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() <= 0){
                    player.removeEffect(FeathersEffects.HOT.get());
                    player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), -1, 0, false, true));
                }
            }

        } else if (player.hasEffect(FeathersEffects.COLD.get()) &&
                player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() > FeathersCommonConfig.COLD_LINGER.get()) {

            player.removeEffect(FeathersEffects.COLD.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(),
                    FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }
        if (player.isCreative() && player.hasEffect(FeathersEffects.COLD.get())) {
            player.removeEffect(FeathersEffects.COLD.get());
        }
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
            FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.COLD, f.isCold()), player);
        });
    }

    /**
     * Handle the Fatigue mechanic here
     *
     * @param event
     */
    static void handleHotEffect(TickEvent.PlayerTickEvent event) {
        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get()) return;
        ServerPlayer player = (ServerPlayer) event.player;
        if (PlayerSituationProvider.isInHotSituation(player)) {

            if (!player.hasEffect(FeathersEffects.HOT.get())) {

                if (player.hasEffect(FeathersEffects.COLD.get()))
                    player.removeEffect(FeathersEffects.COLD.get());

                player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(),-1, 0, false, true));
            }
        } else if (player.hasEffect(FeathersEffects.HOT.get())) {
            player.removeEffect(FeathersEffects.HOT.get());
        }

        if (player.isCreative() && player.hasEffect(FeathersEffects.HOT.get())) {
            player.removeEffect(FeathersEffects.HOT.get());
        }
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
            FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.HOT, f.isHot()), player);
        });

    }

    /**
     * Handle the Endurance mechanic here, where the potion leaves if the player has no endurance feathers left
     */
    static void handleEnduranceEffect(TickEvent.PlayerTickEvent event) {
        /*if (FeathersCommonConfig.ENABLE_ENDURANCE.get()) {
            if (event.player.hasEffect(FeathersEffects.ENDURANCE.get()) &&
                    FeathersHelper.getEndurance((ServerPlayer) event.player) == 0) {
                event.player.removeEffect(FeathersEffects.ENDURANCE.get());
            }
        }*/
    }
}
