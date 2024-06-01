package com.elenai.feathers.networking.packet;

import com.elenai.feathers.client.ClientEventsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReplyWithWeightSTCPacket {
    private final int weight;

    public ReplyWithWeightSTCPacket(int weight) {
        this.weight = weight;
    }

    public ReplyWithWeightSTCPacket(FriendlyByteBuf buf) {
        this.weight = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(weight);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientEventsManager.currentWeight = weight;
        });
        return true;
    }
}
