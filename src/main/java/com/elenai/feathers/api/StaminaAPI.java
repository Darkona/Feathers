package com.elenai.feathers.api;

import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class StaminaAPI {


    public static void addStamina(Player player, int amount) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void removeStamina(Player player, int amount) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void setMaxStamina(Player player, int maxStamina) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }


    public static void addMaxStamina(Player player, int amount) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void removeMaxStamina(Player player, int amount) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void addStaminaDeltaModifier(Player player, String name, IModifier modifier) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.addDeltaModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void removeStaminaDeltaModifier(Player player, IModifier modifier) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.removeDeltaModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void addStaminaUsageModifier(Player player, IModifier modifier) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.addUsageModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void removeStaminaUsageModifier(Player player, IModifier modifier) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.removeUsageModifier(modifier);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void setStaminaDelta(Player player, int delta) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStaminaDelta(delta);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static void setStamina(ServerPlayer player, int amount) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });

    }

    public static int getStamina(Player player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getStamina).orElse(0);
    }

    public static int getMaxStamina(Player player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getMaxStamina).orElse(0);
    }

    public static void setMaxStamina(ServerPlayer player, int maxStamina) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    public static int getStaminaDelta(Player player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getStaminaDelta).orElse(0);
    }
}
