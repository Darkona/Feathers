package com.elenai.feathers.networking.packet;

import com.elenai.feathers.api.IFeathers;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.client.ClientFeathersData;
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

    private final int counterAmount;

    private final Map<String, Integer> counters;

    public FeatherSyncSTCPacket(IFeathers f) {
        stamina = f.getStamina();
        maxStamina = f.getMaxStamina();
        feathers = f.getFeathers();
        maxFeathers = f.getMaxFeathers();
        staminaDelta = f.getStaminaDelta();
        counterAmount = f.getCounters().size();
        counters = f.getCounters();

    }

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        stamina = buf.readInt();
        maxStamina = buf.readInt();
        feathers = buf.readInt();
        maxFeathers = buf.readInt();
        staminaDelta = buf.readInt();
        counterAmount = buf.readInt();
        counters = new HashMap<>();
        for (int i = 0; i < counterAmount; i++) {
            var k = buf.readUtf(512);
            var v = buf.readInt();
            counters.put(k, v);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stamina);
        buf.writeInt(maxStamina);
        buf.writeInt(feathers);
        buf.writeInt(maxFeathers);
        buf.writeInt(staminaDelta);
        buf.writeInt(counterAmount);
        counters.forEach((k, v) -> {
            buf.writeUtf(k);
            buf.writeInt(v);
        });
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer != null) {
                clientPlayer.getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {
                    f.setStamina(stamina);
                    f.setMaxStamina(maxStamina);
                    f.setFeathers(feathers);
                    f.setStaminaDelta(this.staminaDelta);
                    counters.forEach(f::setCounter);
                    ClientFeathersData.getInstance().update(f);
                });
            }
        });
        return true;
    }
}
