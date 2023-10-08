package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Claim claim = ClaimsApi.getApi().getClaim(player.getChunk());
        if (claim != null) {
            // TODO: Add Group name
            String name = claim.getOwner().toString();
            // TODO: Put the message into messages.yml
            TextComponent msg = Component.text().content("Grundstücksbesitzer: ").color(NamedTextColor.WHITE).append(Component.text(name).color(NamedTextColor.GRAY)).build();
            event.getPlayer().sendActionBar(msg);
        }
    }


}
