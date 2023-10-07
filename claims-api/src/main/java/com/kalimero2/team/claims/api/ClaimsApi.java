package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ClaimsApi {

    static ClaimsApi getApi() {
        return ClaimsApiHolder.getApi();
    }

    /**
     * Registers a new {@link Flag} to the plugin
     *
     * @param flag the flag to register
     */
    void registerFlag(Flag flag);

    /**
     * Unregisters a {@link Flag} from the plugin
     *
     * @param flag the flag to unregister
     */
    void unregisterFlag(Flag flag);

    /**
     * Gets a {@link Flag} by its {@link NamespacedKey}
     *
     * @param key the NamespacedKey of the flag
     * @return the flag or null if not found
     */
    Flag getFlag(NamespacedKey key);

    /**
     * Gets the state of a flag in a claim
     *
     * @param claim the claim to get the flag state from
     * @param flag  the flag to get the state from
     * @return the state of the flag
     */
    boolean getFlagState(Claim claim, Flag flag);

    /**
     * Sets the state of a flag in a claim
     *
     * @param claim the claim to set the flag state from
     * @param flag  the flag to set the state from
     * @param state the state of the flag
     */
    void setFlagState(Claim claim, Flag flag, boolean state);

    /**
     * Checks if a chunk is claimed
     *
     * @param chunk the chunk to check
     * @return true if claimed, false otherwise
     */
    default boolean isClaimed(Chunk chunk) {
        return getClaim(chunk) != null;
    }

    /**
     * Gets the claim of a chunk
     *
     * @param chunk the chunk to get the claim from
     * @return the claim of the chunk or null if not claimed
     */
    @Nullable Claim getClaim(Chunk chunk);

    /**
     * Gets the groups of a player
     *
     * @param player the player to get the groups from
     * @return the groups of the player
     */
    List<Group> getGroups(Player player);

    /**
     * Gets the "player" group of a player
     *
     * @param player the player to get the group from
     * @return the "player" group of the player
     */
    Group getPlayerGroup(Player player);

    /**
     * Sets the maximal amount of chunks a group can claim
     * @param group the group
     * @param max the new maximum
     * @see Group#getMaxClaims() to get the maximal amount of claims
     */
    void setMaxClaims(Group group, int max);

    /**
     * Sets the permission level of a member
     *
     * @param member the member to set the permission level from
     * @param level  the permission level to set
     * @return true if successful, false otherwise
     * @see GroupMember#getPermissionLevel() to get the permission level
     */
    boolean setPermissionLevel(GroupMember member, PermissionLevel level);

    /**
     * Adds a player to a group
     *
     * @param group  the group to add the player to
     * @param player the player to add to the group
     * @return the group member or null if failed
     * @see #addGroupMember(Group, Player, PermissionLevel) to add a player with a specific permission level
     * @see #setPermissionLevel(GroupMember, PermissionLevel) to change the permission level of a member
     */
    default @Nullable GroupMember addGroupMember(Group group, Player player) {
        return addGroupMember(group, player, PermissionLevel.MEMBER);
    }

    /**
     * Adds a player to a group
     *
     * @param group  the group to add the player to
     * @param player the player to add to the group
     * @param level  the permission level of the player
     * @return the group member or null if failed
     */
    @Nullable GroupMember addGroupMember(Group group, Player player, PermissionLevel level);

    /**
     * Gets a group member from a group
     *
     * @param group  the group to get the member from
     * @param player the player to get the member from
     * @return the group member or null if not found
     */
    @Nullable GroupMember getGroupMember(Group group, Player player);

    /**
     * Removes a group member from a group
     *
     * @param group  the group to remove the member from
     * @param member the member to remove
     * @return true if successful, false otherwise
     */
    boolean removeGroupMember(Group group, GroupMember member);

    /**
     * Claims a chunk for a group
     *
     * @param chunk the chunk to claim
     * @param group the group to claim the chunk for
     * @return true if successful, false otherwise
     */
    boolean claimChunk(Chunk chunk, Group group);

    /**
     * Unclaims a chunk
     *
     * @param chunk the chunk to unclaim
     * @return true if successful, false otherwise
     */
    boolean unclaimChunk(Chunk chunk);

    /**
     * Adds a group to a claim
     *
     * @param claim the claim to add the group to
     * @param group the group to add
     * @return true if successful, false otherwise
     */
    boolean addGroupToClaim(Claim claim, Group group);


    /**
     * Removes a group from a claim
     *
     * @param claim the claim to remove the group from
     * @param group the group to remove
     * @return true if successful, false otherwise
     */
    boolean removeGroupFromClaim(Claim claim, Group group);

    /**
     * Adds a player to a claim
     *
     * @param claim  the claim to add the player to
     * @param player the player to add
     * @return true if successful, false otherwise
     * @see #addGroupToClaim(Claim, Group) to add a group to a claim
     */
    default boolean addPlayerToClaim(Claim claim, Player player) {
        return addGroupToClaim(claim, getPlayerGroup(player));
    }

    /**
     * Removes a player from a claim
     *
     * @param claim  the claim to remove the player from
     * @param player the player to remove
     * @return true if successful, false otherwise
     * @see #removeGroupFromClaim(Claim, Group) to remove a group from a claim
     */
    default boolean removePlayerFromClaim(Claim claim, Player player) {
        return removeGroupFromClaim(claim, getPlayerGroup(player));
    }


}
