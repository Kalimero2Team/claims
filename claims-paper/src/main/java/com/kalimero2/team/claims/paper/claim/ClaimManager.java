package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.storage.Storage;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;


public class ClaimManager implements ClaimsApi {

    private final Storage storage;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();
    private final HashMap<Chunk, Claim> claimCache = new HashMap<>();

    public ClaimManager(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void registerFlag(Flag flag) {
        registeredFlags.put(flag.getKey(), flag);
    }

    @Override
    public void unregisterFlag(Flag flag) {
        registeredFlags.remove(flag.getKey());
    }

    @Override
    public Flag getFlag(NamespacedKey key) {
        return registeredFlags.get(key);
    }

    @Override
    public boolean getFlagState(Claim claim, Flag flag) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }
        // TODO: implement
        return flag.getDefaultState();
    }

    @Override
    public void setFlagState(Claim claim, Flag flag, boolean state) {

    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) {
        if (claimCache.containsKey(chunk)) {
            return claimCache.get(chunk);
        }else {
            Claim claimData = storage.getClaimData(chunk);
            if (claimData != null) {
                claimCache.put(chunk, claimData);
                return claimData;
            }
        }
        return null;
    }

    @Override
    public List<Group> getGroups(Player player) {
        return null;
    }

    @Override
    public Group getPlayerGroup(Player player) {
        return null;
    }

    @Override
    public void setMaxClaims(Group group, int max) {

    }

    @Override
    public boolean setPermissionLevel(GroupMember member, PermissionLevel level) {
        return false;
    }

    @Override
    public @Nullable GroupMember addGroupMember(Group group, Player player, PermissionLevel level) {
        return null;
    }

    @Override
    public @Nullable GroupMember getGroupMember(Group group, Player player) {
        return null;
    }

    @Override
    public boolean removeGroupMember(Group group, GroupMember member) {
        return false;
    }

    @Override
    public boolean claimChunk(Chunk chunk, Group group) {
        return false;
    }

    @Override
    public boolean unclaimChunk(Chunk chunk) {
        return false;
    }

    @Override
    public boolean addGroupToClaim(Claim claim, Group group) {
        return false;
    }

    @Override
    public boolean removeGroupFromClaim(Claim claim, Group group) {
        return false;
    }
}
