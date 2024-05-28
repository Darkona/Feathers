package com.elenai.feathers.event;


import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;


public class FeatherAmountEvent {


    public static class Full extends PlayerEvent {
        public Full(Player player) {
            super(player);
        }
    }

    public static class Empty extends PlayerEvent {
        public Empty(Player player) {
            super(player);
        }
    }

    public static class Discrete extends PlayerEvent {
        int feathers;
        public Discrete(Player player, int feathers) {
            super(player);
            this.feathers = feathers;
        }
    }


}
