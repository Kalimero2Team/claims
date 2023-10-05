package com.kalimero2.team.claims.api;

import java.util.List;

public interface Group {

    int getId();

    int getMaxClaims();

    boolean isPlayer();

    List<GroupMember> getMembers();

    void addMember(GroupMember member);

    void removeMember(GroupMember member);
}
