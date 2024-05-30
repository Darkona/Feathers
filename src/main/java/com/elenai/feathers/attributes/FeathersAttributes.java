package com.elenai.feathers.attributes;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.util.Calculations;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Feathers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FeathersAttributes {


    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Feathers.MODID);

    public static final RegistryObject<Attribute> MAX_STAMINA = registerAttribute(
            "feathers.max_stamina",
            (id) -> new RangedAttribute(id, 200.0D, 0.0D, 20000D).setSyncable(true),
            "d74ded8f-c5b6-4222-80e2-dbea7ccf8d02"
    );

    private static final double baseFeathersPerSecond = Calculations.calculateFeathersPerSecond(FeathersCommonConfig.baseRegen);
    private static final double minimumFeathersPerSecond = baseFeathersPerSecond * -20;
    private static final double maximumFeathersPerSecond = baseFeathersPerSecond * 20;

    public static final RegistryObject<Attribute> BASE_FEATHERS_PER_SECOND = registerAttribute("feathers.feathers_per_second",
            (id) -> new RangedAttribute(id, baseFeathersPerSecond, minimumFeathersPerSecond, maximumFeathersPerSecond).setSyncable(true),
            "d74ded8f-c5b6-4222-80e2-dbea7ccf8d02"
    );

    public static RegistryObject<Attribute> registerAttribute(String name, Function<String, Attribute> attribute, String uuid) {
        return ATTRIBUTES.register(name, () -> attribute.apply(name));
    }

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> e : event.getTypes()) {
            if (e == EntityType.PLAYER) {
                for (RegistryObject<Attribute> v : ATTRIBUTES.getEntries()) {
                    event.add(e, v.get());
                }
            }
        }
    }

}
