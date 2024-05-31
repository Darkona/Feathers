package com.elenai.feathers.capability;

import com.elenai.feathers.api.IModifier;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class StaminaUsageModifiers {

    public static final IModifier DEFAULT_USAGE = new IModifier() {
        @Override
        public void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger usingFeathers) {
            //Do nothing
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


}
