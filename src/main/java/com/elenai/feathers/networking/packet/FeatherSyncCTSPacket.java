package com.elenai.feathers.networking.packet;

import com.elenai.feathers.capability.PlayerFeathersProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FeatherSyncCTSPacket {

    private final int feathers;
    private final int endurance;
    private final int staminaDelta;

    public FeatherSyncCTSPacket(int feathers, int endurance, int staminaDelta) {
        this.feathers = feathers;
        this.endurance = endurance;
        this.staminaDelta = staminaDelta;
    }

    public FeatherSyncCTSPacket(FriendlyByteBuf buf) {
        this.feathers = buf.readInt();
        this.endurance = buf.readInt();
        this.staminaDelta = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathers);
        buf.writeInt(endurance);
        buf.writeInt(staminaDelta);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            context.getSender().getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setStamina(feathers);
                f.setStaminaDelta(staminaDelta);
                f.setEnduranceStamina(endurance);
            });

        });
        return true;
    }
}
