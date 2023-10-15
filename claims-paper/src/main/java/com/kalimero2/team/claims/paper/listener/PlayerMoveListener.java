package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final ClaimsApi api;
    private final PaperClaims plugin;

    public PlayerMoveListener(ClaimsApi api, PaperClaims plugin) {
        this.api = api;
        this.plugin = plugin;
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

            if(claim.getOwner().isPlayer()){
                player.sendActionBar(plugin.getMessageUtil().getMessage("chunk.enter_message", Placeholder.unparsed("player", name)));
            }else {
                player.sendActionBar(plugin.getMessageUtil().getMessage("chunk.enter_message_group", Placeholder.unparsed("group", name)));
            }
        }
    }


}
