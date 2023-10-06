package com.kalimero2.team.claims.api;

import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import org.bukkit.Chunk;
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
     * Gets a {@link Flag} by its id
     *
     * @param id the id of the flag
     * @return the flag or null if not found
     */
    Flag getFlag(int id);

    /**
     * Gets the flags of a claim
     *
     * @param claim the claim to get the flags from
     * @return the flags of the claim
     */
    List<Flag> getFlags(Claim claim);

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
     * Sets the permission level of a member
     * @param member the member to set the permission level from
     * @param level the permission level to set
     * @return true if successful, false otherwise
     * @see GroupMember#getPermissionLevel() to get the permission level
     */
    boolean setPermissionLevel(GroupMember member, PermissionLevel level);

    /**
     * Gets the permission level of a member
     * @param member the member to get the permission level from
     * @return the permission level of the member
     * @see Group
     */

}
