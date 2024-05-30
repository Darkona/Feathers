package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.effect.PlayerSituationProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
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
        autoApplyColdEffect(event);
        autoApplyHotEffect(event);

    }

    /**
     * Handle the cold mechanic here.
     */
    public static void autoApplyColdEffect(TickEvent.PlayerTickEvent event) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;

        if (event.player instanceof ServerPlayer player && !player.isCreative()) {

            boolean hasCold = player.hasEffect(FeathersEffects.COLD.get());
            boolean hasHot = player.hasEffect(FeathersEffects.HOT.get());
            int coldDuration = hasCold ? player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() : 0;
            int hotDuration = hasHot ? player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() : 0;

            if (PlayerSituationProvider.isInColdSituation(player)) {

                if (hotDuration < 0) {
                    player.removeEffect(FeathersEffects.HOT.get());
                } else if (!hasCold) {
                    player.addEffect(new MobEffectInstance(
                            FeathersEffects.COLD.get(), -1, 0, false, true));
                }

            } else if (hasCold && coldDuration > FeathersCommonConfig.COLD_LINGER.get()) {

                player.removeEffect(FeathersEffects.COLD.get());
                player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(),
                        FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
            }

            if (player.isCreative() && player.hasEffect(FeathersEffects.COLD.get())) {
                player.removeEffect(FeathersEffects.COLD.get());
            }

        }
    }

    /**
     * Handles the hot mechanic here.
     */
    public static void autoApplyHotEffect(TickEvent.PlayerTickEvent event) {
        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get()) return;

        if (event.player instanceof ServerPlayer player && !player.isCreative()) {

            boolean hasCold = player.hasEffect(FeathersEffects.COLD.get());
            boolean hasHot = player.hasEffect(FeathersEffects.HOT.get());
            int hotDuration = hasHot ? player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() : 0;
            int coldDuration = hasCold ? player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() : 0;

            if (PlayerSituationProvider.isInHotSituation(player)) {

                if (hasCold && coldDuration < 0) {
                    player.removeEffect(FeathersEffects.COLD.get());
                } else if (!hasHot) {
                    player.addEffect(new MobEffectInstance(
                            FeathersEffects.HOT.get(), -1, 0, false, true));
                }

            } else if (hotDuration < 0) {
                player.removeEffect(FeathersEffects.HOT.get());
                player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(),
                        FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
            }

            if (player.isCreative() && player.hasEffect(FeathersEffects.HOT.get())) {
                player.removeEffect(FeathersEffects.HOT.get());
            }
        }
    }

}
