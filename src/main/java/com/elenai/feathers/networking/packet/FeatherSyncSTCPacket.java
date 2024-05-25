package com.elenai.feathers.networking.packet;

import com.elenai.feathers.client.ClientFeathersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FeatherSyncSTCPacket {
    private final int feathers;
    private final int maxFeathers;
    private final int regenRate;
    private final int weight;
    private final int endurance;

    private final int maxCooldown;

    public FeatherSyncSTCPacket(int feathers, int maxFeathers, int regenRate, int weight, int endurance, int maxCooldown) {
        this.feathers = feathers;
        this.maxFeathers = maxFeathers;
        this.regenRate = regenRate;
        this.weight = weight;
        this.endurance = endurance;
        this.maxCooldown = maxCooldown;
    }

    public FeatherSyncSTCPacket(FriendlyByteBuf buf) {
        this.feathers = buf.readInt();
        this.maxFeathers = buf.readInt();
        this.regenRate = buf.readInt();
        this.weight = buf.readInt();
        this.endurance = buf.readInt();
        this.maxCooldown = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathers);
        buf.writeInt(maxFeathers);
        buf.writeInt(regenRate);
        buf.writeInt(weight);
        buf.writeInt(endurance);
        buf.writeInt(maxCooldown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFeathersData.setFeathers(feathers);
            ClientFeathersData.setMaxFeathers(maxFeathers);
            ClientFeathersData.setRegenRate(regenRate);
            ClientFeathersData.setWeight(weight);
            ClientFeathersData.setEnduranceFeathers(endurance);
            ClientFeathersData.setMaxCooldown(maxCooldown);
        });
        return true;
    }
}
