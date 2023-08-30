package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.action.ClickAction;

import java.util.List;
import java.util.Map;

public class MenuSimple extends MenuAbstract {

    public MenuSimple(String title, int size, boolean clickable, boolean canExpire, Map<Integer, List<ClickAction>> actionMap) {
        super(title, size, clickable, canExpire, actionMap);
    }
}
