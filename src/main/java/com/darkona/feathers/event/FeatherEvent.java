package com.darkona.feathers.event;

import com.darkona.feathers.api.IModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public abstract class FeatherEvent extends PlayerEvent {


    public FeatherEvent(Player player) {
        super(player);
    }

    @Cancelable
    public static class AttachDefaultDeltaModifiers extends Event {

        public final List<IModifier> modifiers;

        public AttachDefaultDeltaModifiers(List<IModifier> modifiers) {
            this.modifiers = modifiers;
        }

        public boolean hasResult() {
            return true;
        }

    }


    public static class AttachDefaultUsageModifiers extends Event {

        public final List<IModifier> modifiers;

        public AttachDefaultUsageModifiers(List<IModifier> modifiers) {
            this.modifiers = modifiers;
        }

        public boolean hasResult() {
            return true;
        }

    }

    public static class Changed extends FeatherEvent {

        public int prev;
        public boolean post;

        public Changed(Player player, int prev, boolean post) {
            super(player);
            this.prev = prev;
            this.post = post;
        }
    }

    @Cancelable
    public static class Use extends FeatherEvent {

        public int amount;

        public Use(Player player, int amount) {
            super(player);
            this.amount = amount;
        }

    }

    @Cancelable
    public static class Gain extends FeatherEvent {

        public int amount;

        public Gain(Player player, int amount) {
            super(player);
            this.amount = amount;
        }
    }


}
