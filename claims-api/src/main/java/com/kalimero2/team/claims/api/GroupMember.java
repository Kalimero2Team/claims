package com.kalimero2.team.claims.api;

import java.util.UUID;

public interface GroupMember {
    UUID getPlayer();

    PermissionLevel getPermissionLevel();
}
