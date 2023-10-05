package com.kalimero2.team.claims.paper.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        // TODO: Load Claim Data from Database if and add to cache (if exists)

        // TODO: CHECK FOR IF EXPIRED

    }


    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        
        // TODO: Save Claim Data to Database

    }
}
