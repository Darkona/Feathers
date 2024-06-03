package com.elenai.feathers.util;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;

import static com.elenai.feathers.api.FeathersAPI.getArmorWeightByStack;

public class ArmorHandler {
    private static final HashMap<String, Integer> map = new HashMap<>();

    public static HashMap<String, Integer> getWeights() {
        if (map.isEmpty()) {
            populateWeights();
        }
        return map;
    }

    public static void populateWeights() {
        map.clear();
        FeathersCommonConfig.ARMOR_WEIGHTS.get().forEach(value -> {
            String[] split = value.split(":");
            try {
                map.putIfAbsent(split[0], Integer.parseInt(split[1]));
            } catch (Exception e) {
                Feathers.logger.warn(e + " error! Armor value not set as an integer.");
            }
        });
    }

    public static int getArmorWeight(ArmorItem armor) {
        return getWeights().getOrDefault(armor.getDescriptionId(), armor.getDefense());
    }

    /**
     * Gets the total weight of the inputted player based on the armor they are wearing
     *
     * @param player
     * @return
     */
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

    /**
     * Returns the cumulative total of an equipped enchantment type.
     *
     * @param enchantment
     * @param entity
     * @return
     * @author Diesieben07
     */
    public static int getTotalEnchantmentLevel(Enchantment enchantment, LivingEntity entity) {
        Iterable<ItemStack> iterable = enchantment.getSlotItems(entity).values();
        int i = 0;
        for (ItemStack itemstack : iterable) {
            int j = itemstack.getEnchantmentLevel(enchantment);
            i += j;
        }
        return i;
    }

    /**
     * Returns the total of an item enchantment type.
     *
     * @param enchantment
     * @param entity
     * @return
     * @author Elenai
     */
    public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemstack) {
        return itemstack.getEnchantmentLevel(enchantment);
    }
}
