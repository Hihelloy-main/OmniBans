package com.hihelloy.work.omnibans.fabric.command;


import com.hihelloy.work.omnibans.fabric.OmniBansFabricMod;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class FabricCommands {

    private FabricCommands() {
    }

    public static void register(OmniBansFabricMod mod) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            BrigadierCommandRegistry.registerAll(dispatcher, mod));
    }

}
