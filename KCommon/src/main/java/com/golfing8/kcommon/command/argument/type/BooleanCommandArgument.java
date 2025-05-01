package com.golfing8.kcommon.command.argument.type;

import com.golfing8.kcommon.command.argument.ArgumentContext;
import com.golfing8.kcommon.command.argument.CommandArgument;
import lombok.Getter;
import lombok.var;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A command argument that accepts boolean in various forms.
 */
public class BooleanCommandArgument extends CommandArgument<Boolean> {
    /** The arguments mapped to their boolean value */
    @Getter
    private final Map<String, Boolean> valueMap;
    public BooleanCommandArgument(Map<String, Boolean> valueMap) {
        super("A boolean", (context) -> valueMap.keySet(), (context) -> valueMap.containsKey(context.getArgument().toLowerCase()), ctx -> valueMap.get(ctx.getArgument().toLowerCase()));
        Map<String, Boolean> filteredMap = new HashMap<>();
        for (var entry : valueMap.entrySet()) {
            if (entry.getValue() == null)
                throw new IllegalArgumentException("Cannot have null boolean values");

            filteredMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        this.valueMap = filteredMap;
    }
}
