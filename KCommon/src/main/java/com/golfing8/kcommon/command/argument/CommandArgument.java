package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an argument for a command.
 * <p>
 * By convention, the field {@link #predicate} should return true if and only if {@link #getter} will properly supply an object.
 * </p>
 */
@AllArgsConstructor
@Data
public class CommandArgument<A> {
    /**
     * A description of what this argument wants. I.e. 'A module', 'An offline player', 'A positive number'
     */
    private final String description;
    /**
     * The function to get all tab completions for this argument.
     */
    private final Function<ArgumentContext, Collection<String>> completions;
    /**
     * The predicate to verify an argument.
     */
    private final Predicate<ArgumentContext> predicate;
    /** A getter for the argument. Takes in the string argument and converts it to whatever type was requested. */
    private final Function<String, A> getter;

    /**
     * Creates a command argument with all valid types being from the given enum.
     *
     * @param enumm the enum.
     * @return the built command argument.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> CommandArgument<T> fromEnum(Class<T> enumm) {
        EnumSet<T> set = EnumSet.allOf(enumm);
        Map<String, Enum<T>> allStrings = new HashMap<>();
        set.forEach(en -> allStrings.put(en.name(), en));

        //Generate the argument.
        return new CommandArgument<>("A type of " + StringUtil.capitalize(enumm.getSimpleName()), argumentContext -> allStrings.keySet(),
                argumentContext -> allStrings.containsKey(argumentContext.getArgument().toUpperCase()),
                argument -> (T) allStrings.get(argument.toUpperCase()));
    }
}
