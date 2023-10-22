package com.kalimero2.team.claims.squaremap.hook;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.List;

public final class ClaimsHook {

    private static final ClaimsApi api = ClaimsApi.getApi();
    public static final Flag TEAM_FLAG = Flag.of(new NamespacedKey("map", "team"), false, true);

    public static void registerFlag(){

        api.registerFlag(TEAM_FLAG);
    }

    public static List<Claim> getClaims(World world) {


        return api.getClaims(world);
    }
}
