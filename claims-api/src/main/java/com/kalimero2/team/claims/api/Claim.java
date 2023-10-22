package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.Chunk;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class Claim {

    private final int id;
    private final long claimedSince;
    private final Chunk chunk;
    protected final List<Group> members;
    protected final List<MaterialInteractable> materialInteractables;
    protected final List<EntityInteractable> entityInteractables;
    protected final HashMap<Flag, Boolean> flags;
    protected Group owner;
    protected long lastOnline;
    protected long lastInteraction;

    protected Claim(int id, Group owner, Chunk chunk, List<Group> members, List<MaterialInteractable> blockInteractables, List<EntityInteractable> entityInteractables, HashMap<Flag, Boolean> flags, long claimedSince, long lastInteraction, long lastOnline) {
        this.id = id;
        this.owner = owner;
        this.chunk = chunk;
        this.members = members;
        this.materialInteractables = blockInteractables;
        this.entityInteractables = entityInteractables;
        this.flags = flags;
        this.claimedSince = claimedSince;
        this.lastInteraction = lastInteraction;
        this.lastOnline = lastOnline;
    }


    public int getId() {
        return id;
    }

    public Group getOwner() {
        return owner;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public List<Group> getMembers() {
        return members;
    }

    public List<MaterialInteractable> getMaterialInteractables() {
        return materialInteractables;
    }

    public List<EntityInteractable> getEntityInteractables() {
        return entityInteractables;
    }

    public HashMap<Flag, Boolean> getFlags() {
        return flags;
    }

    public long getClaimedSince() {
        return claimedSince;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim that = (Claim) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
