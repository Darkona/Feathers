package com.darkona.feathers.api;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public interface ICapabilityPlugin {

    void onPlayerJoin(EntityJoinLevelEvent event);

    void onPlayerTickBefore(TickEvent.PlayerTickEvent event);

    void onPlayerTickAfter(TickEvent.PlayerTickEvent event);

}
