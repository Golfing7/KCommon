package com.golfing8.kcommon.struct.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class DropContext {
    private final @Nullable Player player;
    private final double boost;

    public DropContext(@Nullable Player player) {
        this.player = player;
        this.boost = 1.0D;
    }

    public static final DropContext DEFAULT = new DropContext(null, 1.0D);
}
