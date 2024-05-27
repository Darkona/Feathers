package com.elenai.feathers.commands;

import com.elenai.feathers.Feathers;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class CommandInit {
    private static final ArrayList<BaseCommand> COMMANDS = new ArrayList<>();

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        COMMANDS.add(new FeathersCommands("feathers", 2, true));

        COMMANDS.forEach(command ->
        {
            if (command.isEnabled() && command.setExecution() != null) {
                dispatcher.register(command.getBuilder());
            }
        });
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Feathers.MODID);

}
