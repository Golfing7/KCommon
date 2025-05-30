package com.golfing8.kcommon.command.flag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a command flag.
 * <p>
 * Command flags are meant to have the same semantics as Unix command flags.
 * </p>
 */
@Getter @AllArgsConstructor
public class CommandFlag {
    public static final String SHORT_FLAG_PREFIX = "-";
    public static final String LONG_FLAG_PREFIX = "--";

    /** The short name of the flag. Can be null if no short equivalent matters */
    private final @Nullable Character shortName;
    /** The full name of the command flag. */
    private final @NotNull String fullName;

    public CommandFlag(@NotNull String fullName) {
        this.shortName = null;
        this.fullName = fullName;
    }
}
