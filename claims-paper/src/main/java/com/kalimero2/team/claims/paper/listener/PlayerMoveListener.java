package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import com.kalimero2.team.claims.paper.util.SerializableChunk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

import static com.kalimero2.team.claims.paper.PaperClaims.plugin;

public class PlayerMoveListener implements Listener {

    private final HashMap<UUID, SerializableChunk> lastChunkMap = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (lastChunkMap.get(player.getUniqueId()) == null) {
            lastChunkMap.put(player.getUniqueId(), SerializableChunk.fromBukkitChunk(player.getLocation().getChunk()));
        }
        if (lastChunkMap.get(player.getUniqueId()).equals(SerializableChunk.fromBukkitChunk(player.getLocation().getChunk()))) {
            return;
        } else {
            lastChunkMap.put(player.getUniqueId(), SerializableChunk.fromBukkitChunk(player.getLocation().getChunk()));
        }


        ClaimsChunk chunk = ClaimsChunk.of(event.getPlayer().getChunk());
        if (chunk.isClaimed() && chunk.hasOwner()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    UUID uuid = chunk.getOwner();
                    String playername = plugin.getServer().getOfflinePlayer(uuid).getName();
                    if (playername == null) {
                        playername = "Unknown (UUID: " + uuid + ")";
                    }
                    // TODO: Put the message into messages.yml
                    TextComponent msg = Component.text().content("Grundstücksbesitzer: ").color(NamedTextColor.WHITE).append(Component.text(playername).color(NamedTextColor.GRAY)).build();
                    event.getPlayer().sendActionBar(msg);
                }
            }.runTaskAsynchronously(plugin);
        }
    }


}
