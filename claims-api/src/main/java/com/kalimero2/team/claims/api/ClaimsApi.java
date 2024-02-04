package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
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
     * Gets all registered {@link Flag}s
     *
     * @return all registered flags
     */
    List<Flag> getFlags();

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
     * @return true if successful, false otherwise
     */
    boolean setFlagState(Claim claim, Flag flag, boolean state);

    /**
     * Unsets the state of a flag in a claim
     *
     * @param claim the claim to unset the flag state from
     * @param flag  the flag to unset the state from
     * @return true if successful, false otherwise
     */
    boolean unsetFlagState(Claim claim, Flag flag);

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
     * Gets all claims in a world. (If claim isn't loaded, members and interactables will be empty)
     *
     * @param world the world to get the claims from
     * @return all claims in specified world
     */
    @Deprecated(forRemoval = true)
    List<Claim> getClaims(World world);

    /**
     * Gets the claims of a group
     *
     * @param group the group to get the claims from
     * @return the claims of the group
     * @see #getClaims(OfflinePlayer) to get the claims of a player
     */
    List<Claim> getClaims(Group group);

    /**
     * Gets the amount of claims of a group
     *
     * @param group the group to get the claims from
     * @return the amount of claims of the group
     */
    int getClaimAmount(Group group);


    /**
     * Gets the claims of a player
     *
     * @param player the player to get the claims from
     * @return the claims of the player
     * @see #getClaims(Group) to get the claims of a group
     */
    default List<Claim> getClaims(OfflinePlayer player) {
        return getClaims(getPlayerGroup(player));
    }

    /**
     * Gets the groups of a player
     *
     * @param player the player to get the groups from
     * @return the groups of the player
     * @see #getGroups(OfflinePlayer, PermissionLevel) to get the groups of a player where the player has at least the specified permission level
     */
    default List<Group> getGroups(OfflinePlayer player) {
        return getGroups(player, PermissionLevel.MEMBER);
    }

    /**
     * Gets the groups of a player where the player has at least the specified permission level
     *
     * @param player          the player to get the groups from
     * @param permissionLevel the minimum permission level
     * @return the groups of the player
     */
    List<Group> getGroups(OfflinePlayer player, PermissionLevel permissionLevel);

    /**
     * Get a group by its id
     *
     * @param id the id of the group
     * @return the group or null if not found
     */
    @Nullable Group getGroup(int id);

    /**
     * Get a group by its name
     *
     * @param name the name of the group
     * @return the group or null if not found
     */
    @Nullable Group getGroup(String name);

    /**
     * Get all groups
     *
     * @return all groups
     */
    List<Group> getGroups();

    /**
     * Gets the "player" group of a player
     *
     * @param player the player to get the group from
     * @return the "player" group of the player
     */
    Group getPlayerGroup(OfflinePlayer player);

    /**
     * Sets the maximal amount of chunks a group can claim
     *
     * @param group the group
     * @param max   the new maximum
     * @see Group#getMaxClaims() to get the maximal amount of claims
     */
    void setMaxClaims(Group group, int max);

    /**
     * Sets the permission level of a member
     *
     * @param group  the group to set the permission level
     * @param member the member to set the permission level
     * @param level  the permission level to set
     * @return true if successful, false otherwise
     * @see GroupMember#getPermissionLevel() to get the permission level
     */
    boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level);

    /**
     * Adds a player to a group
     *
     * @param group  the group to add the player to
     * @param player the player to add to the group
     * @return the group member or null if failed
     * @see #addGroupMember(Group, OfflinePlayer, PermissionLevel) to add a player with a specific permission level
     * @see #setPermissionLevel(Group, GroupMember, PermissionLevel) to change the permission level of a member
     */
    default @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player) {
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
    @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player, PermissionLevel level);

    /**
     * Gets a group member from a group
     *
     * @param group  the group to get the member from
     * @param player the player to get the member from
     * @return the group member or null if not found
     */
    @Nullable GroupMember getGroupMember(Group group, OfflinePlayer player);

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
    default boolean addPlayerToClaim(Claim claim, OfflinePlayer player) {
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
    default boolean removePlayerFromClaim(Claim claim, OfflinePlayer player) {
        return removeGroupFromClaim(claim, getPlayerGroup(player));
    }

    /**
     * Create a new group
     *
     * @param player the group to create
     * @param name   the name of the group
     * @return the new group or null if failed
     */
    @Nullable Group createGroup(OfflinePlayer player, String name);

    /**
     * Delete a group
     *
     * @param group the group to delete
     * @return true if successful, false otherwise
     */
    boolean deleteGroup(Group group);

    /**
     * Rename a group
     *
     * @param group the group to rename
     * @param name  the new name of the group
     * @return true if successful, false otherwise
     */
    boolean renameGroup(Group group, String name);


    /**
     * Sets if all players can interact with a block in a claim
     *
     * @param claim    the claim to set the interactable state
     * @param material the material of the interactable block
     * @param state    the state if all players can interact with the block
     */
    void setBlockInteractable(Claim claim, Material material, boolean state);

    /**
     * Sets if all players can interact with an entity in a claim
     *
     * @param claim      the claim to set the interactable state
     * @param entityType the entity type of the interactable entity
     * @param damage     if all players can damage the entity
     * @param interact   if all players can interact with the entity
     */
    void setEntityInteractable(Claim claim, EntityType entityType, boolean damage, boolean interact);

    /**
     * Removes the interactive state from a block
     *
     * @param claim    the claim to remove the interactable state
     * @param material the material of the interactable block
     */
    void removeBlockInteractable(Claim claim, Material material);

    /**
     * Removes the interactive states from an entity
     *
     * @param claim      the claim to remove the interactable state
     * @param entityType the entity type of the interactable entity
     */
    void removeEntityInteractable(Claim claim, EntityType entityType);


    /**
     * Sets the owner of a chunk
     *
     * @param chunk  the chunk to set the owner
     * @param target the new owner of the chunk
     */
    void setOwner(Chunk chunk, Group target);

}
