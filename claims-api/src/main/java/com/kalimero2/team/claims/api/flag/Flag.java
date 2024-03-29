package com.kalimero2.team.claims.api.flag;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Flag {

    private final NamespacedKey key;
    private final boolean defaultState;
    private final boolean adminOnly;

    private Flag(NamespacedKey key, boolean defaultState, boolean adminOnly) {
        this.key = key;
        this.defaultState = defaultState;
        this.adminOnly = adminOnly;
    }

    public static Flag of(NamespacedKey key, boolean defaultState) {
        return new Flag(key, defaultState, false);
    }

    public static Flag of(NamespacedKey key, boolean defaultState, boolean adminOnly) {
        return new Flag(key, defaultState, adminOnly);
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getKeyString() {
        return key.toString().replace("claims:", ""); // Removes the default namespace
    }

    /**
     * Get the permission for this flag
     * @return permission or null if this flag is not admin only
     */
    @Nullable
    public String getPermission() {
        if(isAdminOnly()){
            return "claims.flag."+getKey().getNamespace()+"."+getKey().getKey();
        }
        return null;
    }

    public boolean getDefaultState() {
        return defaultState;
    }

    public boolean isAdminOnly() {
        return adminOnly;
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
