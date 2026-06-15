package com.golfing8.kcommon.struct.currency;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.Pair;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.util.MS;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A class that contains a map of amounts for currencies.
 * <p>
 * For vault based economies, the key {@code VAULT} is used.
 * </p>
 */
@CASerializable.Options(flatten = true)
public final class CurrencyContainer implements Iterable<Pair<EconomyType, Currency>>, CASerializable, Cloneable {
    /**
     * The map containing all currency data.
     */
    private final Map<EconomyType, Currency> currencies;

    public CurrencyContainer() {
        this.currencies = new HashMap<>();
    }

    public CurrencyContainer(CurrencyContainer container) {
        this.currencies = new HashMap<>(container.currencies);
    }

    public CurrencyContainer(Map<EconomyType, Currency> container) {
        this.currencies = new HashMap<>(container);
    }

    public CurrencyContainer(Iterable<Currency> currencies) {
        this();
        for (Currency currency : currencies) {
            this.currencies.put(currency.getEconomyType(), currency);
        }
    }

    public CurrencyContainer(Currency... currencies) {
        this(Arrays.asList(currencies));
    }

    /**
     * Gets the amount of currency stored under the given type.
     *
     * @param type the type.
     * @return the currency.
     */
    public double getCurrency(EconomyType type) {
        if (currencies.containsKey(type)) {
            return currencies.get(type).getAmount();
        } else {
            return 0.0D;
        }
    }

    /**
     * Sets the currency amount of the given type.
     *
     * @param type  the type.
     * @param value the value.
     */
    public void setCurrency(EconomyType type, double value) {
        currencies.put(type, new Currency(type, value));
    }

    /**
     * Checks if the given player has at least as much currency as in this container.
     *
     * @param player the player.
     * @return true if the player has it, false if not.
     */
    public boolean has(Player player) {
        for (Map.Entry<EconomyType, Currency> entry : this.currencies.entrySet()) {
            if (!entry.getKey().checkBalance.test(player, entry.getValue().getAmount()))
                return false;
        }
        return true;
    }

    /**
     * Withdraws this amount from the given player.
     * Note that this method fails fast, if the player cannot afford a single part of this, no money is removed.
     *
     * @param player the player to withdraw from.
     * @return true if the withdrawal was successful, false if the player could not afford it.
     */
    public boolean withdraw(Player player) {
        if (!has(player))
            return false;

        //Take all currencies from the player.
        currencies.forEach((k, v) -> {
            k.withdrawBalance.accept(player, v.getAmount());
        });
        return true;
    }

    /**
     * Gives all the currencies in this container to the player.
     *
     * @param player the player to give them to.
     */
    public void giveTo(Player player) {
        //Give all currencies to the player.
        currencies.forEach((k, v) -> {
            k.getDepositBalance().accept(player, v.getAmount());
        });
    }

    /**
     * Formats all the currencies of this container as a multi-line placeholder.
     *
     * @return the multi-line placeholder.
     */
    public MultiLinePlaceholder formatPlaceholder() {
        return formatPlaceholder(SortMode.DESCENDING);
    }

    /**
     * Formats all the currencies of this container as a multi-line placeholder.
     *
     * @return the multi-line placeholder.
     */
    public MultiLinePlaceholder formatPlaceholder(SortMode mode) {
        return formatPlaceholder(mode, "{CURRENCY_FORMATTED}");
    }

    /**
     * Formats all the currencies of this container as a multi-line placeholder.
     *
     * @return the multi-line placeholder.
     */
    public MultiLinePlaceholder formatPlaceholder(SortMode mode, String format) {
        List<String> formattedList = new ArrayList<>();
        List<Pair<EconomyType, Currency>> currencies = getAllCurrencies();

        // Loop through all currencies and format them.
        if (mode != SortMode.UNORDERED)
            currencies.sort(mode.comparator);
        for (Pair<EconomyType, Currency> currencyEntry : currencies) {
            formattedList.add(MS.parseSingle(format,
                    "CURRENCY_FORMATTED", currencyEntry.getValue().format(1)));
        }
        return MultiLinePlaceholder.percentTrusted("CURRENCY_AMOUNTS", formattedList);
    }

    /**
     * Gets all currency amounts stored in this container.
     *
     * @return a collection of all currency amounts.
     */
    public List<Pair<EconomyType, Currency>> getAllCurrencies() {
        List<Pair<EconomyType, Currency>> allPairs = new ArrayList<>();
        currencies.forEach((k, v) -> allPairs.add(new Pair<>(k, v)));
        return allPairs;
    }

    /**
     * Multiplies this currencies amount by the given multiplier.
     *
     * @param multiplier the multiplier.
     * @return this.
     */
    public CurrencyContainer multiply(double multiplier) {
        currencies.forEach((key, value) -> currencies.compute(key, (k, v) -> v == null ? null : v.times(multiplier)));
        return this;
    }

    /**
     * Adds the given container to our count.
     *
     * @param other the other container to add.
     * @return this.
     */
    public CurrencyContainer add(CurrencyContainer other) {
        other.currencies.forEach((key, value) -> {
            currencies.compute(key, (k, v) -> v == null ? value : v.add(value.getAmount()));
        });
        return this;
    }

    @Override
    public CurrencyContainer clone() {
        return new CurrencyContainer(new HashMap<>(this.currencies));
    }

    @Override
    public String toString() {
        return "CurrencyContainer{" +
                "currencies=" + currencies +
                '}';
    }

    @NotNull
    @Override
    public Iterator<Pair<EconomyType, Currency>> iterator() {
        List<Pair<EconomyType, Currency>> allCurrencies = getAllCurrencies();
        allCurrencies.sort(SortMode.DESCENDING.comparator);
        return allCurrencies.iterator();
    }

    /**
     * An enumerations of sort modes for this currency container.
     */
    @AllArgsConstructor
    public enum SortMode {
        /**
         * Ascending, or natural ordering.
         */
        ASCENDING(Comparator.comparingDouble(p -> p.getValue().getAmount())),
        /**
         * Descending order.
         */
        DESCENDING((p1, p2) -> Double.compare(p2.getValue().getAmount(), p1.getValue().getAmount())),
        /**
         * Unordered, or rather, whatever order the backing map uses.
         */
        UNORDERED((p1, p2) -> 0),
        ;

        final Comparator<Pair<EconomyType, Currency>> comparator;
    }
}
