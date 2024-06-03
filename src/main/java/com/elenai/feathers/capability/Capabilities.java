package com.elenai.feathers.capability;

import com.elenai.feathers.api.IFeathers;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {
    public static Capability<IFeathers> PLAYER_FEATHERS = CapabilityManager.get(new CapabilityToken<>() {
    });


}
