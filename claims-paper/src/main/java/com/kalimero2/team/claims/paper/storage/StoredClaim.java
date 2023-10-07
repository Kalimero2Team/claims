package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.Chunk;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class StoredClaim implements Claim {

    private final Group owner;
    private final Chunk chunk;
    private final List<Group> members;
    private final List<BlockInteractable> blockInteractables;
    private final List<EntityInteractable> entityInteractables;
    private final HashMap<Flag, Boolean> flags;
    private final LocalDateTime claimedSince;
    private final LocalDateTime lastInteraction;
    private final LocalDateTime lastOnline;


    protected StoredClaim(Group owner, Chunk chunk, List<Group> members, List<BlockInteractable> blockInteractables, List<EntityInteractable> entityInteractables, HashMap<Flag, Boolean> flags, LocalDateTime claimedSince, LocalDateTime lastInteraction, LocalDateTime lastOnline) {
        this.owner = owner;
        this.chunk = chunk;
        this.members = members;
        this.blockInteractables = blockInteractables;
        this.entityInteractables = entityInteractables;
        this.flags = flags;
        this.claimedSince = claimedSince;
        this.lastInteraction = lastInteraction;
        this.lastOnline = lastOnline;
    }


    @Override
    public Group getOwner() {
        return owner;
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public List<Group> getMembers() {
        return members;
    }

    @Override
    public List<BlockInteractable> getBlockInteractables() {
        return blockInteractables;
    }

    @Override
    public List<EntityInteractable> getEntityInteractables() {
        return entityInteractables;
    }

    @Override
    public HashMap<Flag, Boolean> getFlags() {
        return flags;
    }

    @Override
    public LocalDateTime getClaimedSince() {
        return claimedSince;
    }

    @Override
    public LocalDateTime getLastInteraction() {
        return lastInteraction;
    }

    @Override
    public LocalDateTime getLastOnline() {
        return lastOnline;
    }
}
