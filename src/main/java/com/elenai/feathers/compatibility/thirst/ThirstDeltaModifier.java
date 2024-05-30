package com.elenai.feathers.compatibility.thirst;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.atomic.AtomicInteger;

public class ThirstDeltaModifier implements IModifier {
    @Override
    public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
        AtomicInteger result = new AtomicInteger(staminaDelta);
        player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {
            var calculation = new ThirstManager.ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
            var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstManager.ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST));
            if (cancelled) {
                return;
            } else if (calculation.getResult() == Event.Result.DEFAULT) {
                calculation.calculationResult = (iThirst.getThirst() - 20) * FeathersThirstConfig.THIRST_SLOWS_FEATHER_REGEN.get();
            }
            result.set(calculation.calculationResult);
        });
        return result.get();
    }

    @Override
    public int getOrdinal() {
        return 2;
    }

    @Override
    public String getName() {
        return "thirsty";
    }
}
