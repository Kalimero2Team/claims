package com.kalimero2.team.claims.api.flag;

import org.bukkit.NamespacedKey;

import java.util.Objects;

public final class Flag {

    private final NamespacedKey key;
    private final boolean defaultState;

    private Flag(NamespacedKey key, boolean defaultState) {
        this.key = key;
        this.defaultState = defaultState;
    }

    public static Flag of(NamespacedKey key, boolean defaultState) {
        return new Flag(key, defaultState);
    }

    public NamespacedKey getKey() {
        return key;
    }

    public boolean getDefaultState() {
        return defaultState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flag flag = (Flag) o;
        return Objects.equals(key, flag.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
