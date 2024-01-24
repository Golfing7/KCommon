package com.golfing8.kcommon.command.requirement;

import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.config.lang.Message;

/**
 * Represents some predicate that must be met for a CommandSender to be able to execute a command.
 * <p>
 * Each requirement should strive to be atomic in nature (i.e. don't check that a player is in a team AND has played for more than a day)
 * </p>
 */
public interface Requirement {
    /**
     * Checks if the given command context meets the requirement to execute this command.
     *
     * @param context the context.
     * @return if it meets the requirement.
     */
    boolean meetsRequirement(CommandContext context);

    /**
     * Gets the error message as if the requirement wasn't met by the given context.
     * <p>
     * Error messages should be simplistic in nature and not include any superfluous information
     * other than to describe EXACTLY what the problem is.
     * </p>
     *
     * @param context the context.
     * @return the error message.
     */
    Message getErrorMessage(CommandContext context);
}
