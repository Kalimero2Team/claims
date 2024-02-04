package com.kalimero2.team.claims.api.group;

import org.bukkit.OfflinePlayer;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMember that = (GroupMember) o;
        return Objects.equals(player, that.player) && permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, permissionLevel);
    }
}
