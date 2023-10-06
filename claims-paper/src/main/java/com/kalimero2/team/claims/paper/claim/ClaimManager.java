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
        return flag.getDefaultState();
    }

    @Override
    public void setFlagState(Claim claim, Flag flag, boolean state) {

    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) {
        if (claimCache.containsKey(chunk)) {
            return claimCache.get(chunk);
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
    public boolean setPermissionLevel(GroupMember member, PermissionLevel level) {
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
}
