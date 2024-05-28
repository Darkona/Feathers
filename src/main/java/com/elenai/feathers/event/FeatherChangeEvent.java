package com.elenai.feathers.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class FeatherChangeEvent extends PlayerEvent {

    public FeatherChangeEvent(Player player) {
        super(player);
    }

    @Cancelable
    public static class Regenerate extends FeatherChangeEvent {
        public Regenerate(Player player) {
            super(player);
        }
    }


    public static class Use extends FeatherChangeEvent {


        public int amount;
        public Use(Player player, int amount) {
            super(player);
            this.amount = amount;
        }

        @Cancelable
        public static class Pre extends Use {
            public Pre(Player player, int amount) {
                super(player, amount);
            }
        }

        public static class Post extends Use {
            int prevAmount;
            public Post(Player player, int prevAmount, int amount) {
                super(player, amount);
                this.prevAmount = prevAmount;
            }
        }
    }

    public static class Gain extends FeatherChangeEvent {


        public int amount;

        public Gain(Player player, int amount) {
            super(player);
            this.amount = amount;
        }

        @Cancelable
        public static class Pre extends Use {
            public Pre(Player player, int amount) {
                super(player, amount);
            }
        }

        public static class Post extends Use {
            int prevAmount;
            public Post(Player player, int prevAmount, int amount) {
                super(player, amount);
                this.prevAmount = prevAmount;
            }
        }
    }
}
