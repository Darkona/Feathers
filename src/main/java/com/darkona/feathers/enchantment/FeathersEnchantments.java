package com.darkona.feathers.enchantment;

import com.darkona.feathers.Feathers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FeathersEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister
            .create(ForgeRegistries.ENCHANTMENTS, Feathers.MODID);

    public static RegistryObject<Enchantment> LIGHTWEIGHT = ENCHANTMENTS.register("lightweight",
            () -> new LightweightEnchantment(Rarity.UNCOMMON, EnchantmentCategory.ARMOR,
                    EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.LEGS));

    public static RegistryObject<Enchantment> HEAVY = ENCHANTMENTS.register("heavy",
            () -> new HeavyCurse(Rarity.RARE, EnchantmentCategory.ARMOR,
                    EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.LEGS));

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
