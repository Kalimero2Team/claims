package com.kalimero2.team.claims.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SerializableChunk implements ConfigurationSerializable, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final UUID world;
    private final int x;
    private final int z;

    public SerializableChunk(UUID world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static SerializableChunk fromBukkitChunk(Chunk chunk) {
        return new SerializableChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    public Chunk toBukkitChunk() {
        return Objects.requireNonNull(Bukkit.getWorld(world), "World not found. World:" + world).getChunkAt(x, z);
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", world);
        map.put("x", x);
        map.put("z", z);
        return map;
    }

    public static SerializableChunk deserialize(Map<String, Object> map) {
        return new SerializableChunk((UUID) map.get("world"), (int) map.get("x"), (int) map.get("z"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableChunk that = (SerializableChunk) o;
        return x == that.x && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return "SerializableChunk{" +
                "world=" + world +
                ", x=" + x +
                ", z=" + z +
                '}';
    }

    public UUID world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

}
