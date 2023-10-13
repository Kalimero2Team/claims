package com.kalimero2.team.claims.api.event.flag;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FlagSetEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;

    private final Claim claim;
    private final Flag flag;
    private final boolean newState;


    public FlagSetEvent(Claim claim, Flag flag, boolean newState) {
        this.claim = claim;
        this.flag = flag;
        this.newState = newState;
    }

    public Claim getClaim() {
        return claim;
    }

    public Flag getFlag() {
        return flag;
    }

    public boolean getNewState() {
        return newState;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
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
