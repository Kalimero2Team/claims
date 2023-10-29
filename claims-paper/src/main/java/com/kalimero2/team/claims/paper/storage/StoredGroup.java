package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;

import java.util.List;

public class StoredGroup extends Group {

    protected StoredGroup(int id, String name, int maxClaims, boolean isPlayer, List<GroupMember> members) {
        super(id, name, maxClaims, isPlayer, members);

    }

    public static StoredGroup cast(Group group){
        return (StoredGroup) group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxClaims(int maxClaims) {
        this.maxClaims = maxClaims;
    }

    public void addMember(GroupMember member) {
        members.add(member);
    }

    public void removeMember(GroupMember member) {
        members.remove(member);
    }

}
