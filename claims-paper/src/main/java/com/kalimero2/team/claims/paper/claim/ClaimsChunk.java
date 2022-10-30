package com.kalimero2.team.claims.paper.claim;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.util.ExtraPlayerData;
import com.kalimero2.team.claims.paper.util.MoreDataTypes;
import com.kalimero2.team.claims.paper.util.SerializableChunk;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimsChunk extends com.kalimero2.team.claims.api.ClaimsChunk {

    private static final Map<Chunk, ClaimsChunk> cachedChunks = new HashMap<>();

    private final Chunk chunk;
    private Map<String, String> properties;
    private List<Material> ignoredInteractableMaterials;

    private ClaimsChunk(Chunk chunk) {
        super(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        this.chunk = chunk;
    }

    public static ClaimsChunk of(Chunk chunk) {
        if(!cachedChunks.containsKey(chunk)){
            cachedChunks.put(chunk, new ClaimsChunk(chunk));
        }
        return cachedChunks.get(chunk);
    }

    public static void removeFromCache(Chunk chunk){
        cachedChunks.remove(chunk);
    }

    @Override
    public boolean isClaimed() {
        return Boolean.TRUE.equals(chunk.getPersistentDataContainer().get(getKey("claimed"), DataType.BOOLEAN));
    }

    @Override
    public boolean hasOwner() {
        return chunk.getPersistentDataContainer().has(getKey("owner"), DataType.UUID);
    }

    @Override
    public UUID getOwner() {
        if(hasOwner()){
            return chunk.getPersistentDataContainer().get(getKey("owner"), DataType.UUID);
        }
        return null;
    }

    @Override
    @SuppressWarnings("removal")
    public UUID[] getTrusted() {
        return getTrustedList().toArray(new UUID[0]);
    }

    @Override
    public List<UUID> getTrustedList() {
        // Migration from UUID[] to List<UUID>
        NamespacedKey namespacedKey = getKey("trusted");
        if(chunk.getPersistentDataContainer().has(namespacedKey, MoreDataTypes.UUID_ARRAY)){
            UUID[] old_trusted_array = chunk.getPersistentDataContainer().get(namespacedKey, MoreDataTypes.UUID_ARRAY);
            if (old_trusted_array != null) {
                List<UUID> trusted_list = new ArrayList<>(Arrays.asList(old_trusted_array));
                trusted_list.remove(null);
                chunk.getPersistentDataContainer().remove(namespacedKey);
                setTrusted(trusted_list);
            }
        }
        // End of migration

        List<UUID> trusted = chunk.getPersistentDataContainer().get(namespacedKey, MoreDataTypes.UUID_LIST);
        if(trusted == null){
            trusted = new ArrayList<>();
        }
        trusted.remove(null); // Prevent NPEs
        return new ArrayList<>(trusted);
    }

    @Override
    public Map<String, String> getProperties() {
        if(this.properties == null){
            if(chunk.getPersistentDataContainer().has(getKey("properties"), MoreDataTypes.CHUNK_PROPERTY_MAP)){
                this.properties = chunk.getPersistentDataContainer().get(getKey("properties"), MoreDataTypes.CHUNK_PROPERTY_MAP);
            }else{
                this.properties = new HashMap<>();
            }
        }
        return this.properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        if(properties == null) {
            this.properties = new HashMap<>();
            chunk.getPersistentDataContainer().remove(getKey("properties"));
        }else{
            this.properties = properties;
            chunk.getPersistentDataContainer().set(getKey("properties"), MoreDataTypes.CHUNK_PROPERTY_MAP, properties);
        }
    }

    @Override
    public void setOwner(UUID owner) {
        if(owner == null){
            if(hasOwner()){
                ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(getOwner());
                extraPlayerData.chunks.remove(SerializableChunk.fromBukkitChunk(getBukkitChunk()));
                ClaimManager.setExtraPlayerData(getOwner(), extraPlayerData);
            }
            chunk.getPersistentDataContainer().remove(getKey("owner"));
        }else{
            if(hasOwner()){
                ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(getOwner());
                extraPlayerData.chunks.remove(SerializableChunk.fromBukkitChunk(getBukkitChunk()));
                ClaimManager.setExtraPlayerData(getOwner(), extraPlayerData);
            }
            chunk.getPersistentDataContainer().set(getKey("owner"), DataType.UUID, owner);
            if(hasOwner()){
                ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(owner);
                extraPlayerData.chunks.add(SerializableChunk.fromBukkitChunk(getBukkitChunk()));
                ClaimManager.setExtraPlayerData(getOwner(), extraPlayerData);
            }
        }
    }

    @Override
    public void setTrusted(List<UUID> trusted) {
        if(trusted == null) {
            chunk.getPersistentDataContainer().remove(getKey("trusted"));
        }else {
            chunk.getPersistentDataContainer().set(getKey("trusted"), MoreDataTypes.UUID_LIST, trusted);
        }
    }

    @Override
    public void setClaimed(boolean claimed) {
        chunk.getPersistentDataContainer().set(getKey("claimed"), DataType.BOOLEAN, claimed);
    }

    @Override
    public String getProperty(String key) {
        return getProperties().get(key);
    }

    @Override
    public void addProperty(String key, String value) {
        Map<String, String> properties = getProperties();
        properties.put(key, value);
        setProperties(properties);
    }

    @Override
    public void removeProperty(String key) {
        Map<String, String> properties = getProperties();
        properties.remove(key);
        setProperties(properties);
    }

    @Override
    public boolean isTrusted(UUID uuid) {
        return getTrustedList().contains(uuid);
    }

    @Override
    public void addTrusted(UUID trusted) {
        List<UUID> trustedList = getTrustedList();
        trustedList.add(trusted);
        setTrusted(trustedList);
    }

    @Override
    public void removeTrusted(UUID trusted) {
        List<UUID> trustedList = getTrustedList();
        trustedList.remove(trusted);
        setTrusted(trustedList);
    }

    @Override
    public void clearTrusted() {
        setTrusted(null);
    }

    @Override
    public boolean shouldIgnoreInteractable(String material) {
        return shouldIgnoreInteractable(Material.getMaterial(material));
    }

    public boolean shouldIgnoreInteractable(Material material){
        return getIgnoredInteractableBukkitMaterials().contains(material);
    }

    @Override
    public void setIgnoredInteractableMaterials(List<String> ignoredInteractableMaterials) {
        List<Material> materials = new ArrayList<>();
        for(String material : ignoredInteractableMaterials){
            materials.add(Material.getMaterial(material));
        }
        setIgnoredInteractableBukkitMaterials(materials);
    }

    public void setIgnoredInteractableBukkitMaterials(List<Material> ignoredInteractableMaterials) {
        this.ignoredInteractableMaterials = ignoredInteractableMaterials;
        chunk.getPersistentDataContainer().set(getKey("ignored_interactable_materials"), MoreDataTypes.MATERIAL_LIST, ignoredInteractableMaterials);
    }

    @Override
    public List<String> getIgnoredInteractableMaterials() {
        List<String> materials = new ArrayList<>();
        for(Material material : getIgnoredInteractableBukkitMaterials()){
            materials.add(material.name());
        }
        return materials;
    }

    public List<Material> getIgnoredInteractableBukkitMaterials(){
        if(ignoredInteractableMaterials == null){
            if(chunk.getPersistentDataContainer().has(getKey("ignored_interactable_materials"), MoreDataTypes.MATERIAL_LIST)){
                ignoredInteractableMaterials = chunk.getPersistentDataContainer().get(getKey("ignored_interactable_materials"), MoreDataTypes.MATERIAL_LIST);
            }else{
                ignoredInteractableMaterials = new ArrayList<>();
            }
        }
        return ignoredInteractableMaterials;
    }

    @Override
    public void addIgnoredInteractable(String material) {
        addIgnoredInteractable(Material.getMaterial(material));
    }

    public void addIgnoredInteractable(Material material) {
        List<Material> materials = getIgnoredInteractableBukkitMaterials();
        materials.add(material);
        setIgnoredInteractableBukkitMaterials(materials);
    }

    @Override
    public void removeIgnoredInteractable(String material) {
        removeIgnoredInteractable(Material.getMaterial(material));
    }

    public void removeIgnoredInteractable(Material material) {
        List<Material> materials = getIgnoredInteractableBukkitMaterials();
        materials.remove(material);
        setIgnoredInteractableBukkitMaterials(materials);
    }

    @Override
    public void clearIgnoredInteractables() {
        setIgnoredInteractableMaterials(new ArrayList<>());
    }

    public Chunk getBukkitChunk() {
        return chunk;
    }

    private NamespacedKey getKey(String name) {
        return new NamespacedKey(PaperClaims.plugin, name);
    }
}
