package com.kalimero2.team.claims.api.event;

import com.kalimero2.team.claims.api.Claim;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChunkClaimEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Chunk chunk;
    private final Claim claim;

    public ChunkClaimEvent(Chunk chunk, Claim claim) {
        this.chunk = chunk;
        this.claim = claim;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
