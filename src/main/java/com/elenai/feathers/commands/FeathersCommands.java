package com.elenai.feathers.commands;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.effect.FeathersEffects;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class FeathersCommands extends BaseCommand {


    public FeathersCommands(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    public LiteralArgumentBuilder<CommandSourceStack> setExecution() {
        return builder.then(Commands.literal("setStamina")
                                    .then(Commands.argument("entities", EntityArgument.entities())
                                                  .then(Commands.argument("amount", IntegerArgumentType.integer(0, 2000))
                                                                .executes(source ->
                                                                        executeSetStamina(
                                                                                source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                IntegerArgumentType.getInteger(source, "amount"))
                                                                ))))
                      .then(Commands.literal("setCold")
                                    .then(Commands.argument("entities", EntityArgument.entities())
                                                  .then(Commands
                                                          .argument("status", BoolArgumentType.bool())
                                                          .executes(source ->
                                                                  executeSetStamina(
                                                                          source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                          BoolArgumentType.getBool(source, "status"), "feathers")
                                                          ))))
                      .then(Commands.literal("getStamina")
                                    .then(Commands.argument("entities", EntityArgument.entities())
                                                  .executes(source ->
                                                          executeGetStamina(source.getSource(), EntityArgument.getEntities(source, "entities"))
                                                  )))
                      .then(Commands.literal("getMaxStamina")
                                    .then(Commands.argument("entities", EntityArgument.entities())
                                                  .executes(source ->
                                                          executeGetMaxStamina(source.getSource(), EntityArgument.getEntities(source, "entities"))
                                                  )))
                      .then(Commands.literal("setMaxStamina")
                                    .then(Commands.argument("entities", EntityArgument.entities())
                                                  .then(Commands.argument("amount", IntegerArgumentType.integer(0, 2000))
                                                                .executes(source ->
                                                                        executeSetMaxStamina(
                                                                                source.getSource(), EntityArgument.getEntities(source, "entities"),
                                                                                IntegerArgumentType.getInteger(source, "amount"))
                                                                ))));
    }

    private int executeGetMaxStamina(CommandSourceStack source, Collection<? extends Entity> entities) {

        if (entities.stream().anyMatch(entity -> !(entity instanceof Player))) {
            source.sendFailure(Component.translatable("No sirve"));
            return 0;
        }
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                int maxStamina = FeathersAPI.getMaxStamina(player);
                source.sendSuccess(() -> Component.literal("Max Stamina is: " + maxStamina), true);
            }
        }
        return entities.size();

    }

    private int executeSetMaxStamina(CommandSourceStack source, Collection<? extends Entity> entities, int amount) {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player))) {
            source.sendFailure(Component.translatable("No sirve"));
            return 0;
        }
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                FeathersAPI.setMaxStamina(player, amount);
            }
        }

        //Compose & send message
        if (entities.size() == 1) {
            Entity target = entities.iterator().next();
            source.sendSuccess(() -> Component.translatable("Set the max stamina of %d to %d", target.getName().getString(), amount), true);
        } else {
            source.sendSuccess(() -> Component.translatable("Whatever", entities.size(), amount), true);
        }
        return entities.size();
    }

    private int executeGetStamina(CommandSourceStack source, Collection<? extends Entity> entities) {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player))) {
            source.sendFailure(Component.translatable("No sirve"));
            return 0;
        }
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                int stamina = FeathersAPI.getStamina(player);
                source.sendSuccess(() -> Component.literal("Stamina is: " + stamina), true);
            }
        }
        return entities.size();
    }

    private int executeSetStamina(CommandSourceStack source, Collection<? extends Entity> entities, boolean status, String type) {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player))) {
            source.sendFailure(Component.translatable("No sirve"));
            return 0;
        }
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                if (status) {
                    player.addEffect(new MobEffectInstance(FeathersEffects.COLD.get(), 1200, 0, false, true));
                } else {
                    if (player.hasEffect(FeathersEffects.COLD.get())) {
                        player.removeEffect(FeathersEffects.COLD.get());
                    }
                }
            }
        }

        //Compose & send message
        if (entities.size() == 1) {
            Entity target = entities.iterator().next();
            source.sendSuccess(() -> Component.translatable("Set the coldness effect to %d to %d", target.getName().getString(), status), true);
        } else {
            source.sendSuccess(() -> Component.translatable("Whatever", entities.size(), status), true);
        }
        return entities.size();
    }

    private int executeSetStamina(CommandSourceStack source, Collection<? extends Entity> entities, int amount) {
        if (entities.stream().anyMatch(entity -> !(entity instanceof Player))) {
            source.sendFailure(Component.translatable("No sirve"));
            return 0;
        }
        for (Entity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                FeathersAPI.setStamina(player, amount);
            }
        }

        if (entities.size() == 1) {
            Entity target = entities.iterator().next();
            source.sendSuccess(() -> Component.literal("Set the stamina of " + target.getName().getString() + " to " + amount), true);
        } else {
            source.sendSuccess(() -> Component.translatable("Whatever", entities.size(), amount), true);
        }
        return entities.size();
    }
}
