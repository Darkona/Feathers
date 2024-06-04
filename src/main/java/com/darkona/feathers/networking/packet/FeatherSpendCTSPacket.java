package com.darkona.feathers.networking.packet;

import com.darkona.feathers.api.FeathersAPI;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Getter
public class FeatherSpendCTSPacket {

    private final int feathersToSpend;
    private final int cooldown;

    public FeatherSpendCTSPacket(int feathersToSpend, int cooldown) {
        this.feathersToSpend = feathersToSpend;
        this.cooldown = cooldown;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(feathersToSpend);
        buf.writeInt(cooldown);
    }

    public FeatherSpendCTSPacket(FriendlyByteBuf buf) {
        this.feathersToSpend = buf.readInt();
        this.cooldown = buf.readInt();
    }

    public static void handle(FeatherSpendCTSPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                Player player = context.getSender();
                if(player != null) {
                    Level level = player.level();
                    FeathersAPI.spendFeathers(player, message.feathersToSpend, message.cooldown);
                }
            });
        }

        context.setPacketHandled(true);
    }
}
