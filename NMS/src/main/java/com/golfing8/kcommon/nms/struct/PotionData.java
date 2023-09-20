package com.golfing8.kcommon.nms.struct;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.potion.PotionType;

/**
 * A wrapper class for potion data.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class PotionData {
    private PotionType potionType;
    private boolean amplified;
    private boolean extended;
}
