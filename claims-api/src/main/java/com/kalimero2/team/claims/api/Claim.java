package com.kalimero2.team.claims.api;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Claim {

    Group getOwner();

    int getChunkX();

    int getChunkZ();

    UUID getWorld();

    LocalDateTime getClaimedSince();

    LocalDateTime getLastInteraction();

    LocalDateTime getLastOnline();
}
