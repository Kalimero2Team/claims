package com.kalimero2.team.claims.api.flag;

import com.kalimero2.team.claims.api.ClaimsApi;
import org.bukkit.NamespacedKey;

public class Flags {
    public static final Flag PVP = register("pvp", false);
    public static final Flag EXPLOSIONS = register("explosions", false);
    public static final Flag FIRE_SPREAD = register("fire_spread", false);
    public static final Flag ITEM_DROP = register("item_drop", true);
    public static final Flag ITEM_PICKUP = register("item_pickup", true);

    private static Flag register(String key, boolean defaultState) {
        Flag flag = Flag.of(new NamespacedKey("claims", key), defaultState);
        ClaimsApi.getApi().registerFlag(flag);
        return flag;
    }
}
