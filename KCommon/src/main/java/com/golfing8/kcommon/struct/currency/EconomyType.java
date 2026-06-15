package com.golfing8.kcommon.struct.currency;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.util.SetExpFix;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Represents a basic type of currency or economy on the server
 */
@AllArgsConstructor @Getter
public enum EconomyType {
    MONEY((player, amount) -> KCommon.getInstance().getEconomy().has(player, amount),
            (player, amount) -> KCommon.getInstance().getEconomy().withdrawPlayer(player, amount),
            (player, amount) -> KCommon.getInstance().getEconomy().depositPlayer(player, amount),
            "${AMOUNT}"
    ),
    EXP((player, amount) -> SetExpFix.getTotalExperience(player) >= amount,
            (player, amount) -> SetExpFix.setTotalExperience(player, SetExpFix.getTotalExperience(player) - amount.intValue()),
            (player, amount) -> SetExpFix.setTotalExperience(player, SetExpFix.getTotalExperience(player) + amount.intValue()),
            "{AMOUNT} Exp"
    ),
    ;

    final BiPredicate<Player, Double> checkBalance;
    final BiConsumer<Player, Double> withdrawBalance;
    final BiConsumer<Player, Double> depositBalance;
    final String defaultFormat;
}
