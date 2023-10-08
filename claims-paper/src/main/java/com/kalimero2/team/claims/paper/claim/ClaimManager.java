package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.storage.Storage;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ClaimManager implements ClaimsApi, Listener {

    private final Storage storage;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();
    private final HashMap<Chunk, Claim> claimCache = new HashMap<>();

    public ClaimManager(PaperClaims plugin, Storage storage) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        } else {
            Claim claimData = storage.getClaimData(chunk);
            if (claimData != null) {
                claimCache.put(chunk, claimData);
                return claimData;
            }
        }
        return null;
    }

    @Override
    public List<Claim> getClaims(Group group) {
        return new ArrayList<>();
    }

    @Override
    public List<Group> getGroups(OfflinePlayer player) {
        return null;
    }

    @Override
    public Group getPlayerGroup(OfflinePlayer player) {
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
    public @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player, PermissionLevel level) {
        return null;
    }

    @Override
    public @Nullable GroupMember getGroupMember(Group group, OfflinePlayer player) {
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


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Group playerGroup = storage.getPlayerGroup(event.getPlayer());
        if (playerGroup == null) {
            if (storage.createGroup(event.getPlayer().getName(), 8, true)) {
                playerGroup = storage.getPlayerGroup(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Claim claimData = storage.getClaimData(event.getChunk());
        if (claimData != null) {
            claimCache.put(event.getChunk(), claimData);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        claimCache.remove(event.getChunk());
    }
}
