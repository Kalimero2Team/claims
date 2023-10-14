package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemListener implements Listener {

    private final ClaimsApi api;

    public ItemListener(ClaimsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onItemPickUp(PlayerAttemptPickupItemEvent event) {
        if (api.getClaim(event.getItem().getChunk()) != null) {
            if (!api.getFlagState(api.getClaim(event.getItem().getChunk()), ClaimsFlags.ITEM_PICKUP)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (api.getClaim(event.getItemDrop().getChunk()) != null) {
            if (!api.getFlagState(api.getClaim(event.getItemDrop().getChunk()), ClaimsFlags.ITEM_DROP)) {
                event.setCancelled(true);
            }
        }
    }
}
