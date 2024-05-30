package com.elenai.feathers.api;

import net.minecraft.world.entity.player.Player;

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

    void setMaxStamina(int maxStamina);
    int getMaxStamina();


    int gainFeathers(int feathers);

    int useFeathers(Player player, int feathers);





    int getMaxFeathers();

    int getCooldown();

    int getEnduranceFeathers();

    int getStrainFeathers();

    int getMaxStrained();

    int getStaminaDelta();

    boolean shouldRecalculate();

    boolean isCold();

    boolean isHot();

    boolean isEnergized();

    int getEnergizedStrength();

    boolean isFatigued();

    boolean isStrained();
    boolean hasMomentum();

    java.util.Map<String, IModifier> getStaminaDeltaModifiers();

    java.util.Map<String, IModifier> getStaminaUsageModifiers();



    void setMaxFeathers(int maxFeathers);

    void setCooldown(int cooldown);

    void setEnduranceFeathers(int enduranceFeathers);

    void setStrainFeathers(int strainFeathers);

    void setMaxStrained(int maxStrained);

    void setShouldRecalculate(boolean shouldRecalculate);

    void setCold(boolean cold);

    void setHot(boolean hot);

    void setEnergized(boolean energized);

    void setEnergizedStrength(int energizedStrength);

    void setFatigued(boolean fatigued);

    void setMomentum(boolean momentum);

    void setStrained(boolean strain);
}
