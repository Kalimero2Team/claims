package com.kalimero2.team.claims.api.group;

import org.bukkit.entity.Player;

public interface GroupMember {
    /**
     * The player of the member
     * @return the player of the member
     */
    Player getPlayer();

    /**
     * Permission level of the member in the group
     * @return the permission level of the member
     * @see com.kalimero2.team.claims.api.ClaimsApi#setPermissionLevel(GroupMember, PermissionLevel) to set the permission level
     */
    PermissionLevel getPermissionLevel();
}
