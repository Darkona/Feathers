package com.darkona.feathers.networking.packet;

import com.darkona.feathers.client.ClientFeathersData;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Getter
public class FeatherSTCDebugPacket {

    private final int usedFeathers;
    private final int gainedFeathers;
    private final String reason;
    private final boolean used;
    private final boolean gained;

    public static FeatherSTCDebugPacket usedPacket(int usedFeathers, String reason){
        return new FeatherSTCDebugPacket(usedFeathers, 0, reason, true, false);
    }

    public static FeatherSTCDebugPacket gainedPacket(int gainedFeathers, String reason){
        return new FeatherSTCDebugPacket(0, gainedFeathers, reason, false, true);
    }

    public FeatherSTCDebugPacket(int usedFeathers, int gainedFeathers, String reason, boolean used, boolean gained) {
        this.usedFeathers = usedFeathers;
        this.gainedFeathers = gainedFeathers;
        this.reason = reason;
        this.used = used;
        this.gained = gained;
    }

    public FeatherSTCDebugPacket(FriendlyByteBuf buf) {
        usedFeathers = buf.readInt();
        gainedFeathers = buf.readInt();
        reason = buf.readUtf();
        used = buf.readBoolean();
        gained = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(usedFeathers);
        buf.writeInt(gainedFeathers);
        buf.writeUtf(reason);
        buf.writeBoolean(used);
        buf.writeBoolean(gained);
    }

    public static void handle(FeatherSTCDebugPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    ClientFeathersData.getInstance().setExtendedDebugInfo(message);
                }
            }));
        }
    }



}
