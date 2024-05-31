package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class PlayerFeathersEffectsHandler {

    @SubscribeEvent
    public static void canApplyEffect(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            if (event.getEffectInstance().getEffect() == FeathersEffects.HOT.get()) {

                event.setResult(canApplyHotEffect(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }

            if (event.getEffectInstance().getEffect() == FeathersEffects.COLD.get()) {

                event.setResult(canApplyColdEffect(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void applyStrain(FeatherAmountEvent.Empty event) {
        event.getEntity().getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {

            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {

                if (event.prevStamina > 0) {

                    event.getEntity().addEffect(new MobEffectInstance(FeathersEffects.STRAINED.get()));
                } else {

                    event.getEntity().removeEffect(FeathersEffects.STRAINED.get());
                }
            }
        });
    }


    public static boolean canApplyColdEffect(ServerPlayer player) {

        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get() || player.getAbilities().invulnerable || player.isCreative()) return false;

        if (Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {

            if (player.hasEffect(ModEffects.GRACE) || player.hasEffect(ModEffects.ICE_RESISTANCE)) {

                return false;
            }
        }

        return !player.hasEffect(FeathersEffects.ENERGIZED.get());
    }

    public static boolean canApplyHotEffect(ServerPlayer player) {

        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() || player.getAbilities().invulnerable || player.isCreative()) return false;

        boolean hasResistance = player.hasEffect(MobEffects.FIRE_RESISTANCE);

        if (Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()) {

            return !player.hasEffect(ModEffects.GRACE) && !hasResistance;
        }
        return !hasResistance;
    }

}
