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

import java.util.HashMap;
import java.util.List;


public class ClaimManager implements ClaimsApi, Listener {

    private final Storage storage;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();

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

        return storage.getFlagState(claim, flag);
    }

    @Override
    public void setFlagState(Claim claim, Flag flag, boolean state) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }

        storage.setFlagState(claim, flag, state);
    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) {
        return storage.getClaimData(chunk);
    }

    @Override
    public List<Claim> getClaims(Group group) {
        return storage.getClaims(group);
    }

    @Override
    public List<Group> getGroups(OfflinePlayer player) {
        return storage.getGroups(player);
    }

    @Override
    public @Nullable Group getGroup(int id) {
        return storage.getGroup(id);
    }

    @Override
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @Override
    public Group getPlayerGroup(OfflinePlayer player) {
        Group playerGroup = storage.getPlayerGroup(player);
        if (playerGroup == null) {
            if (storage.createPlayerGroup(player, 8)) {
                playerGroup = storage.getPlayerGroup(player);
                storage.addGroupMember(playerGroup, player, PermissionLevel.OWNER);
            }else {
                throw new IllegalStateException("Could not create player group");
            }
        }
        if(playerGroup == null){
            throw new IllegalStateException("Could not create player group");
        }
        return playerGroup;
    }

    @Override
    public void setMaxClaims(Group group, int max) {
        storage.setMaxClaims(group, max);
    }

    @Override
    public boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level) {
        return storage.setPermissionLevel(group, member, level);
    }

    @Override
    public @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player, PermissionLevel level) {
        if (storage.addGroupMember(group, player, level)) {
            return storage.getGroupMember(group, player);
        }
        return null;
    }

    @Override
    public @Nullable GroupMember getGroupMember(Group group, OfflinePlayer player) {
        return storage.getGroupMember(group, player);
    }

    @Override
    public boolean removeGroupMember(Group group, GroupMember member) {
        return storage.removeGroupMember(group, member);
    }

    @Override
    public boolean claimChunk(Chunk chunk, Group group) {
        return storage.claimChunk(chunk, group);
    }

    @Override
    public boolean unclaimChunk(Chunk chunk) {
        Claim claim = getClaim(chunk);
        if (claim != null) {
            return storage.unclaimChunk(claim);
        }
        return false;
    }

    @Override
    public boolean addGroupToClaim(Claim claim, Group group) {
        return storage.addGroupToClaim(claim, group);
    }

    @Override
    public boolean removeGroupFromClaim(Claim claim, Group group) {
        return storage.removeGroupFromClaim(claim, group);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Group playerGroup = storage.getPlayerGroup(event.getPlayer());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Claim claimData = storage.getClaimData(event.getChunk());

    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
    }
}
