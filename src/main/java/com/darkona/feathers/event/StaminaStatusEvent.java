package com.darkona.feathers.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public abstract class StaminaStatusEvent extends PlayerEvent {


    public StaminaStatusEvent(Player player) {
        super(player);
    }

    public static class Full extends StaminaStatusEvent {
        public Full(Player player) {
            super(player);
        }
    }

    public static class Empty extends StaminaStatusEvent {
        public Empty(Player player) {
            super(player);
        }
    }

}
