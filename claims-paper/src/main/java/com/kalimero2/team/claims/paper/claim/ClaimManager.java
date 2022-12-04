package com.kalimero2.team.claims.paper.claim;

import com.kalimero2.team.claims.paper.util.ExtraPlayerData;
import com.kalimero2.team.claims.paper.util.SerializableChunk;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static com.kalimero2.team.claims.paper.PaperClaims.plugin;

public class ClaimManager {

    public static final ArrayList<UUID> forcedPlayers = new ArrayList<>();

    public static ExtraPlayerData getExtraPlayerData(OfflinePlayer offlinePlayer) {
        return getExtraPlayerData(offlinePlayer.getUniqueId());
    }

    public static ExtraPlayerData getExtraPlayerData(UUID uuid) {
        File file = new File(plugin.playerDataFolder + "/" + uuid + ".json");
        if (!file.exists()) {
            int anInt = plugin.getConfig().getInt("claims.max-claims");
            return new ExtraPlayerData(new HashSet<>(), anInt);
        }
        return ExtraPlayerData.loadData(file.getAbsolutePath());
    }

    public static void setExtraPlayerData(OfflinePlayer offlinePlayer, ExtraPlayerData extraPlayerData) {
        setExtraPlayerData(offlinePlayer.getUniqueId(), extraPlayerData);
    }

    public static void setExtraPlayerData(UUID uuid, ExtraPlayerData extraPlayerData) {
        File file = new File(plugin.playerDataFolder + "/" + uuid + ".json");
        extraPlayerData.saveData(file.getAbsolutePath());
    }

    public static boolean claimChunk(ClaimsChunk chunk, OfflinePlayer player) {
        ExtraPlayerData extraPlayerData = getExtraPlayerData(player);
        if (extraPlayerData.chunks.size() >= extraPlayerData.maxclaims) {
            return false;
        }
        extraPlayerData.chunks.add(SerializableChunk.fromBukkitChunk(chunk.getBukkitChunk()));
        setExtraPlayerData(player, extraPlayerData);
        chunk.setOwner(player.getUniqueId());
        chunk.setClaimed(true);
        return true;
    }

    public static void unclaimChunk(ClaimsChunk chunk, OfflinePlayer player) {
        chunk.setOwner(null);
        chunk.setClaimed(false);
        ExtraPlayerData extraPlayerData = getExtraPlayerData(player);
        extraPlayerData.chunks.remove(SerializableChunk.fromBukkitChunk(chunk.getBukkitChunk()));
        setExtraPlayerData(player, extraPlayerData);
    }


}
