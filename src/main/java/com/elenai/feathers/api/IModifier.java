package com.elenai.feathers.api;

import com.elenai.feathers.capability.PlayerFeathers;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface IModifier {

    /**
     * This modifier is used to make the regeneration effect non-linear.
     * Regeneration is faster at the start and slower at the end.
     * Available for modders as an example, but not used in this mod.
     */
    IModifier NON_LINEAR_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {

            var staminaPerSecond = FeathersAPI.getPlayerStaminaRegenerationPerTick(player);
            var maxStamina = playerFeathers.getMaxStamina();
            var value = Math.max((int) (1 / Math.log(playerFeathers.getFeathers() / 40d + 1.4) - 3.5) * staminaPerSecond, 1);
            staminaDelta.set(value);

        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

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
    /**
     * This modifier is used to inverse the regeneration effect.
     * Available for modders as an example, but not used in this mod.
     */
    IModifier INVERSE_REGENERATION = new IModifier() {

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            staminaDelta.set(staminaDelta.get() - FeathersAPI.getPlayerStaminaRegenerationPerTick(player));
        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

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
     * Basic modifier that applies the regeneration effect.
     * This modifier is used to regenerate the player's stamina.
     * The regeneration value is defined in the config.
     * This modifier is applied once per tick.
     */
    IModifier REGENERATION = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta) {
            staminaDelta.set(staminaDelta.get() + FeathersAPI.getPlayerStaminaRegenerationPerTick(player));
        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {

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
    IModifier DEFAULT_USAGE = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger usingFeathers) {
            //Do nothing
        }

        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta, AtomicBoolean result) {
            result.set(true);
        }

        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public String getName() {
            return "default";
        }
    };
    /**
     * Usage Modifier Ordinals reference.
     * 0 = Hot effect / Momentum effect;
     * they will be applied first. If you are going to spend more/less feathers, these need to be applied first.
     * <p>
     * 10 = Endurance/Strain effect;
     * If you're going to overspend feathers, or spend endurance feathers this will be applied after knowing how many will be spent.
     * <p>
     * 1 to 9 are available.
     **/
    int[] ordinals = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta);

    void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta, AtomicBoolean result);

    int getOrdinal();

    String getName();


}
