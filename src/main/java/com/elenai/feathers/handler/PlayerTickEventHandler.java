package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.compatibility.thirst.FeathersThirstConfig;
import com.elenai.feathers.compatibility.thirst.ThirstManager;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class PlayerTickEventHandler {

    static Logger log = LogManager.getLogger(Feathers.MODID);

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        handleEffects(player);

        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;


        if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {
            ThirstManager.handleThirst(event);
        }

        player.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> f.tick(player));


    }


    public static void handleEffects(Player player) {

        autoApplyColdEffect(player);

        autoApplyHotEffect(player);

    }

    private static void logStuff(Player player, PlayerFeathers f) {
        log.info("Stamina: " + f.getStamina() + " Max Stamina: " + f.getMaxStamina() + " Delta: " + f.getStaminaDelta());
        if (player.isAlive()) {
            var core = Temperature.get(player, Temperature.Trait.CORE);
            var body = Temperature.get(player, Temperature.Trait.BODY);
            var base = Temperature.get(player, Temperature.Trait.BASE);
            log.info("Temperature Core = " + core + " Body = " + body + " Base = " + base);
            log.info(Temperature.get(player, Temperature.Trait.BURNING_POINT));
            log.info(Temperature.get(player, Temperature.Trait.FREEZING_POINT));
        }
    }

    /**
     * Handle the cold mechanic here.
     */
    public static void autoApplyColdEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;


        boolean hasCold = player.hasEffect(FeathersEffects.COLD.get());
        boolean hasHot = player.hasEffect(FeathersEffects.HOT.get());

        int coldDuration = hasCold ? player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() : 0;
        int hotDuration = hasHot ? player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() : 0;

        if (isInColdSituation(player)) {

            if (hotDuration != 0) {
                player.removeEffect(FeathersEffects.HOT.get());
            } else if (!hasCold) {
                player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), -1, 0, false, true));
            }

        } else if (hasCold && coldDuration > FeathersCommonConfig.COLD_LINGER.get()) {

            player.removeEffect(FeathersEffects.COLD.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(),FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }

        if (player.isCreative() && player.hasEffect(FeathersEffects.COLD.get())) {
            player.removeEffect(FeathersEffects.COLD.get());
        }


    }

    /**
     * Handles the hot mechanic here.
     */
    public static void autoApplyHotEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get()) return;


        boolean hasCold = player.hasEffect(FeathersEffects.COLD.get());
        boolean hasHot = player.hasEffect(FeathersEffects.HOT.get());
        int hotDuration = hasHot ? player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() : 0;
        int coldDuration = hasCold ? player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() : 0;

        if (isInHotSituation(player)) {

            if (hasCold && coldDuration < 0) {

                player.removeEffect(FeathersEffects.COLD.get());

            } else if (!hasHot) {

                player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(), -1, 0, false, true));
            }

        } else if (hotDuration < 0) {

            player.removeEffect(FeathersEffects.HOT.get());
            player.addEffect(new MobEffectInstance(FeathersEffects.HOT.get(), FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));

        }

        if (player.isCreative() && player.hasEffect(FeathersEffects.HOT.get())) {

            player.removeEffect(FeathersEffects.HOT.get());

        }

    }

    public static boolean isInColdSituation(Player player) {

        boolean isRaining = player.level().isRaining() && player.level().isRainingAt(player.blockPosition());

        if (Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {

            return Temperature.get(player, Temperature.Trait.BODY) <= -50;
        }

        boolean isInColdBiome = player.level().getBiome(player.blockPosition()).get().coldEnoughToSnow(player.blockPosition());

        return (isInColdBiome && isRaining && player.level().canSeeSky(player.blockPosition())) || player.isFreezing();
    }

    public static boolean isInHotSituation(Player player) {

        boolean isBurning = player.wasOnFire || player.isOnFire() || player.isInLava();

        if (Feathers.COLD_SWEAT_LOADED && FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() && FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()) {

            return Temperature.get(player, Temperature.Trait.BODY) >= 50;
        }

        boolean isInHotBiome = player.level().getBiome(player.blockPosition()).get().getModifiedClimateSettings().temperature() > 0.45f;
        isInHotBiome |= player.level().dimension().equals(Level.NETHER);

        return isBurning || isInHotBiome;
    }

}