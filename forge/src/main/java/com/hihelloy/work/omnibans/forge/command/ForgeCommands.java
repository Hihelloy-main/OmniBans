package com.hihelloy.work.omnibans.forge.command;


import com.hihelloy.work.omnibans.forge.OmniBansForgeMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ForgeCommands {

    private final OmniBansForgeMod mod;

    public ForgeCommands(OmniBansForgeMod mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BrigadierCommandRegistry.registerAll(event.getDispatcher(), mod);
    }

}
