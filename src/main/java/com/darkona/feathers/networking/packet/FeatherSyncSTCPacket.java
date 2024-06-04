package com.darkona.feathers.networking.packet;

import com.darkona.feathers.api.IFeathers;
import com.darkona.feathers.capability.Capabilities;
import com.darkona.feathers.client.ClientFeathersData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class FeatherSyncSTCPacket {

    private final int stamina;
    private final int maxStamina;
    private final int feathers;
    private final int maxFeathers;
    private final int staminaDelta;
    private final int cooldown;
    private final int counterAmount;
    private final int weight;
    private final Map<String, Double> counters;

    public FeatherSyncSTCPacket(IFeathers f) {
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

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        counters = new HashMap<>();

        stamina = buf.readInt();
        maxStamina = buf.readInt();
        feathers = buf.readInt();
        maxFeathers = buf.readInt();
        staminaDelta = buf.readInt();

        cooldown = buf.readInt();
        weight = buf.readInt();


        counterAmount = buf.readInt();
        for (int i = 0; i < counterAmount; i++) {
            var k = buf.readUtf(512);
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

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        return context.enqueueWork(() -> {
            Player clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer != null) {
                clientPlayer.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                    f.setStamina(stamina);
                    f.setMaxStamina(maxStamina);
                    f.setFeathers(feathers);
                    f.setStaminaDelta(staminaDelta);
                    f.setCooldown(cooldown);
                    f.setWeight(weight);
                    counters.forEach(f::setCounter);
                    ClientFeathersData.getInstance().update(clientPlayer, f);
                });
            }
        }).isDone();
    }
}
