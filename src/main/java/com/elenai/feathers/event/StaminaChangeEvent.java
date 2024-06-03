package com.elenai.feathers.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public abstract class StaminaChangeEvent extends PlayerEvent {

    public StaminaChangeEvent(Player player) {
        super(player);
    }


    @Cancelable
    public static class Pre extends StaminaChangeEvent {
        public int staminaDelta;
        public int stamina;

        public Pre(Player player, int staminaDelta, int stamina) {
            super(player);
            this.staminaDelta = staminaDelta;
            this.stamina = stamina;
        }
    }

    public static class Post extends StaminaChangeEvent {
        public int stamina;
        public int prevStamina;

        public Post(Player player, int prevStamina, int stamina) {
            super(player);
            this.stamina = stamina;
            this.prevStamina = prevStamina;
        }
    }
}
