package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.OfflinePlayer;

public class StoredGroupMember implements GroupMember {

    private final OfflinePlayer player;
    private final PermissionLevel permissionLevel;

    protected StoredGroupMember(OfflinePlayer player, PermissionLevel permissionLevel) {
        this.player = player;
        this.permissionLevel = permissionLevel;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }
}
