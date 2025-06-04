package com.golfing8.kcommon.struct.permission;

import com.golfing8.kcommon.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides context for checking permissions.
 */
public interface PermissionContext {
    /**
     * Gets the prefix of all permission checks
     *
     * @return the permission prefix
     */
    @Nullable
    String getPermissionPrefix();

    /**
     * Checks if the sender has the permission.
     *
     * @param sender the sender
     * @param permission the permission
     * @return true if they have the permission
     */
    default boolean hasPermission(CommandSender sender, String permission) {
        String prefix = getPermissionPrefix();
        if (StringUtil.isEmpty(prefix))
            return sender.hasPermission(permission);

        return sender.hasPermission(prefix + "." + permission);
    }
}
