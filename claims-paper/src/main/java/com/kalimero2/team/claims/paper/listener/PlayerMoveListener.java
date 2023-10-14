package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final ClaimsApi api;

    public PlayerMoveListener(ClaimsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        Claim claim = api.getClaim(event.getTo().getChunk());
        if (claim != null && !api.getFlagState(claim, ClaimsFlags.NO_ENTER_MESSAGE)) {
            String name = claim.getOwner().getName();
            // TODO: Put the message into messages.yml
            TextComponent msg = Component.text().content("Grundstücksbesitzer: ").color(NamedTextColor.WHITE).append(Component.text(name).color(NamedTextColor.GRAY)).build();
            player.sendActionBar(msg);
        }
    }


}
