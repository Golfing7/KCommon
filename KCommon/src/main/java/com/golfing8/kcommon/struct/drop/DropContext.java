package com.golfing8.kcommon.struct.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class DropContext {
    private final @Nullable Player player;
    private double boost;
    public void setBoost(double boost) {
        if (this == DEFAULT)
            throw new IllegalStateException("Cannot set boost rate of default drop context!");
        this.boost = boost;
    }
    private final Map<String, Double> specificBoosts;

    public DropContext(@Nullable Player player) {
        this.player = player;
        this.boost = 1.0D;
        this.specificBoosts = new HashMap<>();
    }
    public DropContext(@Nullable Player player, double boost) {
        this.player = player;
        this.boost = boost;
        this.specificBoosts = new HashMap<>();
    }

    public static final DropContext DEFAULT = new DropContext(null, 1.0D, Collections.emptyMap());
}
