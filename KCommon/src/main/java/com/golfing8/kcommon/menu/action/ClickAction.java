package com.golfing8.kcommon.menu.action;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapped click task that contains a cooldown for clicking
 */
public class ClickAction {
    private final long cooldownLength;
    private final ClickRunnable clickRunnable;

    private final Map<UUID, Long> clickTimes;

    public ClickAction(ClickRunnable clickRunnable) {
        this(0, clickRunnable);
    }

    public ClickAction(long cooldownLength, ClickRunnable clickRunnable) {
        this.cooldownLength = cooldownLength;
        this.clickRunnable = clickRunnable;

        this.clickTimes = new HashMap<>();
    }

    /**
     * Called when a player attempts to click this action
     *
     * @param event the event
     */
    public void attemptClick(InventoryClickEvent event) {
        if (!clickTimes.containsKey(event.getWhoClicked().getUniqueId())) {
            clickRunnable.click(event);

            if (cooldownLength > 0) {
                clickTimes.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            }
        } else {
            if (System.currentTimeMillis() - clickTimes.get(event.getWhoClicked().getUniqueId()) < cooldownLength) {
                clickRunnable.clickCooldown(event);
            } else {
                clickRunnable.click(event);

                clickTimes.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
            }
        }
    }
}
