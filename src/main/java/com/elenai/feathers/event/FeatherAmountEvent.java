package com.elenai.feathers.event;


import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;


public class FeatherAmountEvent  extends PlayerEvent{


    public FeatherAmountEvent(Player player) {
        super(player);
    }

    public static class Full extends FeatherAmountEvent {
        public Full(Player player) {
            super(player);
        }
    }

    public static class Empty extends FeatherAmountEvent {

        public int prevStamina;
        public Empty(Player player, int prevStamina) {
            super(player);
            this.prevStamina = prevStamina;
        }
    }

    public static class Discrete extends FeatherAmountEvent {
        int feathers;
        public Discrete(Player player, int feathers) {
            super(player);
            this.feathers = feathers;
        }
    }


}
