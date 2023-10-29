package com.kalimero2.team.claims.api.event.flag;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FlagUnsetEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Claim claim;
    private final Flag flag;
    private boolean cancelled;


    public FlagUnsetEvent(Claim claim, Flag flag) {
        this.claim = claim;
        this.flag = flag;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Claim getClaim() {
        return claim;
    }

    public Flag getFlag() {
        return flag;
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
