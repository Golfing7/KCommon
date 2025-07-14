package com.golfing8.kcommon.struct.filter;

import com.cryptomorin.xseries.XMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A tiered filter for items.
 * <p>
 * This filter applies itself to different aspects of an item in different priorities.
 * </p>
 */
@Getter
@Builder
@AllArgsConstructor
public class ItemFilter implements Filter<ItemStack> {
    /**
     * Filters to apply to the material.
     */
    private Set<StringFilter> materialFilters;
    /**
     * Filters to apply to the item name
     */
    private Set<StringFilter> itemNameFilters;
    /**
     * The item lore filters to apply
     */
    private Set<StringFilter> itemLoreFilters;
    /**
     * If colors should be stripped on string comparisons
     */
    private boolean stripColors;

    @Override
    public int filter(ItemStack itemStack) {
        if (itemStack == null)
            return 0;

        int maxFilter = 0;
        if (materialFilters != null && !materialFilters.isEmpty()) {
            String name = XMaterial.matchXMaterial(itemStack).name();
            for (StringFilter filter : materialFilters) {
                if (filter.filter(name) != 0) {
                    maxFilter = 1;
                    break;
                }
            }
        }

        if (!itemStack.hasItemMeta())
            return maxFilter;

        ItemMeta meta = itemStack.getItemMeta();
        if (itemNameFilters != null && !itemNameFilters.isEmpty() && meta.hasDisplayName()) {
            String displayName = stripColors ? ChatColor.stripColor(meta.getDisplayName()) : meta.getDisplayName();
            for (StringFilter filter : itemNameFilters) {
                if (filter.filter(displayName) != 0) {
                    maxFilter = 2;
                    break;
                }
            }
        }

        if (itemLoreFilters != null && !itemLoreFilters.isEmpty() && meta.hasLore()) {
            List<String> itemLore = stripColors ?
                    meta.getLore().stream().map(ChatColor::stripColor).collect(Collectors.toList()) :
                    meta.getLore();

            for (String loreLine : itemLore) {
                for (StringFilter filter : itemLoreFilters) {
                    if (filter.filter(loreLine) != 0) {
                        maxFilter = 2;
                        break;
                    }
                }
            }
        }
        return maxFilter;
    }
}
