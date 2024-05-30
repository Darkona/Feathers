package com.elenai.feathers.capability;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IModifier;
import com.elenai.feathers.attributes.FeathersAttributes;
import net.minecraft.world.entity.player.Player;

public class Modifiers {

    public static final IModifier REGENERATION = new IModifier() {
        @Override
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.BASE_FEATHERS_PER_SECOND.get());
            if (fps == null) {
                return 0;
            }
            return (int) (fps.getValue() * FeathersConstants.STAMINA_PER_FEATHER / 20);
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
            var fps = player.getAttribute(FeathersAttributes.BASE_FEATHERS_PER_SECOND.get());
            if (fps == null) {
                return 0;
            }
            return (int) (fps.getValue() * FeathersConstants.STAMINA_PER_FEATHER / -20);
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
        public int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta) {
            var fps = player.getAttribute(FeathersAttributes.BASE_FEATHERS_PER_SECOND.get());
            if (fps == null) {
                return 0;
            }
            int sps = (int) (fps.getValue() * FeathersConstants.STAMINA_PER_FEATHER / 20);
            return Math.max((int) (1 / Math.log(fps.getValue() / 40 + 1.4) - 3.5) * sps, 1);
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
