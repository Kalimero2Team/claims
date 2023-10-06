package com.kalimero2.team.claims.api.event.group;

import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupMemberPermissionLevelChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Group group;
    private final GroupMember member;
    private final PermissionLevel oldPermissionLevel;
    private final PermissionLevel newPermissionLevel;
    private boolean cancelled;

    public GroupMemberPermissionLevelChangeEvent(Group group, GroupMember member, PermissionLevel oldPermissionLevel, PermissionLevel newPermissionLevel) {
        this.group = group;
        this.member = member;

        this.oldPermissionLevel = oldPermissionLevel;
        this.newPermissionLevel = newPermissionLevel;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PermissionLevel getOldPermissionLevel() {
        return oldPermissionLevel;
    }

    public PermissionLevel getNewPermissionLevel() {
        return newPermissionLevel;
    }

    public Group getGroup() {
        return group;
    }

    public GroupMember getMember() {
        return member;
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
