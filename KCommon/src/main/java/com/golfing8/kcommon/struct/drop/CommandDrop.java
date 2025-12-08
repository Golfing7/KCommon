package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

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
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", context.getPlayer().getName()));
            } catch (CommandException exc) {
                KCommon.getInstance().getLogger().log(Level.SEVERE, "Error while executing command " + command, exc);
            }
        });
    }
}
