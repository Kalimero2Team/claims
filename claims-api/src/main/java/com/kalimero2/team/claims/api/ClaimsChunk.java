package com.kalimero2.team.claims.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class ClaimsChunk {

    private final UUID world;
    private final int chunkX;
    private final int chunkZ;

    protected ClaimsChunk(UUID world, int x, int z) {
        this.world = world;
        this.chunkX = x;
        this.chunkZ = z;
    }

    public UUID getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public abstract boolean isClaimed();

    public abstract void setClaimed(boolean claimed);

    public abstract boolean hasOwner();

    public abstract UUID getOwner();

    public abstract void setOwner(UUID owner);

    @Deprecated(since = "1.0.2", forRemoval = true) // Use getTrustedList() instead
    public abstract UUID[] getTrusted();

    public abstract void setTrusted(List<UUID> trusted);

    public abstract List<UUID> getTrustedList();

    public abstract Map<String, String> getProperties();

    public abstract void setProperties(Map<String, String> properties);

    public abstract String getProperty(String key);

    public abstract void addProperty(String key, String value);

    public abstract void removeProperty(String key);

    public abstract boolean isTrusted(UUID uuid);

    public abstract void addTrusted(UUID trusted);

    public abstract void removeTrusted(UUID trusted);

    public abstract void clearTrusted();

    public abstract boolean shouldIgnoreInteractable(String material);

    public abstract List<String> getIgnoredInteractableMaterials();

    public abstract void setIgnoredInteractableMaterials(List<String> interactables);

    public abstract void addIgnoredInteractable(String material);

    public abstract void removeIgnoredInteractable(String material);

    public abstract void clearIgnoredInteractables();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimsChunk that = (ClaimsChunk) o;
        return chunkX == that.chunkX && chunkZ == that.chunkZ && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return "ClaimsChunk{" +
                "world=" + world +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }

}
