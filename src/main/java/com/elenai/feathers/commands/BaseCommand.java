package com.elenai.feathers.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@Getter

public class BaseCommand {
    protected LiteralArgumentBuilder<CommandSourceStack> builder;
    boolean enabled;

    public BaseCommand(String name, int permissionLevel, boolean enabled) {
        this.builder = Commands.literal(name).requires(source -> source.hasPermission(permissionLevel));
        this.enabled = enabled;
    }

    public LiteralArgumentBuilder<CommandSourceStack> setExecution() {
        return null;
    }
}
