package com.golfing8.kcommon.struct.drop;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a Drop for commands.
 */
@Getter
public class CommandDrop extends Drop<String> {
    private final List<String> commands;

    public CommandDrop(double chance, @Nullable String displayName, double maxBoost, List<String> commands) {
        super(chance, displayName, maxBoost);
        this.commands = commands;
    }

    @Override
    public List<String> getDrop() {
        return commands;
    }

    @Override
    public void giveTo(DropContext context) {
        getDrop().forEach(command -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", context.getPlayer().getName()));
        });
    }
}
