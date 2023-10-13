package com.kalimero2.team.claims.api.event;

import com.kalimero2.team.claims.api.group.Group;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChunkUnclaimedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Chunk chunk;
    private final Group group;

    public ChunkUnclaimedEvent(Chunk chunk, Group group) {
        this.chunk = chunk;
        this.group = group;
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

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
