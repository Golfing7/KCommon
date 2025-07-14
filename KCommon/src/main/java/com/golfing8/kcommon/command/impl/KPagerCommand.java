package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.CommandVisibility;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.config.lang.PagedMessage;
import lombok.Getter;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * A command for controlling pagers, normally hidden to players.
 */
@Cmd(
        name = "pager",
        description = "Control paged messages",
        visibility = CommandVisibility.PRIVATE,
        forPlayers = true
)
public class KPagerCommand extends KCommand {
    @Getter
    private static KPagerCommand instance;

    private final ExpiringMap<String, PagedMessage> liveMessages = ExpiringMap.builder()
            .expiration(60L, TimeUnit.SECONDS)
            .build();

    public void addMessage(PagedMessage message) {
        liveMessages.put(message.getId(), message);
    }

    @Override
    protected void onRegister() {
        instance = this;
        addArgument("pager-id", CommandArguments.ALPHANUMERIC_STRING);
        addArgument("page", CommandArguments.POSITIVE_INTEGER);
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        String pagerID = context.next();
        int page = context.next();

        PagedMessage message = liveMessages.get(pagerID);
        if (message == null)
            return;

        if (page > message.getTotalPages())
            return;

        message.displayTo(context.getSender(), page);
    }
}
