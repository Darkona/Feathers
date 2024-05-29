package com.elenai.feathers.handler;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.StaminaChangeEvent;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.momosoftworks.coldsweat.api.util.Temperature;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class StaminaDeltaTickHandler {

    static Logger log = LogManager.getLogger(Feathers.MODID);

    /**
     * Apply stamina delta to the player's stamina value. Handles regeneration, effects, etc.
     *
     * @param event Player Tick Event
     */
    public static void applyStaminaChanges(TickEvent.PlayerTickEvent event) {

        Player player = event.player;
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {


                    //If there was any change in the delta modifiers, recalculate.
                    //Internal logic will only run if the flag is set to true
                    f.recalculateStaminaDelta();

                    //Event to see if something modifies the stamina delta before applying it and after existing modifiers have been applied.
                    int prevStaminaDelta = f.getStaminaDelta();
                    int prevStamina = f.getStamina();
                    var preChangeEvent = new StaminaChangeEvent.Pre(player, f.getStaminaDelta(), f.getStamina());
                    MinecraftForge.EVENT_BUS.post(preChangeEvent);

                    if (preChangeEvent.hasResult() && preChangeEvent.getResult() == Event.Result.DENY) return;
                    if (preChangeEvent.isCancelable()) return;

                    f.setStaminaDelta(preChangeEvent.staminaDelta);
                    f.setStamina(preChangeEvent.stamina);

                    //Mark dirty for next tick
                    f.setShouldRecalculate(prevStaminaDelta != f.getStaminaDelta());

                    //If the stamina delta is not zero, then the player's stamina will change
                    if (f.getStaminaDelta() != 0) {

                        if (f.getStaminaDelta() > 0) {
                            //If the stamina delta is positive, only apply if we are not at max stamina. Avoid pointless operations.
                            if (f.getStamina() < f.getMaxStamina()) f.applyStaminaDelta();
                        } else {
                            //If the stamina delta is negative, only apply if we are not at zero stamina. Avoid pointless operations.
                            if(f.getStamina() > 0) f.applyStaminaDelta();
                        }
                    }

                    //If there was any change in stamina
                    if (prevStamina != f.getStamina()) {
                        MinecraftForge.EVENT_BUS.post(new StaminaChangeEvent.Post(player, prevStamina, f.getStamina()));
                        if (f.getStamina() == 0) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Empty(player));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() == f.getMaxStamina()) {

                            MinecraftForge.EVENT_BUS.post(new FeatherAmountEvent.Full(player));
                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);

                        } else if (f.getStamina() % FeathersConstants.STAMINA_PER_FEATHER == 0) {

                            FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                        }
                    }

                }
        );
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

    private static int applyThirstModifiers(Player player, int maxCooldown) {
        if (Feathers.THIRST_LOADED && FeathersCommonConfig.THIRST_COMPATIBILITY.get()) {
            AtomicInteger thirstReduction = new AtomicInteger(0);
            AtomicInteger quenchBonus = new AtomicInteger(0);

            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {
                thirstReduction.set((20 - iThirst.getThirst()) * FeathersCommonConfig.THIRST_REGEN_REDUCTION_MULTIPLIER.get());
                quenchBonus.set(iThirst.getQuenched() * FeathersCommonConfig.QUENCH_REGEN_BONUS_MULTIPLIER.get());
            });
            maxCooldown += thirstReduction.get() - quenchBonus.get();

        }
        return maxCooldown;
    }

}
