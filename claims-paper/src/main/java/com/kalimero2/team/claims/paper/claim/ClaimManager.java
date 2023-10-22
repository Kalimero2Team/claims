package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.event.ChunkClaimedEvent;
import com.kalimero2.team.claims.api.event.ChunkUnclaimedEvent;
import com.kalimero2.team.claims.api.event.flag.FlagSetEvent;
import com.kalimero2.team.claims.api.event.flag.FlagUnsetEvent;
import com.kalimero2.team.claims.api.event.group.GroupMemberPermissionLevelChangeEvent;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.storage.Storage;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;


public class ClaimManager implements ClaimsApi, Listener {

    private final Storage storage;
    private final PaperClaims plugin;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();
    private final HashMap<Integer, Claim> loadedClaims = new HashMap<>();


    public ClaimManager(PaperClaims plugin, Storage storage) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.storage = storage;
        this.plugin = plugin;
    }

    @Override
    public void registerFlag(Flag flag) {
        plugin.getLogger().info("Registering flag " + flag.getKey());
        registeredFlags.put(flag.getKey(), flag);
    }

    @Override
    public void unregisterFlag(Flag flag) {
        registeredFlags.remove(flag.getKey());
    }

    @Override
    public List<Flag> getFlags() {
        return List.copyOf(registeredFlags.values());
    }

    @Override
    public List<Flag> getFlags(Claim claim) {
        return storage.getFlags(claim);
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
    public boolean setFlagState(Claim claim, Flag flag, boolean state) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }

        FlagSetEvent event = new FlagSetEvent(claim, flag, state);
        if (event.callEvent()) {
            storage.setFlagState(claim, flag, state);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unsetFlagState(Claim claim, Flag flag) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }
        FlagUnsetEvent event = new FlagUnsetEvent(claim, flag);
        if (event.callEvent()) {
            storage.unsetFlagState(claim, flag);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) {
        return storage.getClaimData(chunk);
    }

    @Override
    public List<Claim> getClaims(World world) {
        return storage.getClaims(world);
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
    public @Nullable Group getGroup(String name) {
        return storage.getGroup(name);
    }

    @Override
    public List<Group> getGroups() {
        return storage.getGroups();
    }

    @Override
    public Group getPlayerGroup(OfflinePlayer player) {
        Group playerGroup = storage.getPlayerGroup(player);
        if (playerGroup == null) {
            if (storage.createPlayerGroup(player, plugin.getConfig().getInt("claims.max_claims"))) {
                playerGroup = storage.getPlayerGroup(player);
                storage.addGroupMember(playerGroup, player, PermissionLevel.OWNER);
            } else {
                throw new IllegalStateException("Could not create player group");
            }
        }
        return playerGroup;
    }

    @Override
    public void setMaxClaims(Group group, int max) {
        storage.setMaxClaims(group, max);
    }

    @Override
    public boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level) {
        GroupMemberPermissionLevelChangeEvent groupMemberPermissionLevelChangeEvent = new GroupMemberPermissionLevelChangeEvent(group, member, member.getPermissionLevel(), level);

        if(groupMemberPermissionLevelChangeEvent.callEvent()){
            return storage.setPermissionLevel(group, member, level);
        }
        return false;
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
        return group.getMembers().stream().filter(member -> member.getPlayer().equals(player)).findFirst().orElse(null);
    }

    @Override
    public boolean removeGroupMember(Group group, GroupMember member) {
        return storage.removeGroupMember(group, member);
    }

    @Override
    public boolean claimChunk(Chunk chunk, Group group) {
        boolean success = storage.claimChunk(chunk, group);

        if (success) {
            Claim claim = getClaim(chunk);
            ChunkClaimedEvent event = new ChunkClaimedEvent(chunk, group, claim);

            event.callEvent();
        }

        return success;

    }

    @Override
    public boolean unclaimChunk(Chunk chunk) {
        Claim claim = getClaim(chunk);
        if (claim != null) {
            boolean unclaimed = storage.unclaimChunk(claim);
            if (unclaimed) {
                ChunkUnclaimedEvent event = new ChunkUnclaimedEvent(chunk, claim.getOwner());
                event.callEvent();
            }
            return unclaimed;
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

    @Override
    public @Nullable Group createGroup(OfflinePlayer owner, String name) {
        boolean group = storage.createGroup(name, 0, false);
        if (group) {
            return storage.getGroup(name);
        }
        return null;
    }

    @Override
    public boolean deleteGroup(Group group) {
        List<Claim> claims = getClaims(group);
        for (Claim claim : claims) {
            unclaimChunk(claim.getChunk());
            plugin.getLogger().info("Unclaimed chunk " + claim.getChunk().getX() + " " + claim.getChunk().getZ() + " because group " + group.getName() + " was deleted");
        }

        return storage.deleteGroup(group);
    }

    @Override
    public boolean renameGroup(Group group, String name) {
        return storage.renameGroup(group, name);
    }

    @Override
    public void setBlockInteractable(Claim claim, Material material, boolean state) {
        storage.setBlockInteractable(claim, material, state);
    }

    @Override
    public void setEntityInteractable(Claim claim, EntityType entityType, boolean damage, boolean interact) {
        storage.setEntityInteractable(claim, entityType, damage, interact);
    }

    @Override
    public void removeBlockInteractable(Claim claim, Material material) {
        storage.removeBlockInteractable(claim, material);
    }

    @Override
    public void removeEntityInteractable(Claim claim, EntityType entityType) {
        storage.removeEntityInteractable(claim, entityType);
    }

    @Override
    public void setOwner(Chunk chunk, Group target) {
        storage.setOwner(chunk, target);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Group playerGroup = storage.getPlayerGroup(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getGroups(event.getPlayer()).forEach(storage::updateLastSeen);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Claim claimData = storage.getClaimData(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

    }
}
