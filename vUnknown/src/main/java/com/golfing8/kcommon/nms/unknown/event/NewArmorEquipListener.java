package com.golfing8.kcommon.nms.unknown.event;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.golfing8.kcommon.nms.event.ArmorEquipEvent;
import com.golfing8.kcommon.nms.event.ArmorType;
import com.golfing8.kcommon.nms.event.DelegatedArmorListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class NewArmorEquipListener implements DelegatedArmorListener {

    @EventHandler
    public void onArmorEquip(PlayerArmorChangeEvent event) {
        ArmorEquipEvent otherEvent = new ArmorEquipEvent(event.getPlayer(), ArmorEquipEvent.EquipMethod.DELEGATED,
                event.getSlotType() == PlayerArmorChangeEvent.SlotType.HEAD ? ArmorType.HELMET :
                        event.getSlotType() == PlayerArmorChangeEvent.SlotType.CHEST ? ArmorType.CHESTPLATE :
                                event.getSlotType() == PlayerArmorChangeEvent.SlotType.LEGS ? ArmorType.LEGGINGS :
                                        event.getSlotType() == PlayerArmorChangeEvent.SlotType.FEET ? ArmorType.BOOTS : ArmorType.HELMET,
                event.getOldItem(),
                event.getNewItem());

        Bukkit.getPluginManager().callEvent(otherEvent);
    }

}
