package com.darkona.feathers.api;

import com.darkona.feathers.capability.FeathersCapabilities;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.effects.StrainEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.units.qual.C;

public class StaminaAPI {


    public static int getStrainedStamina(Player player) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                     .map(f -> (int) Math.round(f.getCounter(StrainEffect.STRAIN_COUNTER))).orElse(0);
    }

    public static int getAvailableStamina(Player player) {
        return FeathersAPI.getAvailableFeathers(player) * Constants.STAMINA_PER_FEATHER;
    }

    public static boolean canUseStamina(Player player, int amount) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .map(f -> {
                  int toUse = f.getStamina() - amount - f.getWeight() * Constants.STAMINA_PER_FEATHER ;
                  if (toUse >= 0) {
                      return true;
                  } else if (FeathersCommonConfig.ENABLE_STRAIN.get() && f.hasCounter(StrainEffect.STRAIN_COUNTER)) {
                      double strain = f.getCounter(StrainEffect.STRAIN_COUNTER);
                      return (strain - toUse) <= FeathersCommonConfig.MAX_STRAIN.get() * Constants.STAMINA_PER_FEATHER;
                  }
                  return false;
              }).orElse(false);

    }

    public static boolean useStamina(Player player, int amount) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                 .map(f -> {
                     if (canUseStamina(player, amount)) {
                         int toUse = f.getStamina() - amount;
                         f.setStamina(toUse);
                         if (toUse < 0 && FeathersCommonConfig.ENABLE_STRAIN.get() &&  f.hasCounter(StrainEffect.STRAIN_COUNTER))
                             f.incrementCounterBy(StrainEffect.STRAIN_COUNTER, -toUse);
                         return true;
                     }
                     return false;
                 }).orElse(false);

    }

    public static void setStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setStamina(amount));
    }

    public static void addStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setStamina(f.getStamina() + amount));
    }

    public static void removeStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setStamina(f.getStamina() - amount));
    }

    public static void setMaxStamina(Player player, int maxStamina) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setMaxStamina(maxStamina));
    }


    public static void addMaxStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setMaxStamina(f.getMaxStamina() + amount));
    }

    public static void removeMaxStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setMaxStamina(f.getMaxStamina() - amount));
    }

    public static void setStamina(ServerPlayer player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setStamina(amount));

    }

    public static int getStamina(Player player) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getStamina).orElse(0);
    }

    public static int getMaxStamina(Player player) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getMaxStamina).orElse(0);
    }

    public static void setMaxStamina(ServerPlayer player, int maxStamina) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setMaxStamina(maxStamina));
    }

    public static int getStaminaDelta(Player player) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getStaminaDelta).orElse(0);
    }
}
