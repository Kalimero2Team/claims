package com.kalimero2.team.claims.api.interactable;

import org.bukkit.entity.EntityType;

public interface EntityInteractable {

    EntityType getEntityType();

    boolean isInteract();

    boolean isDamage();

}
