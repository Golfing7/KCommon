package com.golfing8.kcommon.nms.v1_8.event;

import com.golfing8.kcommon.nms.event.ArmorEquipEvent;
import com.golfing8.kcommon.nms.event.ArmorType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * An extension event listener that uses WineSpigot for enchanced armor events.
 *
 * This will recognize ANY changes to player equipment, even plugin manipulation!
 */
public class WineSpigotArmorEquipListener implements Listener {


    @EventHandler
    public void onArmorEquip(com.golfing8.winespigot.armorequip.ArmorEquipEvent event){
        //It's awfully convenient for me to do this as the event in wine is essentially a copy of the cookies listener.
        ArmorEquipEvent realEvent = new ArmorEquipEvent(event.getPlayer(),
                ArmorEquipEvent.EquipMethod.DELEGATED,
                ArmorType.valueOf(event.getType().name()),
                event.getOldArmorPiece(),
                event.getNewArmorPiece());

        realEvent.callEvent();

        //Update values to make sure they're reflected properly.
        event.setNewArmorPiece(realEvent.getNewArmorPiece());
        event.setOldArmorPiece(realEvent.getOldArmorPiece());
    }
}
