package com.kalimero2.team.claims.api.event;

import com.kalimero2.team.claims.api.Claim;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClaimExpireEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Claim claim;
    private boolean cancelled;

    public ClaimExpireEvent(Claim claim) {
        this.claim = claim;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
