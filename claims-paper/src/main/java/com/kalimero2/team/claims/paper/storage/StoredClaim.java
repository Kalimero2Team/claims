package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoredClaim extends Claim {

    protected final List<Group> originalMembers;
    protected final List<MaterialInteractable> originalBlockInteractables;
    protected final List<EntityInteractable> originalEntityInteractables;
    protected final HashMap<Flag, Boolean> originalFlags;
    protected final Group originalOwner;
    protected final long originalLastOnline;
    protected final long originalLastInteraction;


    protected StoredClaim(int id, Group owner, Chunk chunk, List<Group> members, List<MaterialInteractable> blockInteractables, List<EntityInteractable> entityInteractables, HashMap<Flag, Boolean> flags, long claimedSince, long lastInteraction, long lastOnline) {
        super(id, owner, chunk, members, blockInteractables, entityInteractables, flags, claimedSince, lastInteraction, lastOnline);
        this.originalMembers = new ArrayList<>(members);
        this.originalBlockInteractables = new ArrayList<>(blockInteractables);
        this.originalEntityInteractables = new ArrayList<>(entityInteractables);
        this.originalFlags = new HashMap<>(flags);
        this.originalOwner = owner;
        this.originalLastOnline = lastOnline;
        this.originalLastInteraction = lastInteraction;
    }

    public static StoredClaim cast(Claim claim){
        return (StoredClaim) claim;
    }

    public void setOwner(Group owner) {
        this.owner = owner;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public void setLastInteraction(long lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    public void addMember(Group group) {
        members.add(group);
    }

    public void removeMember(Group group) {
        members.remove(group);
    }

    public void addMaterialInteractable(MaterialInteractable materialInteractable) {
        materialInteractables.add(materialInteractable);
    }

    public void removeMaterialInteractable(MaterialInteractable materialInteractable) {
        materialInteractables.remove(materialInteractable);
    }

    public void addEntityInteractable(EntityInteractable entityInteractable) {
        entityInteractables.add(entityInteractable);
    }

    public void removeEntityInteractable(EntityInteractable entityInteractable) {
        entityInteractables.remove(entityInteractable);
    }

    public void setFlag(Flag flag, boolean state) {
        flags.put(flag, state);
    }

    public void removeFlag(Flag flag) {
        flags.remove(flag);
    }
}
