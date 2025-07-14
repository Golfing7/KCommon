package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.util.StringUtil;
import lombok.Data;
import lombok.var;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents an argument for a command.
 * <p>
 * By convention, the field {@link #predicate} should return true if and only if {@link #getter} will properly supply an object.
 * </p>
 */
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
    /**
     * A getter for the argument. Takes in the argument context and converts it to whatever type was requested.
     */
    private final Function<ArgumentContext, A> getter;

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
                argument -> (T) allStrings.get(argument.getArgument().toUpperCase()));
    }

    public static <T> CommandArgument<T> fromMap(String typeName, Map<String, T> map) {
        Map<T, String> reverseMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        return fromCollection(typeName, map.values(), ctx -> map.get(ctx.getArgument()), reverseMap::get);
    }

    public static <T> CommandArgument<T> fromCollection(String typeName, Collection<T> coll, Function<ArgumentContext, T> fromString) {
        return fromCollection(typeName, coll, fromString, Objects::toString);
    }

    public static <T> CommandArgument<T> fromCollection(String typeName, Collection<T> coll, Function<ArgumentContext, T> fromString, Function<T, String> toString) {
        Set<String> completions = coll.stream().map(toString).collect(Collectors.toSet());
        return new CommandArgument<>("A type of " + typeName,
                argumentContext -> completions,
                argumentContext -> completions.contains(argumentContext.getArgument()),
                fromString);
    }
}
