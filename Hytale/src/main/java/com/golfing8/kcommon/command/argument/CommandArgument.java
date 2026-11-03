package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.util.HytalePlayers;
import com.golfing8.kcommon.util.StringUtilCore;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
     * Completions that are read somewhere other than the thread asking for them, such as the sender's world thread.
     * Used in place of {@link #completions} when set.
     */
    private @Nullable Function<ArgumentContext, CompletableFuture<? extends Collection<String>>> asyncCompletions;

    /**
     * Completes this argument through the given future instead of {@link #completions}.
     *
     * @param completer gives a future of the completions.
     * @return this argument.
     */
    public CommandArgument<A> completeAsync(Function<ArgumentContext, CompletableFuture<? extends Collection<String>>> completer) {
        this.asyncCompletions = completer;
        return this;
    }

    /**
     * Completes this argument on the sender's world thread, or the default world's for the console,
     * so world and entity data can be read safely.
     *
     * @param completer gives the completions, called on the world thread.
     * @return this argument.
     */
    public CommandArgument<A> completeOnWorld(Function<ArgumentContext, Collection<String>> completer) {
        return completeAsync(context -> {
            CompletableFuture<Collection<String>> future = new CompletableFuture<>();
            HytalePlayers.runOnSenderWorld(context.getSender(), () -> {
                try {
                    future.complete(completer.apply(context));
                } catch (Throwable thr) {
                    future.completeExceptionally(thr);
                }
            });
            return future;
        });
    }

    /**
     * Gets the completions for this argument, from {@link #asyncCompletions} if set.
     *
     * @param context the argument context.
     * @return a future of the completions.
     */
    public CompletableFuture<Collection<String>> complete(ArgumentContext context) {
        try {
            return asyncCompletions != null
                    ? asyncCompletions.apply(context).thenApply(values -> values)
                    : CompletableFuture.completedFuture(completions.apply(context));
        } catch (Throwable thr) {
            return CompletableFuture.failedFuture(thr);
        }
    }

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
        return new CommandArgument<>("A type of " + StringUtilCore.capitalize(enumm.getSimpleName()), argumentContext -> allStrings.keySet(),
                argumentContext -> allStrings.containsKey(argumentContext.getArgument().toUpperCase()),
                argument -> (T) allStrings.get(argument.getArgument().toUpperCase()));
    }

    /**
     * Constructs a command argument from the given map
     *
     * @param typeName the name of the type
     * @param map the map containing string to value pairs
     * @return the command argument
     * @param <T> the type of argument
     */
    public static <T> CommandArgument<T> fromMap(String typeName, Map<String, T> map) {
        Map<T, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        return fromCollection(typeName, map.values(), ctx -> map.get(ctx.getArgument()), reverseMap::get);
    }

    /**
     * Constructs a command argument from the given collection of values.
     *
     * @param typeName the type name
     * @param coll the collection of values
     * @param fromString the function converting context to values
     * @return the command argument
     * @param <T> the type of argument
     */
    public static <T> CommandArgument<T> fromCollection(String typeName, Collection<T> coll, Function<ArgumentContext, T> fromString) {
        return fromCollection(typeName, coll, fromString, Objects::toString);
    }

    /**
     * Constructs a command argument from the given collection of values.
     *
     * @param typeName the type name
     * @param coll the collection of values
     * @param fromString the function converting context to values
     * @param toString converts values to strings for command suggestions
     * @return the command argument
     * @param <T> the type of argument
     */
    public static <T> CommandArgument<T> fromCollection(String typeName, Collection<T> coll, Function<ArgumentContext, T> fromString, Function<T, String> toString) {
        Set<String> completions = coll.stream().map(toString).collect(Collectors.toSet());
        return new CommandArgument<>("A type of " + typeName,
                argumentContext -> completions,
                argumentContext -> completions.contains(argumentContext.getArgument()),
                fromString);
    }
}
