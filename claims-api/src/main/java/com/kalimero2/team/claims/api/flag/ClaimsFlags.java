package com.kalimero2.team.claims.api.flag;

import com.kalimero2.team.claims.api.ClaimsApi;
import org.bukkit.NamespacedKey;

public final class ClaimsFlags {
    public static final Flag PVP = register("pvp", false);
    public static final Flag EXPLOSIONS = register("explosions", false);
    public static final Flag ITEM_DROP = register("item_drop", true, true);
    public static final Flag ITEM_PICKUP = register("item_pickup", true, true);
    public static final Flag NO_PHYSICS = register("no_physics", false, true);
    public static final Flag NO_ENTER_MESSAGE = register("no_enter_message", false, true);
    /**
     * Currently defaults to true,but will be changed to false in the future, when expiration is implemented
     */
    public static final Flag NO_EXPIRATION = register("no_expiration", true, true);

    private static Flag register(String key, boolean defaultState) {
        return register(key, defaultState, false);
    }

    private static Flag register(String key, boolean defaultState, boolean adminOnly) {
        Flag flag = Flag.of(new NamespacedKey("claims", key), defaultState, adminOnly);
        ClaimsApi.getApi().registerFlag(flag);
        return flag;
    }
}
