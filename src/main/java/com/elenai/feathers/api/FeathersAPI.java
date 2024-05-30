package com.elenai.feathers.api;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.attributes.FeathersAttributes;
import com.elenai.feathers.capability.Modifiers;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.elenai.feathers.event.FeatherEvent;
import com.elenai.feathers.event.StaminaChangeEvent;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.elenai.feathers.util.ArmorHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;

import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;

@SuppressWarnings("UnusedReturnValue")
public class FeathersAPI {

    public static int getFeathers(Player player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getFeathers).orElse(0);
    }

    /**
     * Sets the player's feathers to the specified amount. For tick-based operations, use staminaDeltaModifiers;
     *
     * @param player the player
     * @param amount the amount of feathers to set
     * @return the amount of feathers set
     */
    public static int setFeathers(ServerPlayer player, int amount) {
        AtomicInteger result = new AtomicInteger(0);
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount * 10);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
                  result.set(f.getFeathers());
              });
        return result.get();
    }

    public static int getMaxFeathers(Player player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getMaxFeathers).orElse(0);
    }

    public static void setMaxFeathers(Player player, int amount) {
        if (player.getAttributes().hasAttribute(FeathersAttributes.MAX_STAMINA.get())) {
            player.getAttribute(FeathersAttributes.MAX_STAMINA.get())
                  .setBaseValue(amount * FeathersConstants.STAMINA_PER_FEATHER);
        }
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina((int) player.getAttribute(FeathersAttributes.MAX_STAMINA.get()).getValue());
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
              });
    }

    /**
     * Give feathers to the player. Returns the amount of feathers gained.
     * For tick-based operations, use staminaUsageModifiers.
     * @param player the player
     * @param amount the amount of feathers to gain
     * @return the amount of feathers gained
     */
    public static int gainFeathers(Player player, int amount) {
        AtomicInteger result = new AtomicInteger(0);
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  var gainEvent = new FeatherEvent.Gain(player, amount);
                  boolean cancelled = MinecraftForge.EVENT_BUS.post(gainEvent);
                  if (!cancelled && gainEvent.getResult() == DEFAULT) {
                      var prev = f.getFeathers();
                      var post = f.gainFeathers(gainEvent.amount);
                      MinecraftForge.EVENT_BUS.post(new FeatherEvent.Changed(player, prev, post));
                      FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), (ServerPlayer) player);
                      result.set(prev - post);
                  }
              });
        return result.get();
    }

    /**
     * Spend feathers from the player. Returns the amount of feathers spent.
     *
     * @param player the player
     * @param amount the amount of feathers to spend
     * @return the amount of feathers spent
     * @throws UnsupportedOperationException if the amount is negative
     */
    public static int spendFeathers(ServerPlayer player, int amount, int cooldownTicks) throws UnsupportedOperationException {
        if (amount < 0) {
            throw new UnsupportedOperationException("Cannot spend negative feathers");
        }
        AtomicInteger result = new AtomicInteger(0);
        if (player.isCreative() || player.isSpectator())
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                  .ifPresent(f -> {
                      var useFeatherEvent = new FeatherEvent.Use(player, amount);
                      boolean cancelled = MinecraftForge.EVENT_BUS.post(useFeatherEvent);
                      if (!cancelled && useFeatherEvent.getResult() == DEFAULT) {
                          var prev = f.getFeathers();
                          var post = f.useFeathers(player, useFeatherEvent.amount);
                          MinecraftForge.EVENT_BUS.post(new FeatherEvent.Changed(player, prev, post));
                          result.set(post - prev);
                          if (prev != f.getFeathers())
                              FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
                          if(cooldownTicks > 0) f.setCooldown(cooldownTicks);
                      } else {
                          result.set(useFeatherEvent.amount);
                      }

                  });
        return result.get();
    }




    public static int getPlayerWeight(ServerPlayer player) {
        if (!FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get()) {
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


}
