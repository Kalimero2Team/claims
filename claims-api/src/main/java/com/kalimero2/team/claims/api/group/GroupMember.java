package com.kalimero2.team.claims.api.group;

import org.bukkit.OfflinePlayer;

public abstract class GroupMember {

    private final OfflinePlayer player;
    protected PermissionLevel permissionLevel;

    protected GroupMember(OfflinePlayer player, PermissionLevel permissionLevel) {
        this.player = player;
        this.permissionLevel = permissionLevel;
    }

    /**
     * The player of the member
     *
     * @return the player of the member
     */
    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Permission level of the member in the group
     *
     * @return the permission level of the member
     * @see com.kalimero2.team.claims.api.ClaimsApi#setPermissionLevel(Group, GroupMember, PermissionLevel) to set the permission level
     */
    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }
}
