package com.elenai.feathers.client;

import com.elenai.feathers.api.FeathersAPI;
import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.config.FeathersClientConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
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

    public static ClientFeathersData getInstance() {
        if (instance == null) {
            instance = new ClientFeathersData();
        }
        return instance;
    }

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
    private boolean hot = false;
    private boolean cold = false;
    private boolean energized = false;
    private boolean overflowing = false;
    private boolean momentum = false;
    private boolean endurance = false;
    private boolean fatigued = false;

    private ClientFeathersData(){}
    public void update(IFeathers f) {
        Player player = Minecraft.getInstance().player;
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
        enduranceFeathers = f.getEnduranceFeathers();
        hot = FeathersAPI.isHot(player);
        cold = FeathersAPI.isCold(player);
        energized = FeathersAPI.isEnergized(player);
        //overflowing = FeathersAPI.isOverflowing(player);
        //momentum = FeathersAPI.isMomentum(player);
        //endurance = FeathersAPI.isEndurance(player);
        fatigued = FeathersAPI.isFatigued(player);
        //weight = FeathersAPI.getWeight(player);

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
            if (feathers > previousFeathers &&
                    FeathersClientConfig.REGEN_EFFECT.get() &&
                    animationCooldown <= 0) {

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
}
