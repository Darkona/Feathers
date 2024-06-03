package com.elenai.feathers.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Map;
import java.util.Optional;

@AutoRegisterCapability
public interface IFeathers {


    int getStamina();

    void setStamina(int stamina);

    int getFeathers();

    void setFeathers(int feathers);

    void addDeltaModifier(IModifier modifier);

    void removeDeltaModifier(IModifier modifier);

    void recalculateStaminaDelta(Player player);

    void addUsageModifier(IModifier modifier);

    void removeUsageModifier(IModifier modifier);

    int getMaxStamina();

    void setMaxStamina(int maxStamina);

    boolean gainFeathers(int feathers);

    /**
     * Spend feathers from the player.
     *
     * @param player   the player
     * @param feathers the amount of feathers to spend
     * @return true if the player has enough feathers to spend, false otherwise
     */
    boolean useFeathers(Player player, int feathers);

    int getCooldown();

    void setCooldown(int cooldown);

    int getStrainFeathers();

    void setStrainFeathers(int strainFeathers);

    int getMaxStrained();

    void setMaxStrained(int maxStrained);

    int getStaminaDelta();

    void setStaminaDelta(int staminaDelta);

    boolean isDirty();

    Map<String, IModifier> getStaminaDeltaModifiers();

    Map<String, IModifier> getStaminaUsageModifiers();

    void removeCounter(String name);

    Optional<Integer> getCounter(String name);

    void setCounter(String name, int value);

    void markDirty();

    int getMaxFeathers();

    void copyFrom(IFeathers oldStore);

    void tick(Player player);

    CompoundTag saveNBTData();

    void loadNBTData(CompoundTag nbt);

    boolean hasCounter(String name);

    Map<String, Integer> getCounters();

}
