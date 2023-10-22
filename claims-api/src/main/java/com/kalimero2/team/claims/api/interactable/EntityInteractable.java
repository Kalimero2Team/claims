package com.kalimero2.team.claims.api.interactable;

import org.bukkit.entity.EntityType;

public abstract class EntityInteractable {

    private final EntityType entityType;
    protected boolean interact;
    protected boolean damage;

    public EntityInteractable(EntityType entityType, boolean interact, boolean damage) {
        this.entityType = entityType;
        this.interact = interact;
        this.damage = damage;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean canInteract() {
        return interact;
    }

    public boolean canDamage() {
        return damage;
    }
}
