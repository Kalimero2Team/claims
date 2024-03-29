package com.kalimero2.team.claims.api.event.group;

import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupAddMemberEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Group group;
    private final GroupMember member;

    public GroupAddMemberEvent(Group group, GroupMember member) {
        this.group = group;
        this.member = member;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
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

}
