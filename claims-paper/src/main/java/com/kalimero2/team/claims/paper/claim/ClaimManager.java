package com.kalimero2.team.claims.paper.claim;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.UUID;


public class ClaimManager {

    public static final ArrayList<UUID> forcedPlayers = new ArrayList<>();

    public static boolean claimChunk(ClaimsChunk chunk, OfflinePlayer player) {
        return true;
    }

    public static void unclaimChunk(ClaimsChunk chunk, OfflinePlayer player) {
    }


}
