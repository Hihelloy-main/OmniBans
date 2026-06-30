package com.hihelloy.work.omnibans.command.executor;

import com.hihelloy.work.omnibans.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class OmniBansExecutor implements CommandExecutor, TabCompleter {

    private final SubCommand subCommand;

    public OmniBansExecutor(SubCommand subCommand) {
        this.subCommand = subCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return subCommand.tabComplete(sender, args);
    }

}
