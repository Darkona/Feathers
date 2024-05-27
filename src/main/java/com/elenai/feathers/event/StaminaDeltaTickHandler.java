package com.elenai.feathers.event;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.FeathersHelper;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class StaminaDeltaTickHandler {

    /**
     * Apply stamina delta to the player's stamina value. Handles regeneration, effects, etc.
     *
     * @param event Player Tick Event
     */
    static void applyStaminaChanges(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                    if (f.isShouldRecalculate()) {
                        f.recalculateStaminaDelta();
                    }
                    f.applyStaminaDelta();
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
