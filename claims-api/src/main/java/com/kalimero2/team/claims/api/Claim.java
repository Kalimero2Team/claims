package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.Chunk;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface Claim {
    int getId();

    Group getOwner();

    Chunk getChunk();

    List<Group> getMembers();

    List<BlockInteractable> getBlockInteractables();

    List<EntityInteractable> getEntityInteractables();

    HashMap<Flag, Boolean> getFlags();

    LocalDateTime getClaimedSince();

    LocalDateTime getLastInteraction();

    LocalDateTime getLastOnline();
}
