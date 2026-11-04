package com.golfing8.kcommon.menu;

import org.jetbrains.annotations.Nullable;

/**
 * One copy of a menu template added to a list with {@link MenuBuilder#addRow(String, String)}.
 * Its elements are addressed by their keys inside the template, and its placeholders apply only to it.
 */
public final class MenuRow extends MenuScope<MenuRow> {
    final MenuElement template;

    MenuRow(MenuElement template) {
        this.template = template;
    }

    @Override
    @Nullable MenuElement element(String key) {
        return template.find(key);
    }

    @Override
    MenuRow self() {
        return this;
    }
}
