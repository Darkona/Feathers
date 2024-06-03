package com.darkona.feathers.commands;

import com.darkona.feathers.api.FeathersAPI;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FeathersCommands extends BaseCommand {


    public static final String FAILURE_EXECUTING_COMMAND = "Failure executing command.";

    public FeathersCommands(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    public LiteralArgumentBuilder<CommandSourceStack> setExecution() {

        feathersCommands(builder);
        maxFeathersCommands(builder);
        regenCommands(builder);
        return builder;
    }

    private void feathersCommands(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack
                .then(Commands.literal("getFeathers")
                              .then(Commands.argument("entities", EntityArgument.entities())
                                            .executes(source ->
                                                    executeGetFeathers(source.getSource(), EntityArgument.getEntities(source, "entities"))
                                            )))
                .then(Commands.literal("setFeathers")
                              .then(Commands.argument("entities", EntityArgument.entities())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer()))
                                            .executes(source ->
                                                    executeSetFeathers(source.getSource(),
                                                            EntityArgument.getEntities(source, "entities"),
                                                            IntegerArgumentType.getInteger(source, "amount"))
                                            )));
    }
    private int executeGetFeathers(CommandSourceStack source, Collection<? extends Entity> entities) {
        Integer x = checkError(source, entities);
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                source.sendSuccess(() -> Component.literal(
                        String.format("%s has %d feathers, %d available.",
                                player.getName().getString(),
                                FeathersAPI.getFeathers(player),
                                FeathersAPI.getAvailableFeathers(player))),
                        true);
            }
        }
        return entities.size();
    }
    private int executeSetFeathers(CommandSourceStack source, Collection<? extends Entity> entities, int amount) {
        Integer x = checkError(source, entities);
        if (x != null) return x;
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                source.sendSuccess(() -> Component.literal(
                        String.format("Set %s feathers to %d", player.getName().getString(), FeathersAPI.setFeathers(player, amount))),true);
            }
        }
        return entities.size();
    }

    private void maxFeathersCommands(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack.then(Commands.literal("setMaxFeathers")
                           .then(Commands.argument("entities", EntityArgument.entities())
                                         .then(Commands.argument("amount", IntegerArgumentType.integer()))
                                         .executes(source ->
                                                 executeSetMaxFeathers(source.getSource(),
                                                         EntityArgument.getEntities(source, "entities"),
                                                         IntegerArgumentType.getInteger(source, "amount"))
                                         )))
             .then(Commands.literal("getMaxFeathers")
                           .then(Commands.argument("entities", EntityArgument.entities())
                                         .executes(source ->
                                                 executeGetMaxFeathers(source.getSource(), EntityArgument.getEntities(source, "entities"))
                                         )));
    }
    private int executeGetMaxFeathers(CommandSourceStack source, Collection<? extends Entity> entities) {

        Integer x = checkError(source, entities);

        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                source.sendSuccess(() -> Component.literal(
                        String.format("%s has a maximum of %d feathers",
                                player.getName(),
                                FeathersAPI.getPlayerMaxFeathers(player))),
                        true);
            }
        }
        return entities.size();
    }
    private int executeSetMaxFeathers(CommandSourceStack source, Collection<? extends Entity> entities, int amount) {
        Integer x = checkError(source, entities);
        if (x != null) return x;
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                FeathersAPI.setMaxFeathers(player, amount);
                source.sendSuccess(() -> Component.literal(
                        String.format("Set maximum feathers to %d feathers for %s",FeathersAPI.getPlayerMaxFeathers(player),player.getName().getString())),true);
            }
        }
        return entities.size();
    }

    private void regenCommands(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack.then(Commands.literal("getFeatherRegen")
                           .then(Commands.argument("entities", EntityArgument.entities())
                                         .executes(source ->
                                                 executeGetFeatherRegen(source.getSource(), EntityArgument.getEntities(source, "entities"))
                                         )))
             .then(Commands.literal("setFeatherRegen")
                           .then(Commands.argument("entities", EntityArgument.entities())
                                         .then(Commands.argument("amount", DoubleArgumentType.doubleArg()))
                                         .executes(source ->
                                                 executeSetFeatherRegen(source.getSource(),
                                                         EntityArgument.getEntities(source, "entities"),
                                                         DoubleArgumentType.getDouble(source, "amount"))
                                         )));
    }
    private int executeGetFeatherRegen(CommandSourceStack source, Collection<? extends Entity> entities) {
        Integer x = checkError(source, entities);
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                double regen = FeathersAPI.getPlayerFeatherRegenerationPerSecond(player);
                source.sendSuccess(() -> Component.literal(String.format("%s has a regeneration of %.2f feathers per second", player.getName(), regen)), true);
            }
        }
        return entities.size();
    }
    private int executeSetFeatherRegen(CommandSourceStack source, Collection<? extends Entity> entities, double amount) {
        Integer x = checkError(source, entities);
        if (x != null) return x;
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                FeathersAPI.setFeatherRegen(player, amount);
                source.sendSuccess(() -> Component.literal(String.format("Set base feather regeneration to %.2f feathers per second for %s", amount, player.getName())), true);
            }
        }
        return entities.size();
    }


    private @Nullable Integer checkError(CommandSourceStack source, Collection<? extends Entity> entities) {
        if (entities.stream().noneMatch(entity -> entity instanceof Player)) {
            source.sendFailure(Component.literal(FAILURE_EXECUTING_COMMAND));
            return 0;
        }
        return null;
    }


}
