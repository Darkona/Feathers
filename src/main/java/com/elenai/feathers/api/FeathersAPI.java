package com.elenai.feathers.api;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.elenai.feathers.util.ArmorHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Function;

public class FeathersAPI {


    public static int getFeathers(Player player){
        var feathers = (double) (getStamina(player) / FeathersConstants.STAMINA_PER_FEATHER);
        return  (int)feathers;
    }

    public static int getMaxFeathers(Player player){
        var feathers = (double) (getMaxStamina(player) / FeathersConstants.STAMINA_PER_FEATHER);
        return  (int)feathers;
    }

    public static void setFeathers(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount * FeathersConstants.STAMINA_PER_FEATHER);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void setMaxFeathers(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(amount * FeathersConstants.STAMINA_PER_FEATHER);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void addFeathers(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() + (amount * FeathersConstants.STAMINA_PER_FEATHER));
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void removeFeathers(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() - (amount * FeathersConstants.STAMINA_PER_FEATHER));
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void addStamina(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void removeStamina(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(f.getStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void setMaxStamina(Player player, int maxStamina){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }


    public static void addMaxStamina(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() + amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void removeMaxStamina(Player player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(f.getMaxStamina() - amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void addStaminaDeltaModifier(Player player,String name,  Function<Integer, Integer> modifier){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.addDeltaModifier(name, modifier);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static void removeStaminaDeltaModifier(Player player, String name){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.removeDeltaModifier(name);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    public static int getStamina(Player player){
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getStamina).orElse(0);
    }

    public static int getMaxStamina(Player player){
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getMaxStamina).orElse(0);
    }

    public static void setMaxStamina(ServerPlayer player, int maxStamina){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina(maxStamina);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }
    public static int getStaminaDelta(Player player){
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getStaminaDelta).orElse(0);
    }

    public static void setStamina(ServerPlayer player, int amount){
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });

    }

    public static int getPlayerWeight(ServerPlayer player) {
        if(!FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get()) {
            return 0;
        }
        int weight = 0;
        for (ItemStack i : player.getArmorSlots()) {
            weight += getArmorWeightByStack(i);
        }
        return weight;
    }

    public static int getArmorWeightByStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem armor) {
            return Math.max(ArmorHandler.getArmorWeight(armor) -
                    ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.LIGHTWEIGHT.get(), itemStack) +
                    (ArmorHandler.getItemEnchantmentLevel(FeathersEnchantments.HEAVY.get(), itemStack) * ArmorHandler.getArmorWeight(armor)), 0);
        } else if (itemStack.getItem() == Items.AIR) {
            return 0;
        }
        Feathers.logger.warn("Attempted to calculate weight of non armor item: " + itemStack.getDescriptionId());
        return 0;
    }

    public static void setFeathers(ServerPlayer player, int amount) {
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount * 10);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }


}
