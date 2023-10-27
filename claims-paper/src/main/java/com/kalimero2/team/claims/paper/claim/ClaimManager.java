package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.event.ChunkClaimedEvent;
import com.kalimero2.team.claims.api.event.ChunkUnclaimedEvent;
import com.kalimero2.team.claims.api.event.ClaimOwnerChangeEvent;
import com.kalimero2.team.claims.api.event.flag.FlagSetEvent;
import com.kalimero2.team.claims.api.event.flag.FlagUnsetEvent;
import com.kalimero2.team.claims.api.event.group.GroupMemberPermissionLevelChangeEvent;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.storage.Storage;
import com.kalimero2.team.claims.paper.storage.StoredBlockInteractable;
import com.kalimero2.team.claims.paper.storage.StoredClaim;
import com.kalimero2.team.claims.paper.storage.StoredEntityInteractable;
import com.kalimero2.team.claims.paper.storage.StoredGroup;
import com.kalimero2.team.claims.paper.storage.StoredGroupMember;
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
import java.util.Objects;


public class ClaimManager implements ClaimsApi, Listener {

    private final Storage storage;
    private final PaperClaims plugin;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();
    private final HashMap<Chunk, Claim> loadedClaims = new HashMap<>();


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
    public Flag getFlag(NamespacedKey key) {
        return registeredFlags.get(key);
    }

    @Override
    public boolean getFlagState(Claim claim, Flag flag) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }
        return Objects.requireNonNullElseGet(claim.getFlags().get(flag), flag::getDefaultState);
    }

    @Override
    public boolean setFlagState(Claim claim, Flag flag, boolean state) {
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag is not registered");
        }

        FlagSetEvent event = new FlagSetEvent(claim, flag, state);
        if (event.callEvent()) {
            if (loadedClaims.containsValue(claim)) {
                StoredClaim.cast(claim).setFlag(flag, state);
            } else {
                storage.setFlagState(claim, flag, state);
            }
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
            if (loadedClaims.containsValue(claim)) {
                StoredClaim.cast(claim).removeFlag(flag);
            } else {
                storage.unsetFlagState(claim, flag);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable Claim getClaim(Chunk chunk) {
        if (loadedClaims.containsKey(chunk)) {
            return loadedClaims.get(chunk);
        }
        Claim claimData = storage.getClaimData(chunk);
        loadedClaims.put(chunk, claimData);
        return claimData;
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
            if (storage.createPlayerGroup(player, plugin.getConfig().getInt("claims.max-claims"))) {
                return storage.getPlayerGroup(player);
            } else {
                throw new IllegalStateException("Could not create player group");
            }
        }
        return playerGroup;
    }

    @Override
    public void setMaxClaims(Group group, int max) {
        storage.setMaxClaims(group, max);
        StoredGroup.cast(group).setMaxClaims(max);
        refreshGroupInLoadedClaims(group);
    }

    @Override
    public boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level) {
        GroupMemberPermissionLevelChangeEvent groupMemberPermissionLevelChangeEvent = new GroupMemberPermissionLevelChangeEvent(group, member, member.getPermissionLevel(), level);

        if (groupMemberPermissionLevelChangeEvent.callEvent()) {
            boolean success = storage.setPermissionLevel(group, member, level);
            StoredGroupMember storedGroupMember = StoredGroupMember.cast(member);
            storedGroupMember.setPermissionLevel(level);
            StoredGroup.cast(group).removeMember(member);
            StoredGroup.cast(group).addMember(storedGroupMember);
            refreshGroupInLoadedClaims(group);
            return success;
        }
        return false;
    }

    @Override
    public @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player, PermissionLevel level) {
        if (storage.addGroupMember(group, player, level)) {
            GroupMember groupMember = storage.getGroupMember(group, player);
            StoredGroup.cast(group).addMember(groupMember);
            refreshGroupInLoadedClaims(group);
            return groupMember;
        }
        return null;
    }

    private void refreshGroupInLoadedClaims(Group group) {
        loadedClaims.values().forEach(claim -> {
            if(claim != null){
                if (claim.getOwner().equals(group)) {
                    StoredClaim storedClaim = StoredClaim.cast(claim);
                    storedClaim.setOwner(group);
                }
                if (claim.getMembers().contains(group)) {
                    StoredClaim storedClaim = StoredClaim.cast(claim);
                    storedClaim.removeMember(group);
                    storedClaim.addMember(group);
                }
            }
        });
    }

    @Override
    public @Nullable GroupMember getGroupMember(Group group, OfflinePlayer player) {
        return group.getMembers().stream().filter(member -> member.getPlayer().equals(player)).findFirst().orElse(null);
    }

    @Override
    public boolean removeGroupMember(Group group, GroupMember member) {
        boolean success = storage.removeGroupMember(group, member);
        StoredGroup.cast(group).removeMember(member);
        refreshGroupInLoadedClaims(group);
        return success;
    }

    @Override
    public boolean claimChunk(Chunk chunk, Group group) {
        boolean success = storage.claimChunk(chunk, group);

        if (success) {
            loadedClaims.remove(chunk);
            Claim claim = getClaim(chunk);
            loadedClaims.put(chunk, claim);
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
                loadedClaims.remove(chunk);
                ChunkUnclaimedEvent event = new ChunkUnclaimedEvent(chunk, claim.getOwner());
                event.callEvent();
            }
            return unclaimed;
        }
        return false;
    }

    @Override
    public boolean addGroupToClaim(Claim claim, Group group) {
        if (loadedClaims.containsValue(claim)) {
            StoredClaim storedClaim = StoredClaim.cast(claim);
            if (storedClaim.getMembers().contains(group)) {
                return false;
            }
            storedClaim.addMember(group);
            return true;
        }
        return storage.addGroupToClaim(claim, group);
    }

    @Override
    public boolean removeGroupFromClaim(Claim claim, Group group) {
        if (loadedClaims.containsValue(claim)) {
            StoredClaim storedClaim = StoredClaim.cast(claim);
            if (!storedClaim.getMembers().contains(group)) {
                return false;
            }
            storedClaim.removeMember(group);
            return true;
        }
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
        if (loadedClaims.containsValue(claim)) {
            MaterialInteractable interactable = StoredClaim.cast(claim).getMaterialInteractables().stream().filter(materialInteractable -> materialInteractable.getBlockMaterial().equals(material)).findFirst().orElse(null);
            if (interactable != null) {
                StoredBlockInteractable.cast(interactable).setState(state);
            } else {
                StoredClaim.cast(claim).addMaterialInteractable(new StoredBlockInteractable(material, state));
            }
        } else {
            storage.setBlockInteractable(claim, material, state);
        }
    }

    @Override
    public void setEntityInteractable(Claim claim, EntityType entityType, boolean damage, boolean interact) {
        if (loadedClaims.containsValue(claim)) {
            EntityInteractable interactable = StoredClaim.cast(claim).getEntityInteractables().stream().filter(entityInteractable -> entityInteractable.getEntityType().equals(entityType)).findFirst().orElse(null);
            if (interactable != null) {
                StoredEntityInteractable.cast(interactable).setDamage(damage);
                StoredEntityInteractable.cast(interactable).setInteract(interact);
            } else {
                StoredClaim.cast(claim).addEntityInteractable(new StoredEntityInteractable(entityType, interact, damage));
            }
        } else {
            storage.setEntityInteractable(claim, entityType, damage, interact);
        }
    }

    @Override
    public void removeBlockInteractable(Claim claim, Material material) {
        if (loadedClaims.containsValue(claim)) {
            StoredClaim.cast(claim).getMaterialInteractables().stream()
                    .filter(materialInteractable -> materialInteractable.getBlockMaterial().equals(material))
                    .findFirst()
                    .ifPresent(
                            interactable -> StoredClaim.cast(claim).removeMaterialInteractable(interactable)
                    );
        } else {
            storage.removeBlockInteractable(claim, material);
        }
    }

    @Override
    public void removeEntityInteractable(Claim claim, EntityType entityType) {
        if (loadedClaims.containsValue(claim)) {
            StoredClaim.cast(claim).getEntityInteractables().stream()
                    .filter(entityInteractable -> entityInteractable.getEntityType().equals(entityType))
                    .findFirst()
                    .ifPresent(
                            interactable -> StoredClaim.cast(claim).removeEntityInteractable(interactable)
                    );
        } else {
            storage.removeEntityInteractable(claim, entityType);
        }
    }

    @Override
    public void setOwner(Chunk chunk, Group target) {
        Claim claim2 = getClaim(chunk);
        if(claim2 != null){
            Group owner = claim2.getOwner();
            new ClaimOwnerChangeEvent(chunk, owner, target, claim2).callEvent();
        }

        if (loadedClaims.containsKey(chunk)) {
            Claim claim = loadedClaims.get(chunk);
            if (claim != null) {
                StoredClaim.cast(claim).setOwner(target);
            }
        } else {
            storage.setOwner(chunk, target);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Group playerGroup = storage.getPlayerGroup(event.getPlayer());
        if (playerGroup == null) {
            storage.createPlayerGroup(event.getPlayer(), plugin.getConfig().getInt("claims.max-claims"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getGroups(event.getPlayer()).forEach(storage::updateLastSeen);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        loadedClaims.put(event.getChunk(), getClaim(event.getChunk()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (loadedClaims.containsKey(event.getChunk())) {
            Claim claim = loadedClaims.get(event.getChunk());
            if (claim != null) {
                storage.saveClaim(claim);
            }
            loadedClaims.remove(event.getChunk());
        }
    }

    public void shutdown() {
        for (Claim claim : loadedClaims.values()) {
            if (claim != null) {
                storage.saveClaim(claim);
            }
        }
        loadedClaims.clear();
        storage.shutdown();
    }
}
