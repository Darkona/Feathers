package com.darkona.feathers.networking.packet;

import com.darkona.feathers.api.IFeathers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FeatherSTCSyncPacket {

    public final int stamina;
    public final int maxStamina;
    public final int feathers;
    public final int maxFeathers;
    public final int staminaDelta;
    public final int cooldown;
    public final int counterAmount;
    public final int weight;
    public final Map<String, Double> counters;

    public FeatherSTCSyncPacket(IFeathers f) {
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
        counterAmount = f.getCounters().size();
        counters = f.getCounters();
        cooldown = f.getCooldown();
        weight = f.getWeight();
    }

    public FeatherSTCSyncPacket(FriendlyByteBuf buf) {
        stamina = buf.readInt();
        maxStamina = buf.readInt();
        feathers = buf.readInt();
        maxFeathers = buf.readInt();
        staminaDelta = buf.readInt();
        cooldown = buf.readInt();
        weight = buf.readInt();

        counters = new HashMap<>();
        counterAmount = buf.readInt();
        for (int i = 0; i < counterAmount; i++) {
            var k = buf.readUtf();
            var v = buf.readDouble();
            counters.put(k, v);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stamina);
        buf.writeInt(maxStamina);
        buf.writeInt(feathers);
        buf.writeInt(maxFeathers);
        buf.writeInt(staminaDelta);
        buf.writeInt(cooldown);
        buf.writeInt(weight);

        buf.writeInt(counterAmount);
        counters.forEach((k, v) -> {
            buf.writeUtf(k);
            buf.writeDouble(v);
        });
    }

    public static void handle(FeatherSTCSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FeatherSTCSyncPacket.handle(message, contextSupplier)));
        }
    }
}
