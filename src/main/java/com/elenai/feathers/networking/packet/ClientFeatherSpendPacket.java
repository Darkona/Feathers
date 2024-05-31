package com.elenai.feathers.networking.packet;

import com.elenai.feathers.api.FeathersAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientFeatherSpendPacket {

    private final int feathers;

    public ClientFeatherSpendPacket(int feathers) {
        this.feathers = feathers;
    }

    public ClientFeatherSpendPacket(FriendlyByteBuf buf) {
        this.feathers = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathers);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            FeathersAPI.spendFeathers(context.getSender(), feathers, 20);
        });
        return true;
    }
}
