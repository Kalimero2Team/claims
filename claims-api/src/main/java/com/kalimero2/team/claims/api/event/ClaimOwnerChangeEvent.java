package com.kalimero2.team.claims.api.event;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClaimOwnerChangeEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Chunk chunk;
    private final Group oldGroup;
    private final Group newGroup;
    private final Claim claim;

    public ClaimOwnerChangeEvent(Chunk chunk, Group group, Group newGroup, Claim claim) {
        this.chunk = chunk;
        this.oldGroup = group;
        this.newGroup = newGroup;
        this.claim = claim;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Group getOldGroup() {
        return oldGroup;
    }

    public Group getNewGroup() {
        return newGroup;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
