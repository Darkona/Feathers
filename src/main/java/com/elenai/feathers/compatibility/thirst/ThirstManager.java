package com.elenai.feathers.compatibility.thirst;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.event.FeatherEvent;
import com.elenai.feathers.util.Calculations;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class ThirstManager {


    public static final IModifier THIRSTY = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {


                var calculation = new ThirstManager.ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
                var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstManager.ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST));

                if (cancelled) {

                    return;
                } else if (calculation.getResult() == Event.Result.DEFAULT) {

                    calculation.calculationResult = (iThirst.getThirst() - 20) * FeathersThirstConfig.THIRST_STAMINA_DRAIN.get();

                    var fps = Calculations.calculateFeathersPerSecond(calculation.calculationResult);
                    var modifier = new AttributeModifier(Feathers.MODID + ":thirsty", fps, AttributeModifier.Operation.ADDITION);

                    Objects.requireNonNull(player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get())).addTransientModifier(modifier);
                }

               // staminaDelta.set(staminaDelta.get() + calculation.calculationResult);
            });
        }

        @Override
        public int getOrdinal() {
            return 2;
        }

        @Override
        public String getName() {
            return "thirsty";
        }
    };

    public static final IModifier QUENCHED = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {

                var calculation = new ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST);
                var cancelled = MinecraftForge.EVENT_BUS.post(new ThirstCalculation(player, playerFeathers, ModCapabilities.PLAYER_THIRST));

                if (cancelled) {

                    return;
                } else if (calculation.getResult() == Event.Result.DEFAULT) {

                    calculation.calculationResult = iThirst.getQuenched() * FeathersThirstConfig.THIRST_STAMINA_DRAIN.get();

                    var fps = Calculations.calculateFeathersPerSecond(calculation.calculationResult);
                    var modifier = new AttributeModifier(Feathers.MODID + ":quenched", fps, AttributeModifier.Operation.ADDITION);

                    iThirst.updateThirstData(player);
                    Objects.requireNonNull(player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get())).addPermanentModifier(modifier);
                }

                //staminaDelta.set(staminaDelta.get() + calculation.calculationResult);
            });
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

    @SubscribeEvent
    public static void attachThirstDeltaModifiers(FeatherEvent.AttachDeltaModifiers event) {
        if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {

            if (FeathersThirstConfig.THIRST_STAMINA_DRAIN.get() > 0)
                event.modifiers.add(THIRSTY);

            if(FeathersThirstConfig.QUENCH_REGEN_BONUS_MULTIPLIER.get() > 0)
                event.modifiers.add(QUENCHED);

        }
    }

    public static void handleThirst(TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.START){
            if (Feathers.THIRST_LOADED && FeathersThirstConfig.THIRST_COMPATIBILITY.get()) {
                event.player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(iThirst -> {

                    if(iThirst.getShouldTickThirst()){
                        var thirst = iThirst.getThirst();
                        var quenched = iThirst.getQuenched();

                        //TODO stuff
                    }
                });
            }
        }

    }

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
}
