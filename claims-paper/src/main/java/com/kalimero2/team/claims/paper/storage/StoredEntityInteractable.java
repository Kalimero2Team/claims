package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.entity.EntityType;

public class StoredEntityInteractable implements EntityInteractable {
    private final EntityType entityType;
    private final boolean interact;
    private final boolean damage;

    public StoredEntityInteractable(EntityType entityType, boolean interact, boolean damage) {
        this.entityType = entityType;
        this.interact = interact;
        this.damage = damage;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isInteract() {
        return interact;
    }

    public boolean isDamage() {
        return damage;
    }
}
