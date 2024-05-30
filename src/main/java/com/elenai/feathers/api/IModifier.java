package com.elenai.feathers.api;

import com.elenai.feathers.capability.PlayerFeathers;
import net.minecraft.world.entity.player.Player;

public interface IModifier {

    int apply(Player player, PlayerFeathers playerFeathers, int staminaDelta);

    int getOrdinal();

    String getName();
}
