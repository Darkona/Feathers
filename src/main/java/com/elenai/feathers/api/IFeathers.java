package com.elenai.feathers.api;

import com.elenai.feathers.capability.PlayerFeathers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

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

    int gainFeathers(int feathers);

    /**
     * Spend feathers from the player. Returns the amount of feathers spent.
     *
     * @param player the player
     * @param feathers the amount of feathers to spend
     * @return the amount of feathers spent
     */
    int useFeathers(Player player, int feathers);

    int getCooldown();

    void setCooldown(int cooldown);

    int getEnduranceFeathers();

    void setEnduranceFeathers(int enduranceFeathers);

    int getStrainFeathers();

    void setStrainFeathers(int strainFeathers);

    int getMaxStrained();

    void setMaxStrained(int maxStrained);

    int getStaminaDelta();

    boolean shouldRecalculate();

    boolean isCold();

    void setCold(boolean cold);

    boolean isHot();

    void setHot(boolean hot);

    boolean isEnergized();

    void setEnergized(boolean energized);

    int getEnergizedStrength();

    void setEnergizedStrength(int energizedStrength);

    boolean isFatigued();

    void setFatigued(boolean fatigued);

    boolean isStrained();

    void setStrained(boolean strain);

    boolean hasMomentum();

    java.util.Map<String, IModifier> getStaminaDeltaModifiers();

    java.util.Map<String, IModifier> getStaminaUsageModifiers();

    void setShouldRecalculate(boolean shouldRecalculate);

    void setMomentum(boolean momentum);

    int getMaxFeathers();

    void copyFrom(IFeathers oldStore);

    void  tick(Player player);

    CompoundTag saveNBTData();

    void loadNBTData(CompoundTag nbt);

    void setStaminaDelta(int staminaDelta);
}
