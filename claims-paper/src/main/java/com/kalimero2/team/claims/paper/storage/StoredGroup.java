package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;

import java.util.List;

public class StoredGroup implements Group {

    private final int id;
    private final String name;
    private final int maxClaims;
    private final boolean isPlayer;
    private final List<GroupMember> members;

    protected StoredGroup(int id, String name, int maxClaims, boolean isPlayer, List<GroupMember> members) {
        this.id = id;
        this.name = name;
        this.maxClaims = maxClaims;
        this.isPlayer = isPlayer;
        this.members = members;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxClaims() {
        return maxClaims;
    }

    @Override
    public boolean isPlayer() {
        return isPlayer;
    }

    @Override
    public List<GroupMember> getMembers() {
        return members;
    }
}
