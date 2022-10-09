package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event){
        ClaimsChunk.removeFromCache(event.getChunk());
    }
}
