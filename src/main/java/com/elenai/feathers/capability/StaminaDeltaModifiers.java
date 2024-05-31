package com.elenai.feathers.capability;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.util.Calculations;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class StaminaDeltaModifiers {

    /**
     * Basic modifier that applies the regeneration effect.
     * This modifier is used to regenerate the player's stamina.
     * The regeneration value is defined in the config.
     * This modifier is applied once per tick.
     */
    public static final IModifier REGENERATION = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                staminaDelta.set(staminaDelta.get() + Calculations.calculateStaminaPerTick(fps.getValue()));
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "regeneration";
        }

    };


    /**
     * This modifier is used to inverse the regeneration effect.
     * Available for modders as an example, but not used in this mod.
     */
    public static final IModifier INVERSE_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                staminaDelta.set(staminaDelta.get() - Calculations.calculateStaminaPerTick(fps.getValue()));
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "inverse_regeneration";
        }
    };


    /**
     * This modifier is used to make the regeneration effect non-linear.
     * Regeneration is faster at the start and slower at the end.
     * Available for modders as an example, but not used in this mod.
     */
    public static final IModifier NON_LINEAR_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
            if (fps == null) {
                staminaDelta.set(Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()));
            } else {
                var staminaPerSecond = Calculations.calculateStaminaPerTick(FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get()) * 20;
                var something = Math.max((int) (1 / Math.log(playerFeathers.getFeathers() / 40d + 1.4) - 3.5) * staminaPerSecond, 1);
                staminaDelta.set(something);
            }
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "non_linear_regeneration";
        }
    };
}
