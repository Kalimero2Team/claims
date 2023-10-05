package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
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
        if (!cachedChunks.containsKey(chunk)) {
            cachedChunks.put(chunk, new ClaimsChunk(chunk));
        }
        return cachedChunks.get(chunk);
    }

    public static void removeFromCache(Chunk chunk) {
        cachedChunks.remove(chunk);
    }

    @Override
    public boolean isClaimed() {
        return false;
    }

    @Override
    public void setClaimed(boolean claimed) {

    }

    @Override
    public boolean hasOwner() {
        return false;

    }

    @Override
    public UUID getOwner() {
        return null;
    }

    @Override
    public void setOwner(UUID owner) {

    }

    @Override
    @SuppressWarnings("removal")
    public UUID[] getTrusted() {
        return getTrustedList().toArray(new UUID[0]);
    }

    @Override
    public void setTrusted(List<UUID> trusted) {

    }

    @Override
    public List<UUID> getTrustedList() {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<String, String> properties) {

    }
    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public void addProperty(String key, String value) {

    }

    @Override
    public void removeProperty(String key) {
    }

    @Override
    public boolean isTrusted(UUID uuid) {
        return false;
    }

    @Override
    public void addTrusted(UUID trusted) {
    }

    @Override
    public void removeTrusted(UUID trusted) {
    }

    @Override
    public void clearTrusted() {
        setTrusted(null);
    }

    @Override
    public boolean shouldIgnoreInteractable(String material) {
        return shouldIgnoreInteractable(Material.getMaterial(material));
    }

    public boolean shouldIgnoreInteractable(Material material) {
        return getIgnoredInteractableBukkitMaterials().contains(material);
    }

    @Override
    public List<String> getIgnoredInteractableMaterials() {
        List<String> materials = new ArrayList<>();
        for (Material material : getIgnoredInteractableBukkitMaterials()) {
            materials.add(material.name());
        }
        return materials;
    }

    @Override
    public void setIgnoredInteractableMaterials(List<String> ignoredInteractableMaterials) {
        List<Material> materials = new ArrayList<>();
        for (String material : ignoredInteractableMaterials) {
            materials.add(Material.getMaterial(material));
        }
        setIgnoredInteractableBukkitMaterials(materials);
    }

    public List<Material> getIgnoredInteractableBukkitMaterials() {
        ignoredInteractableMaterials = new ArrayList<>();
        return ignoredInteractableMaterials;
    }

    public void setIgnoredInteractableBukkitMaterials(List<Material> ignoredInteractableMaterials) {

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
