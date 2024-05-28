package com.elenai.feathers.api;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.PlayerFeathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.enchantment.FeathersEnchantments;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.FeatherSyncCTSPacket;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import com.elenai.feathers.util.ArmorHandler;
import com.elenai.feathers.util.Calculations;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FeathersHelper {

    /*

        /**
         * Sets the inputted players feathers and syncs them to the client
         *
         * @side server
         * @param player Player to set feathers for
         * @param feathers Amount of feathers to set
         */
    @Deprecated
    public static void setFeathers(ServerPlayer player, int feathers) {
        FeathersAPI.setFeathers(player, feathers);
    }

    /**
     * Sets the inputted player's max feathers and syncs them to the client
     *
     * @param player   Player to set max feathers for
     * @param feathers Amount of feathers to set
     * @side server
     */
    @Deprecated
    public static void setMaxFeathers(ServerPlayer player, int feathers) {
        FeathersAPI.setMaxFeathers(player, feathers);
    }

    /**
     * Sets the inputted player's feather regeneration rate and syncs them to the client
     *
     * @param player         Player to set max feathers for
     * @param tickPerFeather Amount of stamina per tick for regeneration
     * @side server
     */
    @Deprecated
    public static void setFeatherRegen(ServerPlayer player, int tickPerFeather) {
        FeathersAPI.setStaminaDelta(player, Calculations.calculateStaminaRegenPerSecondFromTicksPerFeather(tickPerFeather));
    }

    /**
     * Returns the given player's feather count
     *
     * @param player Player from which the feather value is being acquired
     * @return the player's feathers
     * @side server
     */
    public static int getFeathers(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getFeathers).orElse(0);
    }


    public static int getUseableFeathers(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS)
                     .map(PlayerFeathers::getStamina).orElse(0);
    }

    /**
     * Returns the given player's feather count
     *
     * @param player Player from which the feather value is being acquired
     * @return the player's feathers
     * @side server
     */
    public static int getMaxFeathers(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).map(PlayerFeathers::getMaxFeathers).orElse(0);
    }

    /**
     * Returns the client player's feather count
     *
     * @return the player's feathers
     * @side client
     */
    public static int getFeathers() {
        return ClientFeathersData.getFeathers();
    }

    /**
     * Returns the client player's max feather count
     *
     * @return the player's feathers
     * @side client
     */
    public static int getMaxFeathers() {
        return ClientFeathersData.getMaxFeathers();
    }

    /**
     * Returns the given player's endurance count
     *
     * @param player Player whose endurance is being acquired
     * @return the player's feathers
     * @side server
     */
    public static int getEndurance(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).map(PlayerFeathers::getEnduranceStamina)
                     .orElse(0) / FeathersConstants.STAMINA_PER_FEATHER;
    }

    /**
     * Returns the client player's endurance count
     *
     * @return the player's feathers
     * @side client
     */
    public static int getEndurance() {
        return ClientFeathersData.enduranceFeathers / FeathersConstants.STAMINA_PER_FEATHER;
    }

    /**
     * Adds the inputted players feathers to their total and syncs them to the client
     *
     * @param player
     * @param feathers
     * @side server
     */
    public static void addFeathers(ServerPlayer player, int feathers) {
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
            f.gainFeathers(feathers);

        });
    }

    /**
     * Decreases the inputted players feathers from their total and syncs them to the
     * client
     * <p>
     * NOTE: This differs from spendFeathers as it does not take armor weight into
     * account and is therefore not recommended, Only use this if you want to drain armor too
     *
     * @param player
     * @param feathers
     * @side server
     */
    public static void subFeathers(ServerPlayer player, int feathers) {
        player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
            f.useFeathers(feathers);

        });
    }

    /**
     * Decreases the inputted players feathers + endurance from their total and syncs them to the
     * client IF the final result is greater than the armor weight, returns whether
     * it is possible to or not
     * <p>
     * TIP: Use this method at the end of if statements when you wish to spend feathers
     * <p>
     *
     * @param player
     * @param feathers
     * @return If the effect was applied
     * @side server
     */
    public static boolean spendFeathers(ServerPlayer player, int feathers) {

        if (player.isCreative() || player.isSpectator()) {return true;}

        if (Math.min(getPlayerWeight(player), 20) <= (getFeathers(player) + getEndurance(player) - feathers)) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {

                int amount = f.getEnduranceStamina() - feathers;
                if (f.getEnduranceStamina() > 0) {
                    f.setEnduranceStamina(Math.max(0, amount));
                }
                if (amount < 0) {
                    f.gainFeathers(amount);
                }

                FeathersMessages.sendToPlayer(new FeatherSyncSTCPacket(f), player);
            });
            return true;
        }
        return false;
    }

    /**
     * Decreases the inputted players amount + endurance from their total and syncs them to the
     * server IF the final result is greater than the armor weight, returns whether
     * it is possible to or not
     * <p>
     * TIP: Use this method at the end of if statements when you wish to spend amount
     * <p>
     *
     * @param amount
     * @return If the effect was applied
     * @side client
     */
    @Deprecated
    public static boolean spendFeathers(int amount) {


        Player player = Minecraft.getInstance().player;
        assert player != null;
        if (player.isCreative() || player.isSpectator()) { return true; }

        if (ClientFeathersData.getFeathers() >= amount) {
            ClientFeathersData.setFeathers(ClientFeathersData.getFeathers() - amount);
            FeathersMessages.sendToServer(new FeatherSyncCTSPacket(ClientFeathersData.getFeathers(), 0));
            return true;
        }
        return false;
    }

    /**
     * Gets the weight of the given armor item, minus the input lightweight level, if the item has a weight in
     * the config, returns that value, if not it returns the item's defence rating
     * <p>
     * This method is for use when sending items as packets to the server
     * </p>
     *
     * @param item The armor who's weight you wish to get
     * @return the armor's weight
     * @side server
     */
    public static int getArmorWeight(Item item, int lightweightLevel, int heavyLevel) {
        if (item instanceof ArmorItem armor) {
            return Math.max(ArmorHandler.getArmorWeight(armor) - lightweightLevel + (heavyLevel * ArmorHandler.getArmorWeight(armor)), 0);
        } else if (item == Items.AIR) {
            return 0;
        }
        return 0;
    }

    /**
     * Gets the weight of the given armor item stack, if the item has a weight in
     * the config, returns that value, if not it returns the item's defence rating
     *
     * @param itemStack The armor who's weight you wish to get
     * @return the armor's weight
     * @side server
     */
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

    /**
     * Gets the total weight of the inputted player based on the armor they are wearing
     *
     * @param player
     * @return
     */
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

    /**
     * Returns the given player's coldness
     *
     * @param player
     * @return if the player is cold
     * @side server
     */
    public static boolean getCold(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).map(PlayerFeathers::isCold).orElse(false);
    }

    /**
     * Returns the given player's hotness
     *
     * @param player
     * @return if the player is hot
     * @side server
     */
    public static boolean getHot(ServerPlayer player) {
        return player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).map(PlayerFeathers::isHot).orElse(false);
    }


}
