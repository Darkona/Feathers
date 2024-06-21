package com.darkona.feathers.attributes;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.config.FeathersCommonConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;


public class FeathersAttributes {

    public static final HashMap<RegistryObject<Attribute>, UUID> UUIDS = new HashMap<>();

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Feathers.MODID);

    public static final RegistryObject<Attribute> MAX_FEATHERS = registerAttribute("max_feathers",
            (id) -> new RangedAttribute(id, 20.0D, 0.0D, 40.0D).setSyncable(true),
            "eeff9a0d-bd90-418c-bb1e-2ef3446adfa3"
    );

    public static final RegistryObject<Attribute> MAX_STRAIN = registerAttribute("max_strain",
            (id) -> new RangedAttribute(id, 6.0D, 0.0D, 40.0D).setSyncable(true),
            "d74ded8f-c5b6-4222-80e2-dbea7ccf8d02"
    );

    public static final RegistryObject<Attribute> FEATHERS_PER_SECOND = registerAttribute("feathers_per_second",
            (id) -> new RangedAttribute(id, 0.4D, -40.0, 40.0D).setSyncable(true),

            "f39cfede-259c-45dd-b3fd-7307a8cc1255"
    );

    public static final RegistryObject<Attribute> STAMINA_USAGE_MULTIPLIER = registerAttribute("usage_multiplier",
            (id) -> new RangedAttribute(id, 1.0D, 0.0D, 40.0D).setSyncable(true),
            "d9caa995-9dc5-46e9-8559-6c4542ba89bc"
    );


    public static RegistryObject<Attribute> registerAttribute(String name, Function<String, Attribute> attribute, String uuid) {
        return registerAttribute(name, attribute, UUID.fromString(uuid));
    }

    public static RegistryObject<Attribute> registerAttribute(String name, Function<String, Attribute> attribute, UUID uuid) {
        RegistryObject<Attribute> registryObject = ATTRIBUTES.register(name, () -> attribute.apply(name));
        UUIDS.put(registryObject, uuid);
        return registryObject;
    }

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }


}
