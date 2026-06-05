package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.command.flag.CommandFlag;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.struct.profiler.ProfileStatistics;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A command that lets you view the profiler timings of different aspects of KCommon plugins
 */
@Cmd(
        name = "ktimings",
        aliases = "kt",
        description = "Allows you to view the timings of different KCommon plugin aspects"
)
public class KTimingsCommand extends KCommand {
    @Override
    protected void onRegister() {
        KCommon.getInstance().addLanguageConstant("timings-command-no-data", "&cThere is no data!");
        KCommon.getInstance().addLanguageConstant("timings-command-key-data-verbose",
                "&e{KEY} &aProfile Data: (All/95%)",
                " &7- &aSamples: &e{SAMPLES}",
                " &7- &aAverage: &e({AVERAGE}/{AVERAGE_95})",
                " &7- &aMax: &e({MAX}/{MAX_95})",
                " &7- &aMin: &e({MIN}/{MIN_95})",
                " &7- &aSum: &e({SUM}/{SUM_95})",
                " &7- &aStandard Deviation: &e({STD_DEV})"
        );
        KCommon.getInstance().addLanguageConstant("timings-command-key-data",
                "&e{KEY} &aProfile Data: (All/95%)",
                " &7- &aAverage: &e({AVERAGE}/{AVERAGE_95})",
                " &7- &aMax: &e({MAX}/{MAX_95})",
                " &7- &aMin: &e({MIN}/{MIN_95})"
        );

        addArgument("module", CommandArguments.MODULE);
        addArgument("key", CommandArguments.ALPHANUMERIC_STRING, (k) -> null);

        addFlag(new CommandFlag('v', "verbose"));
        addFlag(new CommandFlag('r', "reset"));
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Module module = context.next();
        String key = context.next();
        boolean verbose = context.getFlagState('v') == TriState.TRUE;
        boolean reset = context.getFlagState('r') == TriState.TRUE;
        String messageKey = verbose ? "timings-command-key-data-verbose" : "timings-command-key-data";

        if (key == null) {
            Collection<ProfileStatistics> values = module.getProfiler().getStatistics().values();
            if (values.isEmpty()) {
                KCommon.getInstance().sendConfigMessage(context.getSender(), "timings-command-no-data");
                return;
            }

            for (ProfileStatistics statistics : values) {
                KCommon.getInstance().sendConfigMessage(context.getSender(), messageKey, statistics.toPlaceholderContainer());
            }

            if (reset) {
                module.getProfiler().resetData();
            }
        } else {
            ProfileStatistics statistics = module.getProfiler().getStatistics(key);
            if (statistics == null) {
                KCommon.getInstance().sendConfigMessage(context.getSender(), "timings-command-no-data");
                return;
            }

            KCommon.getInstance().sendConfigMessage(context.getSender(), messageKey, statistics.toPlaceholderContainer());

            if (reset) {
                module.getProfiler().resetData(key);
            }
        }
    }
}
