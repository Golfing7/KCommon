package com.golfing8.kcommon.command.requirement;

import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.config.lang.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * A command requirement for a predicate that must be fulfilled.
 */
@Getter
@RequiredArgsConstructor
public class RequirementPredicate implements Requirement {
    private final Predicate<CommandContext> predicate;
    private Message message = new Message("&cYou can't use this command!");

    public RequirementPredicate(Predicate<CommandContext> predicate, Message message) {
        this.predicate = predicate;
        this.message = message;
    }

    @Override
    public boolean meetsRequirement(CommandContext context) {
        return predicate.test(context);
    }

    @Override
    public Message getErrorMessage(CommandContext context) {
        return message;
    }
}
