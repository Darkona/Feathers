package com.elenai.feathers.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class FeathersCommands {

    public FeathersCommands(){}

    @SubscribeEvent
    public static void onCommandsRegistered(RegisterCommandsEvent event) {

    }
}
