package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuManager;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.util.MS;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Cmd(
        name = "kmenu",
        aliases = "kmenus",
        description = "Open and view any KCommon menu",
        forPlayers = true
)
public class KMenuCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("menu-id", CommandArguments.UUID, (sender) -> "");

        KCommon.getInstance().getLangConfig().addLanguageConstant("kmenu-command.no-menu-exists-command", "&cNo menu exists with that ID.");
        KCommon.getInstance().getLangConfig().addLanguageConstant("kmenu-command.menus-list-command", Message.builder()
                .paged(true)
                .messages(Collections.singletonList("%MENU_DATA%"))
                .build()
        );
        KCommon.getInstance().getLangConfig().addLanguageConstant("kmenu-command.menu-line-format", "&f{TITLE} &8- &eViewers: {VIEWERS}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        // Was the argument not specified?
        if (context.getArg(0).isEmpty()) {
            // Send the menu list!
            List<String> menuLines = new ArrayList<>();
            List<String> menuLineFormat = KCommon.getInstance().getLangConfig().getMessage("kmenu-command.menu-line-format").getMessages();
            for (Menu menu : MenuManager.getInstance().getAll()) {
                menuLines.addAll(MS.parseAll(menuLineFormat,
                        "TITLE", menu.getTitle(),
                        "VIEWERS", menu.getViewers().stream().map(HumanEntity::getName).collect(Collectors.joining(", "))));
            }
            KCommon.getInstance().getLangConfig().getMessage("kmenu-command.menus-list-command").send(context.getSender(),
                    Collections.emptyList(),
                    Collections.singleton(MultiLinePlaceholder.percent("MENU_DATA", menuLines)));
            return;
        }

        UUID menuID = context.next();
        Menu menu = MenuManager.getInstance().getMenu(menuID);
        if (menu == null) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "kmenu-command.no-menu-exists-command");
            return;
        }

        context.getPlayer().openInventory(menu.getGUI());
    }
}
