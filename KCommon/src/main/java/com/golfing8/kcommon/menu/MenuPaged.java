package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;

import java.util.List;
import java.util.Map;

public class MenuPaged extends MenuAbstract {
    public MenuPaged(String title, int size, boolean clickable, boolean canExpire, Map<Integer, List<ClickAction>> actionMap, List<Placeholder> placeholders, List<MultiLinePlaceholder> multiLinePlaceholders) {
        super(title, size, clickable, canExpire, actionMap, placeholders, multiLinePlaceholders);
    }
}