package com.hihelloy.work.omnibans.neoforge.command;


import com.hihelloy.work.omnibans.neoforge.OmniBansNeoForgeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class NeoForgeCommands {

    private final OmniBansNeoForgeMod mod;

    public NeoForgeCommands(OmniBansNeoForgeMod mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BrigadierCommandRegistry.registerAll(event.getDispatcher(), mod);
    }

}
