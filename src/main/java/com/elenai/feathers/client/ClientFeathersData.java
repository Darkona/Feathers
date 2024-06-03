package com.elenai.feathers.client;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.config.FeathersClientConfig;
import com.elenai.feathers.effect.effects.EnduranceEffect;
import com.elenai.feathers.effect.effects.StrainEffect;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Setter
@Getter
@OnlyIn(Dist.CLIENT)
public class ClientFeathersData {

    //Use singleton for sanity
    private static ClientFeathersData instance;
    private int stamina = 2000;
    private int feathers = 0;
    private int maxStamina = 2000;
    private int maxFeathers = 0;
    private int staminaDelta = 0;
    private int previousFeathers = 0;
    private int enduranceFeathers = 0;
    private int weight = 0;
    private int animationCooldown = 0;
    private int fadeCooldown = 0;
    private int strainFeathers = 0;
    private boolean hot = false;
    private boolean cold = false;
    private boolean energized = false;
    private boolean overflowing = false;
    private boolean momentum = false;
    private boolean fatigued = false;
    private boolean endurance = false;
    private boolean strained = false;
    private ClientFeathersData() {}

    public static ClientFeathersData getInstance() {
        if (instance == null) {
            instance = new ClientFeathersData();
        }
        return instance;
    }

    public void update(Player player, IFeathers f) {
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
        enduranceFeathers = f.getCounter(EnduranceEffect.ENDURANCE_COUNTER).orElse(0);
        strainFeathers = f.getCounter(StrainEffect.STRAIN_COUNTER).orElse(0);
        endurance = FeathersAPI.isEnduring(player);
        hot = FeathersAPI.isHot(player);
        cold = FeathersAPI.isCold(player);
        energized = FeathersAPI.isEnergized(player);
        fatigued = FeathersAPI.isFatigued(player);
        strained = FeathersAPI.isStrained(player);
    }

    public boolean hasFullStamina() {
        return stamina >= maxStamina;
    }

    public boolean hasFeathers() {
        return feathers > 0;
    }

    public boolean hasFullFeathers() {
        return feathers >= maxFeathers;
    }

    public boolean hasWeight() {
        return false;
    }

    public boolean isOverflowing() {
        return feathers > maxFeathers;
    }

    public void tick() {

        if (animationCooldown > 0) animationCooldown--;

        if (feathers != previousFeathers) {
            if (feathers > previousFeathers && FeathersClientConfig.REGEN_EFFECT.get() && animationCooldown <= 0) {
                animationCooldown = 18;
            }
            previousFeathers = feathers;
        }

        if (FeathersClientConfig.FADE_WHEN_FULL.get()) {
            int cooldown = fadeCooldown;
            if (feathers == getMaxFeathers() || enduranceFeathers > 0) {
                fadeCooldown = cooldown < FeathersClientConfig.FADE_COOLDOWN.get() ? fadeCooldown + 1 : 0;
            }
        }
    }

    public int getStrainFeathers() {
        return strainFeathers > 0 ? strainFeathers / FeathersConstants.STAMINA_PER_FEATHER : 0;
    }

}
