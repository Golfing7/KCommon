package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.adapter.CASerializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a 'patch' to an item. This will apply a modification to some part of an item.
 */
@Getter
@Setter
@NoArgsConstructor
public class ItemPatch implements CASerializable {
    private @Nullable Patch materialPatch;
    private @Nullable Patch namePatch;
    private @Nullable Patch lorePatch;

    @Getter
    @NoArgsConstructor
    public static class Patch implements CASerializable {
        private String pattern;
        /**
         * Lazily initialized
         */
        private transient Pattern compiledPattern;

        public Pattern getCompiledPattern() {
            return compiledPattern == null ? compiledPattern = Pattern.compile(pattern) : compiledPattern;
        }

        private String replacement;

        public Patch(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        /**
         * Applies this patch to the given string.
         *
         * @param in the patch string in.
         * @return the patched string out.
         */
        public String applyTo(String in) {
            // Override the replacement if it is ever null.
            if (replacement == null)
                replacement = "";

            // If there's no pattern to match, the user probably just wants to replace everything with the replacement.
            if (pattern == null) {
                return replacement;
            }

            return getCompiledPattern().matcher(in).replaceAll(replacement);
        }
    }

    /**
     * Applies this patch to the given builder.
     *
     * @param builder the item builder.
     * @return the same builder.
     */
    public ItemStackBuilder applyToItem(ItemStackBuilder builder) {
        if (this.materialPatch != null) {
            String patchedMaterial = this.materialPatch.applyTo(builder.getItemType().name());
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(patchedMaterial);
            xMaterial.ifPresent(builder::material);
        }
        if (this.namePatch != null) {
            String patchedName = this.namePatch.applyTo(builder.getItemName() == null ? "" : builder.getItemName());
            builder.name(patchedName);
        }
        if (this.lorePatch != null) {
            String serializedLore = String.join(" \\n ", builder.getItemLore());
            String patchedSerializedLore = this.lorePatch.applyTo(serializedLore);
            List<String> patchedLore = Arrays.asList(patchedSerializedLore.split(" \\\\n "));
            builder.lore(patchedLore);
        }
        return builder;
    }
}
