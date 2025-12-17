package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigPath;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A command for giving custom items
 */
@Cmd(
        name = "kitem",
        description = "Get an item from a config"
)
public class KItemCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("path", CommandArguments.CONFIG_PATH);
        addArgument("player", CommandArguments.PLAYER);
        addArgument("amount", CommandArguments.POSITIVE_INTEGER, sender -> "1");

        KCommon.getInstance().getLangConfig().addLanguageConstant("kitem-command.no-entries", "&cThat config entry does not exist!");
        KCommon.getInstance().getLangConfig().addLanguageConstant("kitem-command.item-not-defined-properly", "&cThat item was not defined properly!");
        KCommon.getInstance().getLangConfig().addLanguageConstant("kitem-command.item-given", "&aGave &e{PLAYER} &aa config item.");
        KCommon.getInstance().getLangConfig().addLanguageConstant("kitem-command.item-received", "&aReceived a config item.");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        ConfigPath configPath = context.next();
        Player player = context.next();
        int amount = context.next();

        List<ConfigEntry> entries = configPath.enumerate();
        if (entries.isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "kitem-command.no-entries");
            return;
        }

        ItemStackBuilder builder;
        try {
            builder = ConfigTypeRegistry.getFromType(entries.get(0), ItemStackBuilder.class);
        } catch (Exception exc) {
            // Failed to parse the item
            KCommon.getInstance().sendConfigMessage(context.getSender(), "kitem-command.item-not-defined-properly");
            return;
        }
        if (builder == null) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "kitem-command.item-not-defined-properly");
            return;
        }
        builder.amount(amount);
        PlayerUtil.givePlayerItemSafe(player, builder.buildFromTemplate());
        KCommon.getInstance().sendConfigMessage(player, "kitem-command.item-received");
        KCommon.getInstance().sendConfigMessage(context.getSender(), "kitem-command.item-given", "PLAYER", player.getName());
    }
}
