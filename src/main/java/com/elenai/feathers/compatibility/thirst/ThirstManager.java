package com.elenai.feathers.compatibility.thirst;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.event.FeatherEvent;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class ThirstManager {


    public static final IModifier THIRSTY = new ThirstDeltaModifier();

    public static final IModifier QUENCHED = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            AtomicInteger result = new AtomicInteger(staminaDelta);
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {
                var calculation = new ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
                var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST));
                if(cancelled) {
                    return;
                }else if (calculation.getResult() == Event.Result.DEFAULT) {
                    calculation.calculationResult = iThirst.getQuenched() * FeathersThirstConfig.THIRST_SLOWS_FEATHER_REGEN.get();
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
            return "quenched";
        }
    };

    public static class ThirstCalculation extends PlayerEvent {

        public int calculationResult;
        public PlayerFeathers playerFeathers;

        public Capability<IThirst> thirst;

        public ThirstCalculation(Player player, PlayerFeathers playerFeathers, Capability<IThirst> thirst) {
            super(player);
            this.playerFeathers = playerFeathers;
            this.thirst = thirst;
        }

    }

    @SubscribeEvent
    public static void attachThirstDeltaModifiers(FeatherEvent.AttachDeltaModifiers event) {
        if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {

            if (FeathersThirstConfig.THIRST_SLOWS_FEATHER_REGEN.get() > 0)
                event.modifiers.add(THIRSTY);

        }
    }
}
