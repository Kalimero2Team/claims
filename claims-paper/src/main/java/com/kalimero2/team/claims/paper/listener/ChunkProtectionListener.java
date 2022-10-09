package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class ChunkProtectionListener implements Listener {
    private boolean shouldCancel(Player player, ClaimsChunk chunk){
        if(chunk.isClaimed()){
            if(chunk.hasOwner()){
                if(!chunk.getOwner().equals(player.getUniqueId())){
                    return !chunk.isTrusted(player.getUniqueId());
                }
            }else {
                return !player.hasPermission("claims.admin.teamclaim");
            }
        }
        return false;
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFishEvent(PlayerBucketEntityEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBookEvent(PlayerTakeLecternBookEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getLectern().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if(shouldCancel(event.getPlayer(), chunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlocksPlace(BlockMultiPlaceEvent event){
        for(BlockState blockState:event.getReplacedBlockStates()){
            ClaimsChunk chunk = ClaimsChunk.of(blockState.getChunk());
            if(shouldCancel(event.getPlayer(), chunk)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        if(event.getRemover() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
            if(shouldCancel(player, chunk)){
                event.setCancelled(true);
            }
        }else if (event.getRemover() instanceof Projectile projectile){
            if(projectile.getShooter() instanceof Player player){
                ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
                if(shouldCancel(player, chunk)){
                    event.setCancelled(true);
                }
            }else{
                ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
                if(chunk.isClaimed()){
                    event.setCancelled(true);
                }
            }
        }else if(event.getRemover() instanceof Creeper){
            ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
            if(chunk.isClaimed()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        if(shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getEntity().getChunk()))){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if(shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getRightClicked().getChunk()))){
            event.setCancelled(true);
        }
    }



}
