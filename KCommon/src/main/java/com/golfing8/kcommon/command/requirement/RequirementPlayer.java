package com.golfing8.kcommon.command.requirement;

import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.config.lang.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

/**
 * Represents a requirement that the command sender must be a player.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequirementPlayer implements Requirement {
    @Getter
    private static final RequirementPlayer instance = new RequirementPlayer();

    @Override
    public boolean meetsRequirement(CommandContext context) {
        return context.getSender() instanceof Player;
    }

    @Override
    public Message getErrorMessage(CommandContext context) {
        return new Message("&cYou must be a player to use this command!");
    }
}
