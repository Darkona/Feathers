package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.StaminaChangeEvent;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class TickEventHandler {

    static Logger log = LogManager.getLogger(Feathers.MODID);

    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {

            handleStaminaDelta(event);

            handleEffects(event);

        }
    }

    /**
     * Apply stamina delta to the player's stamina value. Handles regeneration, effects, etc.
     *
     * @param event Player Tick Event
     */
    public static void handleStaminaDelta(TickEvent.PlayerTickEvent event) {

        Player player = event.player;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                    //If there is cooldown to start regenerating, it will go down before attempting to regenerate again.
                    if (f.getCooldown() > 0) {
                        f.setCooldown(f.getCooldown() - 1);
                        return;
                    }

                    //If there was any change in the delta modifiers, recalculate.
                    //Internal logic will only run if the flag is set to true
                    f.recalculateStaminaDelta(player);

                    //Event to see if something modifies the stamina delta before applying it and after existing modifiers have been applied.

                    int prevStamina = f.getStamina();

                    var preChangeEvent = new StaminaChangeEvent.Pre(player, f.getStaminaDelta(), f.getStamina());
                    var cancelled = MinecraftForge.EVENT_BUS.post(preChangeEvent);

                    if (cancelled) return;
                    if (preChangeEvent.getResult() == Event.Result.DENY) return;

                    f.setStaminaDelta(preChangeEvent.prevStaminaDelta);
                    f.setStamina(preChangeEvent.prevStamina);

                    f.applyStaminaDelta();

                    //If the stamina delta is not zero, then the player's stamina will change
                    if (f.getStaminaDelta() != 0) {

                        if (f.getStaminaDelta() > 0) {
                            //If the stamina delta is positive, only apply if we are not at max stamina. Avoid pointless operations.
                            if (f.getStamina() < f.getMaxStamina()) f.applyStaminaDelta();
                        } else {
                            //If the stamina delta is negative, only apply if we are not at zero stamina. Avoid pointless operations.
                            if (f.getStamina() > 0) f.applyStaminaDelta();
                        }
                    }

                    //If there was any change in stamina
                    if (prevStamina != f.getStamina()) {
                        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, prevStamina, f.getStamina()));
                        if (f.getStamina() <= 0) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player, prevStamina));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() == f.getMaxStamina()) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() % FeathersConstants.STAMINA_PER_FEATHER == 0) {
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                        }

                        if (f.getStamina() < 0) {
                            player.addEffect(new MobEffectInstance(FeathersEffects.STRAINED.get(), -1, 0, false, true));
                        }
                    }

                }
        );

    }


    public static void handleEffects(TickEvent.PlayerTickEvent event) {

        autoApplyColdEffect(event);

        autoApplyHotEffect(event);

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
    public static void autoApplyColdEffect(TickEvent.PlayerTickEvent event) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;

        if (event.player instanceof ServerPlayer player && !player.isCreative()) {

            boolean hasCold = player.hasEffect(FeathersEffects.COLD.get());
            boolean hasHot = player.hasEffect(FeathersEffects.HOT.get());
            int coldDuration = hasCold ? player.getActiveEffectsMap().get(FeathersEffects.COLD.get()).getDuration() : 0;
            int hotDuration = hasHot ? player.getActiveEffectsMap().get(FeathersEffects.HOT.get()).getDuration() : 0;

            if (isInColdSituation(player)) {

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

            if (isInHotSituation(player)) {

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


    public static boolean isInColdSituation(ServerPlayer player) {

        boolean isRaining = player.level().isRaining() && player.level().isRainingAt(player.blockPosition());

        if (Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {
            return Temperature.get(player, Temperature.Trait.BODY) <= -50;
        }

        boolean isInColdBiome = player.level().getBiome(player.blockPosition()).get().coldEnoughToSnow(player.blockPosition());

        return (isInColdBiome && isRaining && player.level().canSeeSky(player.blockPosition())) || player.isFreezing();
    }

    public static boolean isInHotSituation(ServerPlayer player) {

        boolean isBurning = player.wasOnFire || player.isOnFire() || player.isInLava();

        if (Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()) {
            return Temperature.get(player, Temperature.Trait.BODY) >= 50;
        }

        boolean isInHotBiome = player.level().getBiome(player.blockPosition()).get().getModifiedClimateSettings().temperature() > 0.45f;
        isInHotBiome |= player.level().dimension().equals(Level.NETHER);
        return isBurning || isInHotBiome;
    }

}