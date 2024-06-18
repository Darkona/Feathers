package com.darkona.feathers.api;

import com.darkona.feathers.capability.FeathersCapabilities;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.effects.StrainEffect;
import com.darkona.feathers.networking.FeathersMessages;
import com.darkona.feathers.networking.packet.FeatherSTCSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class StaminaAPI {

    public static int getAvailableStamina(Player player) {
        return FeathersCommonConfig.MAX_STRAIN.get() - getStrainedStamina(player) + getStamina(player);
    }

    public static int getStrainedStamina(Player player){
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .map(f -> (int)Math.round(f.getCounter(StrainEffect.STRAIN_COUNTER))).orElse(0);
    }


    public static boolean canSpendStamina(Player player, int amount) {
        return getStamina(player) >= amount;
    }

    public static boolean useStamina(Player player, int amount) {
        if (getStamina(player) >= amount) {
            removeStamina(player, amount);
            return true;
        }
        return false;
    }

    public static void setStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void addStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void removeStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void setMaxStamina(Player player, int maxStamina) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }


    public static void addMaxStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void removeMaxStamina(Player player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void addStaminaDeltaModifier(Player player, String name, IModifier modifier) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.addDeltaModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void removeStaminaDeltaModifier(Player player, IModifier modifier) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.removeDeltaModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void addStaminaUsageModifier(Player player, IModifier modifier) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.addUsageModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void removeStaminaUsageModifier(Player player, IModifier modifier) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.removeUsageModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void setStaminaDelta(Player player, int delta) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStaminaDelta(delta);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static void setStamina(ServerPlayer player, int amount) {
        player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });

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
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSTCSyncPacket(f), player);
              });
    }

    public static int getStaminaDelta(Player player) {
        return player.getCapability(FeathersCapabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getStaminaDelta).orElse(0);
    }
}
