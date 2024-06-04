package com.darkona.feathers.api;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.config.FeathersCommonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;

public class ArmorWeightAPI {
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

    public static int getTotalEnchantmentLevel(Enchantment enchantment, LivingEntity entity) {
        Iterable<ItemStack> iterable = enchantment.getSlotItems(entity).values();
        int i = 0;
        for (ItemStack itemstack : iterable) {
            int j = itemstack.getEnchantmentLevel(enchantment);
            i += j;
        }
        return i;
    }

    public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemstack) {
        return itemstack.getEnchantmentLevel(enchantment);
    }
}
