package com.kalimero2.team.claims.squaremap.hook;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.event.ChunkClaimedEvent;
import com.kalimero2.team.claims.api.flag.Flag;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public final class ClaimsHook implements Listener {

    private static final ClaimsApi api = ClaimsApi.getApi();
    private static final HashMap<World, List<Claim>> claims = new HashMap<>();
    public static final Flag TEAM_FLAG = Flag.of(new NamespacedKey("map", "team"), false, true);

    public static void registerFlag(){
        api.registerFlag(TEAM_FLAG);
    }

    public static void registerListener(Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(new ClaimsHook(), plugin);
    }

    public static List<Claim> getClaims(World world) {
        if(claims.containsKey(world)){
            return claims.get(world);
        }
        List<Claim> claimsInWorld = api.getClaims(world);
        claims.put(world, claimsInWorld);
        return claimsInWorld;
    }

    @EventHandler
    public void onChunkClaim(ChunkClaimedEvent event){
        World world = event.getChunk().getWorld();
        if(claims.containsKey(world)){
            claims.get(world).add(event.getClaim());
        }else {
            claims.put(world, List.of(event.getClaim()));
        }
    }

    @EventHandler
    public void onChunkUnclaim(ChunkClaimedEvent event){
        World world = event.getChunk().getWorld();
        if(claims.containsKey(world)){
            claims.get(world).remove(event.getClaim());
        }
    }
}
