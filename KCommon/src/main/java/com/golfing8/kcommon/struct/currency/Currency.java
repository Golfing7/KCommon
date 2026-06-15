package com.golfing8.kcommon.struct.currency;

import com.golfing8.kcommon.util.Placeholders;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for containing a currency type and amount.
 */
@Data
public class Currency {
    private final EconomyType economyType;
    private final double amount;
    private String formatOverride;

    public Currency(EconomyType economyType, double amount, @Nullable String formatOverride) {
        Preconditions.checkArgument(amount >= 0, "Amount must be greater than or equal to zero");
        this.economyType = economyType;
        this.amount = amount;
        this.formatOverride = formatOverride;
    }

    public Currency(EconomyType economyType, double amount) {
        this(economyType, amount, null);
    }

    /**
     * Sets the formatting override for this currency object
     *
     * @param formatOverride the formatting override
     * @return this
     */
    public Currency formattingOverride(String formatOverride) {
        this.formatOverride = formatOverride;
        return this;
    }

    /**
     * Formats the given amount using default formatting
     *
     * @param amount the amount
     * @return the format
     */
    public String format(int amount) {
        switch (economyType) {
            case EXP:
                return format(amount, this.formatOverride != null ? formatOverride : "{AMOUNT} Exp");
            case MONEY:
                return format(amount, this.formatOverride != null ? formatOverride : "${AMOUNT}");
            default:
                return format(amount, "{AMOUNT}");
        }
    }

    /**
     * Formats the given amount using the given formatting
     *
     * @param amount the amount
     * @param format the formatting
     * @return the formatted string
     */
    public String format(int amount, String format) {
        return Placeholders.parseFully(format, "AMOUNT", StringUtil.parseMoney(this.amount * amount));
    }

    /**
     * Adds this currency to the other
     *
     * @param amount the amount
     * @return the new currency
     */
    public Currency add(double amount) {
        return new Currency(economyType, this.amount + amount, formatOverride);
    }

    /**
     * Multiplies this currency by the given amount
     *
     * @param amount the amount
     * @return the new currency
     */
    public Currency times(double amount) {
        return new Currency(economyType, this.amount * amount, formatOverride);
    }

    /**
     * Checks if the given player can afford this currency amount
     *
     * @param player the player
     * @return true if they can afford it
     */
    public boolean canAfford(Player player) {
        return economyType.getCheckBalance().test(player, amount);
    }

    /**
     * Withdraws this amount from the given player
     *
     * @param player the player
     */
    public void withdraw(Player player) {
        economyType.getWithdrawBalance().accept(player, amount);
    }

    /**
     * Deposits the given amount for the given player
     *
     * @param player the player
     */
    public void deposit(Player player) {
        economyType.getDepositBalance().accept(player, amount);
    }
}
