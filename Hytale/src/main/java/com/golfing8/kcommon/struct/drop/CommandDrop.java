package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.platform.Platform;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Represents a Drop for commands.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandDrop extends Drop<String> {
    private List<String> commands = Collections.emptyList();

    public CommandDrop(double chance, @Nullable String displayName, double maxBoost, List<String> commands) {
        super(chance, displayName, maxBoost);
        this.commands = commands;
    }

    @Override
    public List<String> getDrop() {
        return commands;
    }

    @Override
    public void giveTo(DropContext context) {
        if (context.getPlayer() == null)
            return;

        PlayerRef pref = context.getPlayer().getReference().getStore().getComponent(context.getPlayer().getReference(), PlayerRef.getComponentType());
        String username = pref.getUsername();
        // handleCommand runs async; chain so commands execute in their configured order
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (String command : getDrop()) {
            chain = chain.thenCompose(ignored -> HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command.replace("{PLAYER}", username)))
                    .exceptionally(exc -> {
                        Platform.get().getLogger().log(Level.SEVERE, "Error while executing command " + command, exc);
                        return null;
                    });
        }
    }
}
