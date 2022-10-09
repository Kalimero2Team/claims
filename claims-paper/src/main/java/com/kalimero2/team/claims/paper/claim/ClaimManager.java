package com.kalimero2.team.claims.paper.claim;

import org.bukkit.OfflinePlayer;

public class ClaimManager {

    public static void claimChunk(ClaimsChunk chunk, OfflinePlayer player){
        // TODO: Write Data to Player
        chunk.setOwner(player.getUniqueId());
        chunk.setClaimed(true);
    }

    public static void unclaimChunk(ClaimsChunk chunk, OfflinePlayer player){
        // TODO: Write Data to Player
        chunk.setOwner(null);
        chunk.setClaimed(false);
    }

}
