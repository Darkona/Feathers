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
        public int prevStaminaDelta;
        public int prevStamina;

        public Pre(Player player, int prevStaminaDelta, int prevStamina) {
            super(player);
            this.prevStaminaDelta = prevStaminaDelta;
            this.prevStamina = prevStamina;
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
