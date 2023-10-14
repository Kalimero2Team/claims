package com.kalimero2.team.claims.paper.listener;


import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class PhysicsListener implements Listener {

    private final ClaimsApi api;

    public PhysicsListener(ClaimsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (api.getClaim(event.getBlock().getChunk()) != null) {
            boolean no_physics = api.getFlagState(api.getClaim(event.getBlock().getChunk()), ClaimsFlags.NO_PHYSICS);
            if (no_physics) {
                event.setCancelled(true);
            }
        }
    }

}
