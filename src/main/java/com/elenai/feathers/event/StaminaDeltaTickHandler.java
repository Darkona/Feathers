package com.elenai.feathers.event;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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
    static void applyStaminaChanges(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                    if (f.getStamina() == 0) {
                        MinecraftForge.EVENT_BUS.post(new FeathersEvents.FeathersEmptyEvent(player));
                        FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                    }
                    if (f.getStamina() == f.getMaxStamina()) {
                        MinecraftForge.EVENT_BUS.post(new FeathersEvents.FeathersFullEvent(player));
                        FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                    }


                    if (f.isShouldRecalculate()) {

                        f.recalculateStaminaDelta();

                    }

                    if (f.getStamina() < f.getMaxStamina() && f.getStaminaDelta() > 0) {
                        MinecraftForge.EVENT_BUS.post(new FeathersEvents.FeathersRegenerateEvent(player));
                    }

                    f.applyStaminaDelta();

                    if (f.getStamina() % FeathersConstants.STAMINA_PER_FEATHER == 0 && f.getStamina() != f.getMaxStamina()) {
                        //log.info("Stamina: " + f.getStamina() + " Max Stamina: " + f.getMaxStamina() + " Delta: " + f.getStaminaDelta());
                       /* if(player.isAlive()){
                            var core = Temperature.get(player, Temperature.Trait.CORE);
                            var body = Temperature.get(player, Temperature.Trait.BODY);
                            var base = Temperature.get(player, Temperature.Trait.BASE);
                            log.info("Temperature Core = " + core + " Body = " + body + " Base = " + base);
                            log.info (Temperature.get(player, Temperature.Trait.BURNING_POINT));
                            log.info (Temperature.get(player, Temperature.Trait.FREEZING_POINT));
                        }*/


                        FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                    }


                }
        );
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
