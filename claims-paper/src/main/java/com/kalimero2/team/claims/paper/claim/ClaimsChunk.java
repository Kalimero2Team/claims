package com.kalimero2.team.claims.paper.claim;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.util.MoreDataTypes;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimsChunk extends com.kalimero2.team.claims.api.ClaimsChunk {

    private static final Map<Chunk, ClaimsChunk> cachedChunks = new HashMap<>();

    private final Chunk chunk;
    private Map<String, String> properties;

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
    public UUID[] getTrusted() {
        if(chunk.getPersistentDataContainer().has(getKey("trusted"), MoreDataTypes.UUID_ARRAY)){
            return chunk.getPersistentDataContainer().get(getKey("trusted"), MoreDataTypes.UUID_ARRAY);
        }
        return new UUID[0];
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
        this.properties = properties;
        chunk.getPersistentDataContainer().set(getKey("properties"), MoreDataTypes.CHUNK_PROPERTY_MAP, properties);
    }

    @Override
    public void setOwner(UUID owner) {
        if(owner == null){
            chunk.getPersistentDataContainer().remove(getKey("owner"));
        }else{
            chunk.getPersistentDataContainer().set(getKey("owner"), DataType.UUID, owner);
        }
    }

    @Override
    public void setTrusted(UUID[] trusted) {
        if(trusted == null) {
            chunk.getPersistentDataContainer().remove(getKey("trusted"));
        }else {
            chunk.getPersistentDataContainer().set(getKey("trusted"), MoreDataTypes.UUID_ARRAY, trusted);
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
        for(UUID trusted : getTrusted()){
            if(trusted.equals(uuid)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void addTrusted(UUID trusted) {
        UUID[] trustedArray = getTrusted();
        UUID[] newTrustedArray = new UUID[trustedArray.length + 1];
        System.arraycopy(trustedArray, 0, newTrustedArray, 0, trustedArray.length);
        newTrustedArray[newTrustedArray.length - 1] = trusted;
        setTrusted(newTrustedArray);
    }

    @Override
    public void removeTrusted(UUID trusted) {
        UUID[] trustedArray = getTrusted();
        UUID[] newTrustedArray = new UUID[trustedArray.length - 1];
        int index = 0;
        for (UUID uuid : trustedArray) {
            if(!uuid.equals(trusted)){
                newTrustedArray[index] = uuid;
                index++;
            }
        }
        setTrusted(newTrustedArray);
    }

    @Override
    public void clearTrusted() {
        setTrusted(new UUID[0]);
    }


    public Chunk getBukkitChunk() {
        return chunk;
    }

    private NamespacedKey getKey(String name) {
        return new NamespacedKey(PaperClaims.plugin, name);
    }
}
