package com.elenai.feathers.attributes;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Feathers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FeathersAttributes {

    public static final HashMap<RegistryObject<Attribute>, UUID> UUIDS = new HashMap<>();
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Feathers.MODID);


    public static final RegistryObject<Attribute> MAX_FEATHERS = registerAttribute("feathers.max_feathers",
        (id) -> new RangedAttribute(id, 20.0D, 0.0D, 40.0D).setSyncable(true),
            "d74ded8f-c5b6-4222-80e2-dbea7ccf8d02"
    );

    public static final RegistryObject<Attribute> FEATHERS_PER_SECOND = registerAttribute("feathers.feathers_per_second",
        (id) -> new RangedAttribute(id,0.4D,-40.0,40.0D).setSyncable(true),
        "d74ded8f-c5b6-4222-80e2-dbea7ccf8d02"
    );

    public static RegistryObject<Attribute> registerAttribute(
        String name, Function<String, Attribute> attribute, String uuid
    ) {
        return registerAttribute(name, attribute, UUID.fromString(uuid));
    }

    public static RegistryObject<Attribute> registerAttribute(
        String name, Function<String, Attribute> attribute, UUID uuid
    ) {
        RegistryObject<Attribute> registryObject = ATTRIBUTES.register(name,
            () -> attribute.apply(name)
        );
        UUIDS.put(registryObject, uuid);
        return registryObject;
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

    @SubscribeEvent
    public static void onConfig(ModConfigEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        server.getPlayerList().getPlayers().forEach(PlayerFeathersProvider::assignRegenerationAttribute);
        server.getPlayerList().getPlayers().forEach(PlayerFeathersProvider::assignMaxFeathersAttribute);

    }
}
