package com.golfing8.kcommon.nms.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class BookData {
    private @Nullable Component author;
    private @Nullable Component title;
    private @Nullable List<Component> pages;
}
