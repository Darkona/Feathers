package com.elenai.feathers.capability;

import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.config.FeathersCommonConfig;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class Modifiers {

    public static final IModifier REGENERATION = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            return FeathersCommonConfig.REGENERATION.get();
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
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            return -FeathersCommonConfig.REGENERATION.get();
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

        private final Map<Integer, Integer> regenValues = Map.of(
                0, FeathersCommonConfig.REGENERATION.get() * 3,
                6, FeathersCommonConfig.REGENERATION.get() * 2,
                10, FeathersCommonConfig.REGENERATION.get(),
                14, (int) (FeathersCommonConfig.REGENERATION.get() * 0.6)
        );

        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            return regenValues.get(playerFeathers.getFeathers());
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
