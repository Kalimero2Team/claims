package com.kalimero2.team.claims.api.event;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChunkClaimedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Chunk chunk;
    private final Group group;
    private final Claim claim;

    public ChunkClaimedEvent(Chunk chunk, Group group, Claim claim) {
        this.chunk = chunk;
        this.group = group;
        this.claim = claim;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Group getGroup() {
        return group;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
