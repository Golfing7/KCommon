package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Drop for commands.
 */
@Getter
public class CommandDrop extends Drop<String> {
    private List<String> items;
    public CommandDrop(double chance, String message, List<String> commands) {
        super(chance, message);
        this.items = commands;
    }

    @Override
    public List<String> getDrop() {
        return items;
    }

    @Override
    public void giveTo(Player player) {
        getDrop().forEach(command -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", player.getName()));
        });
    }
}
