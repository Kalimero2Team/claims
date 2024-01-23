package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.event.ChunkClaimedEvent;
import com.kalimero2.team.claims.api.event.ChunkUnclaimedEvent;
import com.kalimero2.team.claims.api.event.ClaimOwnerChangeEvent;
import com.kalimero2.team.claims.api.event.flag.FlagSetEvent;
import com.kalimero2.team.claims.api.event.flag.FlagUnsetEvent;
import com.kalimero2.team.claims.api.event.group.GroupAddMemberEvent;
import com.kalimero2.team.claims.api.event.group.GroupMemberPermissionLevelChangeEvent;
import com.kalimero2.team.claims.api.event.group.GroupRemoveMemberEvent;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.command.ChunkAdminCommands;
import com.kalimero2.team.claims.paper.storage.Storage;
import com.kalimero2.team.claims.paper.storage.StoredClaim;
import com.kalimero2.team.claims.paper.storage.StoredEntityInteractable;
import com.kalimero2.team.claims.paper.storage.StoredGroup;
import com.kalimero2.team.claims.paper.storage.StoredGroupMember;
import com.kalimero2.team.claims.paper.storage.StoredMaterialInteractable;
import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class ClaimManager implements ClaimsApi, Listener {

    private final Storage storage;
    private final PaperClaims plugin;
    private final HashMap<NamespacedKey, Flag> registeredFlags = new HashMap<>();
    private final HashMap<Chunk, Claim> loadedClaims = new HashMap<>();
    private final HashMap<Integer, StoredGroup> loadedGroups = new HashMap<>();


    public ClaimManager(PaperClaims plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.storage = new Storage(plugin, this, new File(plugin.getDataFolder() + "/" + plugin.getConfig().getString("database")));;
        this.plugin = plugin;

        storage.getGroups().forEach(group -> {
            loadedGroups.put(group.getId(), StoredGroup.cast(group));
        });
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
        if (flag == null) throw new IllegalArgumentException("Flag cannot be null");
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag (" + flag.getKeyString() + ") is not registered");
        }
        return Objects.requireNonNullElseGet(claim.getFlags().get(flag), flag::getDefaultState);
    }

    @Override
    public boolean setFlagState(Claim claim, Flag flag, boolean state) {
        if (flag == null) throw new IllegalArgumentException("Flag cannot be null");
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag (" + flag.getKeyString() + ") is not registered");
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
        if (flag == null) throw new IllegalArgumentException("Flag cannot be null");
        if (!registeredFlags.containsValue(flag)) {
            throw new IllegalArgumentException("Flag (" + flag.getKeyString() + ") is not registered");
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
    @SuppressWarnings("removal")
    public List<Claim> getClaims(World world) {
        return storage.getClaims(world);
    }

    @Override
    public List<Claim> getClaims(Group group) {
        List<Claim> claims = storage.getClaims(group);
        // Replace all claims with loaded claims if they are loaded
        claims.replaceAll(claim -> loadedClaims.containsValue(claim) ? loadedClaims.get(claim.getChunk()) : claim);
        return claims;
    }

    @Override
    public int getClaimAmount(Group group) {
        return storage.getClaimAmount(group);
    }

    @Override
    public List<Group> getGroups(OfflinePlayer player, PermissionLevel permissionLevel) {
        return loadedGroups.values().stream().filter(
                group -> group.getMembers().stream().anyMatch(
                        member -> member.getPlayer().equals(player) && member.getPermissionLevel().isHigherOrEqual(permissionLevel))
        ).collect(Collectors.toList());
    }

    @Override
    public @Nullable Group getGroup(int id) {
        return loadedGroups.get(id);
    }

    @Override
    public @Nullable Group getGroup(String name) {
        return loadedGroups.values().stream().filter(group -> group.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(loadedGroups.values());
    }

    @Override
    public Group getPlayerGroup(OfflinePlayer player) {
        StoredGroup storedGroup = loadedGroups.values().stream().filter(group -> group.isPlayer() && player.equals(getGroupMember(group, player))).findFirst().orElse(null);

        if (storedGroup != null) {
            if (player.getName() != null && !storedGroup.getName().equals(player.getName())) {
                if (renameGroup(storedGroup, player.getName())) {
                    plugin.getLogger().info("Renamed group " + storedGroup.getName() + " to " + player.getName());
                }else {
                    plugin.getLogger().severe("Could not rename group " + storedGroup.getName() + " to " + player.getName());
                }
            }
            return storedGroup;
        }

        Group playerGroup = storage.getPlayerGroup(player);
        if (playerGroup == null) {
            if (storage.createPlayerGroup(player, plugin.getConfig().getInt("claims.max-claims"))) {
                playerGroup = storage.getPlayerGroup(player);
            } else {
                throw new IllegalStateException("Could not create player group");
            }
        }
        loadedGroups.put(playerGroup.getId(), StoredGroup.cast(playerGroup));
        return playerGroup;
    }

    @Override
    public void setMaxClaims(Group group, int max) {
        loadedGroups.get(group.getId()).setMaxClaims(max);
        StoredGroup.cast(group).setMaxClaims(max);
        refreshGroupInLoadedClaims(group);
    }

    @Override
    public boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level) {
        boolean success = storage.setPermissionLevel(group, member, level);

        if (success) {
            GroupMemberPermissionLevelChangeEvent groupMemberPermissionLevelChangeEvent = new GroupMemberPermissionLevelChangeEvent(group, member, member.getPermissionLevel(), level);
            groupMemberPermissionLevelChangeEvent.callEvent();

            StoredGroupMember storedGroupMember = StoredGroupMember.cast(member);
            storedGroupMember.setPermissionLevel(level);

            StoredGroup.cast(group).removeMember(member);
            StoredGroup.cast(group).addMember(storedGroupMember);

            refreshGroupInLoadedClaims(group);
        }
        return success;
    }

    @Override
    public @Nullable GroupMember addGroupMember(Group group, OfflinePlayer player, PermissionLevel level) {
        if (storage.addGroupMember(group, player, level)) {
            GroupMember groupMember = storage.getGroupMember(group, player);
            StoredGroup.cast(group).addMember(groupMember);
            refreshGroupInLoadedClaims(group);
            GroupAddMemberEvent groupAddMemberEvent = new GroupAddMemberEvent(group, groupMember);
            groupAddMemberEvent.callEvent();
            return groupMember;
        }
        return null;
    }

    private void refreshGroupInLoadedClaims(Group group) {
        // TODO: This is an ugly hack, and should be removed ...
        loadedClaims.values().forEach(claim -> {
            if (claim != null) {
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

        if (success) {
            StoredGroup.cast(group).removeMember(member);
            refreshGroupInLoadedClaims(group);
            GroupRemoveMemberEvent groupRemoveMemberEvent = new GroupRemoveMemberEvent(group, member);
            groupRemoveMemberEvent.callEvent();
        }

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
        } else {
            return storage.addGroupToClaim(claim, group);
        }
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
        } else {
            return storage.removeGroupFromClaim(claim, group);
        }
    }

    @Override
    public @Nullable Group createGroup(OfflinePlayer owner, String name) {
        boolean created = storage.createGroup(name, 0, false);
        if (created) {
            Group group = storage.getGroup(name);
            if (group == null) {
                throw new IllegalStateException("Could not create group");
            }
            loadedGroups.put(group.getId(), StoredGroup.cast(group));
            return group;
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

        boolean deleted = storage.deleteGroup(group);
        if (deleted) {
            loadedGroups.remove(group.getId());
        } else {
            plugin.getLogger().severe("Could not delete group " + group.getName());
        }
        return deleted;
    }

    @Override
    public boolean renameGroup(Group group, String name) {
        boolean isPlayer = group.isPlayer();

        Optional<StoredGroup> groupWithNameExists = loadedGroups.values().stream().filter(
                group1 -> group1.getName().equals(name) && group1.isPlayer() == isPlayer
        ).findAny();

        if (groupWithNameExists.isPresent()) {
            return false;
        }

        loadedGroups.get(group.getId()).setName(name);
        return true;
    }

    @Override
    public void setBlockInteractable(Claim claim, Material material, boolean state) {
        if (loadedClaims.containsValue(claim)) {
            MaterialInteractable interactable = StoredClaim.cast(claim).getMaterialInteractables().stream().filter(materialInteractable -> materialInteractable.getBlockMaterial().equals(material)).findFirst().orElse(null);
            if (interactable != null) {
                StoredMaterialInteractable.cast(interactable).setInteractable(state);
            } else {
                StoredClaim.cast(claim).addMaterialInteractable(new StoredMaterialInteractable(material, state));
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
            StoredClaim.cast(claim).getMaterialInteractables().stream().filter(materialInteractable -> materialInteractable.getBlockMaterial().equals(material)).findFirst().ifPresent(interactable -> StoredClaim.cast(claim).removeMaterialInteractable(interactable));
        } else {
            storage.removeBlockInteractable(claim, material);
        }
    }

    @Override
    public void removeEntityInteractable(Claim claim, EntityType entityType) {
        if (loadedClaims.containsValue(claim)) {
            StoredClaim.cast(claim).getEntityInteractables().stream().filter(entityInteractable -> entityInteractable.getEntityType().equals(entityType)).findFirst().ifPresent(interactable -> StoredClaim.cast(claim).removeEntityInteractable(interactable));
        } else {
            storage.removeEntityInteractable(claim, entityType);
        }
    }

    @Override
    public void setOwner(Chunk chunk, Group target) {
        Claim claim = getClaim(chunk);
        if (claim != null) {
            Group owner = claim.getOwner();
            new ClaimOwnerChangeEvent(chunk, owner, target, claim).callEvent();
        }

        if (loadedClaims.containsKey(chunk)) {
            StoredClaim.cast(loadedClaims.get(chunk)).setOwner(target);
        } else {
            storage.setOwner(chunk, target);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Group playerGroup = getPlayerGroup(event.getPlayer());
        if (playerGroup == null) {
            event.getPlayer().kick(Component.text("Could not create player group. Please contact an administrator"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getGroups(event.getPlayer()).forEach(group -> {
            storage.updateLastSeen(group);
            refreshGroupInLoadedClaims(group);
        });
        ChunkAdminCommands.forcedPlayers.remove(event.getPlayer().getUniqueId());
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
                boolean saved = storage.saveClaim(claim);
                if (!saved) {
                    plugin.getLogger().severe("Could not save claim " + claim.getChunk().getX() + " " + claim.getChunk().getZ());
                }
            }
            loadedClaims.remove(event.getChunk());
        }
    }

    public void shutdown() {
        long startTime = System.nanoTime();
        plugin.getLogger().info("Saving all claims... (This may take a while)");
        for (Claim claim : loadedClaims.values()) {
            if (claim != null) {
                boolean saved = storage.saveClaim(claim);
                if (!saved) {
                    plugin.getLogger().severe("Could not save claim " + claim.getChunk().getX() + " " + claim.getChunk().getZ());
                }
            }
        }
        loadedClaims.clear();
        plugin.getLogger().info("Saved all claims in " + (System.nanoTime() - startTime) / 1000000 + "ms");
        startTime = System.nanoTime();
        plugin.getLogger().info("Saving all groups...");
        for (Group group : loadedGroups.values()) {
            if (group != null) {
                boolean saved = storage.saveGroup(group);
                if (!saved) {
                    plugin.getLogger().severe("Could not save group " + group.getName());
                }
            }
        }
        loadedGroups.clear();
        plugin.getLogger().info("Saved all groups in " + (System.nanoTime() - startTime) / 1000000 + "ms");
        storage.shutdown();

    }

    public HashMap<Chunk, Claim> getLoadedClaims() {
        return loadedClaims;
    }
}
