package com.elenai.feathers.client;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.FeathersEffects;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

@Setter
@Getter
public class ClientFeathersData {

    public static int stamina = 2000;
    @Setter
    public static int feathers = 0;
    public static int maxStamina = 2000;
    public static int maxFeathers = 0;
    public static int staminaDelta = 0;
    public static int previousFeathers = 0;
    public static int enduranceFeathers = 0;
    public static int weight = 0;
    public static int animationCooldown = 0;
    public static int fadeCooldown = 0;
    public static boolean hot = false;
    public static boolean energized = false;
    public static boolean overflowing = false;
    public static boolean momentum = false;
    public static boolean endurance = false;
    public static boolean fatigued = false;

    public static int getFeathers() {
        return stamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public static int getMaxFeathers() {
        return maxStamina / FeathersConstants.STAMINA_PER_FEATHER;
    }

    public static boolean isCold() {
        var player = Minecraft.getInstance().player;
        return player != null && player.hasEffect(FeathersEffects.COLD.get());
    }
}
