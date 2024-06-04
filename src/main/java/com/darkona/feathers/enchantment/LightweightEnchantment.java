package com.darkona.feathers.enchantment;

import com.darkona.feathers.config.CommonConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class LightweightEnchantment extends Enchantment {

    protected LightweightEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slot) {
        super(rarity, category, slot);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return !CommonConfig.ENABLE_LIGHTWEIGHT_ENCHANTMENT.get();
    }
}
