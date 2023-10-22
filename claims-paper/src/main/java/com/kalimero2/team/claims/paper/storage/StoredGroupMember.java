package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.OfflinePlayer;

public class StoredGroupMember extends GroupMember {

    protected StoredGroupMember(OfflinePlayer player, PermissionLevel permissionLevel) {
        super(player, permissionLevel);
    }

    public static StoredGroupMember cast(GroupMember groupMember){
        return (StoredGroupMember) groupMember;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}
