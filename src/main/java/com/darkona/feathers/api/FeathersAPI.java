package com.darkona.feathers.api;

import com.darkona.feathers.attributes.FeathersAttributes;
import com.darkona.feathers.capability.Capabilities;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import com.darkona.feathers.enchantment.FeathersEnchantments;
import com.darkona.feathers.event.FeatherEvent;
import com.darkona.feathers.networking.FeathersMessages;
import com.darkona.feathers.networking.packet.FeatherSyncSTCPacket;
import com.darkona.feathers.util.Calculations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;

@SuppressWarnings({"UnusedReturnValue", "DataFlowIssue"})
public class FeathersAPI {

    public static int getFeathers(Player player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS).map(IFeathers::getFeathers)
                     .orElse(0);
    }

    public static int getAvailableFeathers(Player player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS).map(IFeathers::getAvailableFeathers)
                     .orElse(0);
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
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setStamina(amount * 10);
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
                  result.set(f.getFeathers());
              });
        return result.get();
    }

    public static void setMaxFeathers(Player player, int amount) {
        if (player.getAttributes().hasAttribute(FeathersAttributes.MAX_FEATHERS.get())) {
            player.getAttribute(FeathersAttributes.MAX_FEATHERS.get())
                  .setBaseValue(amount * FeathersConstants.STAMINA_PER_FEATHER);
        }
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  f.setMaxStamina((int) player.getAttribute(FeathersAttributes.MAX_FEATHERS.get()).getValue());
                  FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
              });
    }

    /**
     * Give feathers to the player. Returns the amount of feathers gained.
     * For tick-based operations, use staminaUsageModifiers.
     *
     * @param player the player
     * @param amount the amount of feathers to gain
     * @return true if the player has gained feathers, false otherwise
     */
    public static boolean gainFeathers(Player player, int amount) {
        var result = new AtomicBoolean(false);
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {
                  var gainEvent = new FeatherEvent.Gain(player, amount);
                  boolean cancelled = MinecraftForge.EVENT_BUS.post(gainEvent);
                  if (!cancelled && gainEvent.getResult() == DEFAULT) {
                      var prev = f.getFeathers();
                      var post = f.gainFeathers(gainEvent.amount);
                      MinecraftForge.EVENT_BUS.post(new FeatherEvent.Changed(player, prev, post));
                      FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
                      result.set(post);
                  }
              });
        return result.get();
    }

    /**
     * Spend feathers from the player. Returns the amount of feathers spent.
     *
     * @param player the player
     * @param amount the amount of feathers to spend
     * @return true if the player has enough feathers to spend, false otherwise
     * @throws UnsupportedOperationException if the amount is negative
     */
    public static boolean spendFeathers(Player player, int amount, int cooldownTicks) throws UnsupportedOperationException {
        if (amount < 0) {
            throw new UnsupportedOperationException("Cannot spend negative feathers");
        }
        var result = new AtomicBoolean(false);
        if (player.isCreative() || player.isSpectator()) return true;

        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> {

                  var useFeatherEvent = new FeatherEvent.Use(player, amount);
                  boolean cancelled = MinecraftForge.EVENT_BUS.post(useFeatherEvent);

                  if (!cancelled && useFeatherEvent.getResult() == DEFAULT) {

                      var prev = f.getFeathers();
                      var used = f.useFeathers(player, useFeatherEvent.amount, cooldownTicks);

                      MinecraftForge.EVENT_BUS.post(new FeatherEvent.Changed(player, prev, used));
                      result.set(used);

                      if (used) FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
                  }
              });
        return result.get();
    }

    public static int getPlayerWeight(Player player) {
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
            return Math.max(ArmorWeightAPI.getArmorWeight(armor) -
                    ArmorWeightAPI.getItemEnchantmentLevel(FeathersEnchantments.LIGHTWEIGHT.get(), itemStack) +
                    (ArmorWeightAPI.getItemEnchantmentLevel(FeathersEnchantments.HEAVY.get(), itemStack) * ArmorWeightAPI.getArmorWeight(armor)), 0);
        } else if (itemStack.getItem() == Items.AIR) {
            return 0;
        }
        return 0;
    }

    public static void setCooldown(ServerPlayer player, int cooldownTicks) {
        player.getCapability(Capabilities.PLAYER_FEATHERS)
              .ifPresent(f -> f.setCooldown(cooldownTicks));
    }

    public static int getCooldown(ServerPlayer player) {
        return player.getCapability(Capabilities.PLAYER_FEATHERS)
                     .map(IFeathers::getCooldown).orElse(0);
    }

    public static boolean isCold(Player player) {
        return player != null && player.hasEffect(FeathersEffects.COLD.get());
    }

    public static boolean isHot(Player player) {
        return player != null && player.hasEffect(FeathersEffects.HOT.get());
    }

    public static boolean isEnergized(Player player) {
        return player != null && player.hasEffect(FeathersEffects.ENERGIZED.get());
    }

    public static boolean isStrained(Player player) {
        return player != null && player.hasEffect(FeathersEffects.STRAINED.get());
    }

    public static boolean isEnduring(Player player) {
        return player != null && player.hasEffect(FeathersEffects.ENDURANCE.get());
    }

    public static boolean isFatigued(Player player) {
        return player != null && player.hasEffect(FeathersEffects.FATIGUE.get());
    }


    public static double getPlayerFeatherRegenerationPerSecond(Player player) {
        var regen = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
        return regen != null ? regen.getValue() : FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get();
    }

    public static int getPlayerStaminaRegenerationPerTick(Player player) {
        return Calculations.calculateStaminaPerTick(getPlayerFeatherRegenerationPerSecond(player));
    }

    public static int getPlayerMaxFeathers(Player player) {
        var maxFeathers = player.getAttribute(FeathersAttributes.MAX_FEATHERS.get());
        return (int) Math.ceil((maxFeathers != null ? maxFeathers.getValue() : FeathersCommonConfig.MAX_FEATHERS.get()));
    }

    public static double getPlayerStaminaUsageMultiplier(Player player) {
        var multiplier = player.getAttribute(FeathersAttributes.STAMINA_USAGE_MULTIPLIER.get());
        return multiplier != null ? multiplier.getValue() : 1.0D;
    }

    public static void setFeatherRegen(ServerPlayer player, double amount) {
        player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get()).setBaseValue(amount);
    }
}


