package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class MonsterSpawnListener implements Listener {

    private final ClaimsApi api;

    public MonsterSpawnListener(ClaimsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {

        if (api.getClaim(event.getEntity().getChunk()) != null) {
            if (event.getEntity() instanceof Monster) {
                boolean mobSpawning = api.getFlagState(api.getClaim(event.getEntity().getChunk()), ClaimsFlags.MONSTER_SPAWNING);
                if (!mobSpawning) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
