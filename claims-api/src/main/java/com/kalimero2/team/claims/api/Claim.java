package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.group.Group;
import org.bukkit.Chunk;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Claim {

    Group getOwner();

    Chunk getChunk();

    LocalDateTime getClaimedSince();

    LocalDateTime getLastInteraction();

    LocalDateTime getLastOnline();
}
