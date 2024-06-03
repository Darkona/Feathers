package com.elenai.feathers.api;

import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.ClientFeatherSpendPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FeathersHelper {

    @Deprecated
    public static boolean spendFeathers(int amount) {


        FeathersMessages.sendToServer(new ClientFeatherSpendPacket(amount));

        return FeathersAPI.spendFeathers(Minecraft.getInstance().player, amount, 20);
    }

}
