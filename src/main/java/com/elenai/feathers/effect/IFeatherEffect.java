package com.elenai.feathers.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.common.extensions.IForgeMobEffect;
import org.jetbrains.annotations.NotNull;

public interface IFeatherEffect extends IForgeMobEffect {
    void addAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength);

    void removeAttributeModifiers(@NotNull LivingEntity target, @NotNull AttributeMap map, int strength);
}
