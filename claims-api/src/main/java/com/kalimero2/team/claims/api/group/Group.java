package com.kalimero2.team.claims.api.group;

import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;

public abstract class Group {

    protected final List<GroupMember> members;
    private final int id;
    private final boolean isPlayer;
    protected String name;
    protected int maxClaims;


    protected Group(int id, String name, int maxClaims, boolean isPlayer, List<GroupMember> members) {
        this.id = id;
        this.name = name;
        this.maxClaims = maxClaims;
        this.isPlayer = isPlayer;
        this.members = members;
    }

    /**
     * Gets the id of the group
     *
     * @return the id of the group
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the group
     *
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the limit of claims that can be claimed by this group
     *
     * @return the limit of claims
     */
    public int getMaxClaims() {
        return maxClaims;
    }

    /**
     * Checks if the group represents a single player
     * Each player has such a group, that is representing the player for player specific claims
     *
     * @return if the group represents a single player
     * @see com.kalimero2.team.claims.api.ClaimsApi#getPlayerGroup(OfflinePlayer) to get the group of a player
     */
    public boolean isPlayer() {
        return isPlayer;
    }

    /**
     * Gets the list of members of this group
     *
     * @return the list of members
     */
    public List<GroupMember> getMembers() {
        return members;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group that = (Group) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
