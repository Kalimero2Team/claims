package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import org.bukkit.entity.EntityType;

public class StoredEntityInteractable extends EntityInteractable {
    public StoredEntityInteractable(EntityType entityType, boolean interact, boolean damage) {
        super(entityType, interact, damage);
    }

    public static StoredEntityInteractable cast(EntityInteractable entityInteractable){
        return (StoredEntityInteractable) entityInteractable;
    }

    public void setInteract(boolean interact) {
        this.interact = interact;
    }

    public void setDamage(boolean damage) {
        this.damage = damage;
    }
}