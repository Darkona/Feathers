package com.darkona.feathers.networking.packet;

import com.darkona.feathers.api.FeathersAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class FeatherSpendCTSPacket {

    private final int feathers;

    public FeatherSpendCTSPacket(int feathers) {
        this.feathers = feathers;
    }

    public FeatherSpendCTSPacket(FriendlyByteBuf buf) {
        this.feathers = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathers);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        return context.enqueueWork(() -> FeathersAPI.spendFeathers(context.getSender(), feathers, 20)).isDone();
    }
}
