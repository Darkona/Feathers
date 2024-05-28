package com.elenai.feathers.event;


import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;


public class FeathersEvents {


    public static class FeathersFullEvent extends PlayerEvent {
        public FeathersFullEvent(Player player) {
            super(player);
        }
    }

    public static class FeathersEmptyEvent extends PlayerEvent {
        public FeathersEmptyEvent(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class FeathersRegenerateEvent extends PlayerEvent {
        public FeathersRegenerateEvent(Player player) {
            super(player);
        }
    }
}
