package com.elenai.feathers.networking.packet;

import com.elenai.feathers.client.ClientFeathersData;
import com.elenai.feathers.config.FeathersClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EffectChangeSTCPacket {

    private final Effect name;

    private final int strength;
    private final boolean state;

    public EffectChangeSTCPacket(Effect name, boolean state, int strength) {
        this.name = name;
        this.state = state;
        this.strength = strength;
    }

    public EffectChangeSTCPacket(FriendlyByteBuf buf) {
        this.name = buf.readEnum(Effect.class);
        this.state = buf.readBoolean();
        this.strength = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(name);
        buf.writeBoolean(state);
        buf.writeInt(strength);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            switch (name) {
                case COLD:
                    ClientFeathersData.cold = state;
                    if (ClientFeathersData.cold && FeathersClientConfig.FROST_SOUND.get()) {

                        Minecraft instance = Minecraft.getInstance();
                        assert instance.level != null;
                        assert instance.player != null;
                        instance.level.playLocalSound(instance.player.blockPosition(),
                                SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.hurt_freeze")),
                                SoundSource.PLAYERS, 1f, instance.level.random.nextFloat(), false);
                    }
                    break;
                case HOT:
                    ClientFeathersData.hot = state;
                    break;
                case ENDURANCE:
                    ClientFeathersData.endurance = state;
                    ClientFeathersData.enduranceFeathers = strength;
                    break;
                case ENERGIZED:
                    ClientFeathersData.energized = state;
                    break;
                case FATIGUE:
                    ClientFeathersData.fatigued = state;
                    break;
                case MOMENTUM:
                    ClientFeathersData.momentum = state;
                    break;
            }
        });
        return true;
    }

}
