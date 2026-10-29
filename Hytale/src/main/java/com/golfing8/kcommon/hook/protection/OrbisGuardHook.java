package com.golfing8.kcommon.hook.protection;

import com.golfing8.kcommon.nms.struct.Position;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.orbisguard.api.OrbisGuardAPI;
import com.orbisguard.api.protection.ProtectionAction;
import com.orbisguard.api.region.IRegion;
import com.orbisguard.api.region.IRegionManager;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Checks protection with OrbisGuard's region flags.
 */
public class OrbisGuardHook implements ProtectionHook {
    @Override
    public List<String> getRegions(String world, Position position) {
        List<String> ids = new ArrayList<>();
        for (IRegion region : OrbisGuardAPI.getInstance().getRegionContainer().getRegionsAt(world, position.getX(), position.getY(), position.getZ())) {
            ids.add(region.getId());
        }
        return ids;
    }

    @Override
    public boolean canPlayerBuild(PlayerRef player, String world, Position position) {
        return test(ProtectionAction.BLOCK_PLACE, world, position, player.getUuid());
    }

    @Override
    public boolean canAttack(PlayerRef attacker, PlayerRef attacked) {
        String world = worldOf(attacked);
        return world == null || test(ProtectionAction.PVP, world, positionOf(attacked), attacker.getUuid());
    }

    @Override
    public boolean canBeDamaged(PlayerRef player) {
        String world = worldOf(player);
        return world == null || test(ProtectionAction.ENTITY_DAMAGE, world, positionOf(player), player.getUuid());
    }

    private static boolean test(ProtectionAction action, String world, Position position, UUID player) {
        IRegionManager manager = OrbisGuardAPI.getInstance().getRegionContainer().getRegionManager(world);
        return manager == null || manager.testStateFlag(action.getFlagName(), position.getX(), position.getY(), position.getZ(), player);
    }

    private static String worldOf(PlayerRef player) {
        if (player.getWorldUuid() == null)
            return null;

        World world = Universe.get().getWorld(player.getWorldUuid());
        return world == null ? null : world.getName();
    }

    private static Position positionOf(PlayerRef player) {
        Vector3d position = player.getTransform().getPosition();
        return new Position(position.x, position.y, position.z);
    }
}
