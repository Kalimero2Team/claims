package com.kalimero2.team.claims.api.group;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public interface Group {

    /**
     * Gets the id of the group
     * @return the id of the group
     */
    int getId();

    /**
     * Gets the name of the group
     * @return the name of the group
     */
    String getName();

    /**
     * Gets the limit of claims that can be claimed by this group
     * @return the limit of claims
     */
    int getMaxClaims();

    /**
     * Checks if the group represents a single player
     * Each player has such a group, that is representing the player for player specific claims
     * @see com.kalimero2.team.claims.api.ClaimsApi#getPlayerGroup(OfflinePlayer) to get the group of a player
     * @return if the group represents a single player
     */
    boolean isPlayer();

    /**
     * Gets the list of members of this group
     * @return the list of members
     */
    List<GroupMember> getMembers();

}
