package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {

    private final ClaimsApi api;

    public ExplosionListener(ClaimsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Claim blockClaim = api.getClaim(block.getChunk());
            if (blockClaim != null) {
                boolean explosionPossible = api.getFlagState(blockClaim, ClaimsFlags.EXPLOSIONS);
                return !explosionPossible;
            } else {
                return true;
            }
        });
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Claim blockClaim = api.getClaim(block.getChunk());
            if (blockClaim != null) {
                boolean explosionPossible = api.getFlagState(blockClaim, ClaimsFlags.EXPLOSIONS);
                return !explosionPossible;
            } else {
                return true;
            }
        });
    }


}
