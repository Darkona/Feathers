package com.elenai.feathers.api;

import com.elenai.feathers.capability.PlayerFeathers;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicInteger;

public interface IModifier {

    void apply(Player player, PlayerFeathers playerFeathers, AtomicInteger staminaDelta);

    int getOrdinal();

    String getName();
}
