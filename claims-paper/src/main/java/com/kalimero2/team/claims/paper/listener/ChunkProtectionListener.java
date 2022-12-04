package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.projectiles.BlockProjectileSource;

public class ChunkProtectionListener implements Listener {
    private boolean shouldCancel(Player player, Chunk bukkitChunk) {
        return shouldCancel(player, ClaimsChunk.of(bukkitChunk));
    }

    private boolean shouldCancel(Player player, ClaimsChunk chunk) {
        if (chunk.isClaimed()) {
            if (chunk.hasOwner()) {
                if (!chunk.getOwner().equals(player.getUniqueId())) {
                    return !chunk.isTrusted(player.getUniqueId());
                }
            } else {
                return !player.hasPermission("claims.admin.teamclaim");
            }
        }
        return false;
    }

    private boolean shouldCancel(Chunk originChunk, Chunk destChunk) {
        return shouldCancel(ClaimsChunk.of(originChunk), ClaimsChunk.of(destChunk));
    }

    private boolean shouldCancel(ClaimsChunk originChunk, ClaimsChunk destChunk) {
        if (originChunk.equals(destChunk)) {
            return false;
        }
        if(destChunk.isClaimed()){ // destChunk is claimed
            if(originChunk.isClaimed()){ // both chunks are claimed
                if(destChunk.hasOwner()){ // destChunk is claimed by a player
                    if(originChunk.hasOwner()){ // both chunks are claimed by a player
                        return !destChunk.getOwner().equals(originChunk.getOwner()); // true if different owner, false if same owner
                    }
                    return true; // destChunk is claimed by a player, originChunk isn't
                }else{
                    if(originChunk.hasOwner()){
                        return true; // originChunk is claimed by a player, destChunk isn't
                    }
                }
            }else{
                return true; // originChunk is not claimed, dest chunk is claimed
            }
        }
        return false; // destChunk is not claimed
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFishEvent(PlayerBucketEntityEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getEntity().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBookEvent(PlayerTakeLecternBookEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getLectern().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if (shouldCancel(event.getPlayer(), chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlocksPlace(BlockMultiPlaceEvent event) {
        for (BlockState blockState : event.getReplacedBlockStates()) {
            ClaimsChunk chunk = ClaimsChunk.of(blockState.getChunk());
            if (shouldCancel(event.getPlayer(), chunk)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getRightClicked().getChunk()))) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getTo().getChunk()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        if (event.getDamager() instanceof Player player) {
            onEntityDamageByPlayer(event, player);
        } else {
            if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player player) {
                    if (shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))) {
                        onEntityDamageByPlayer(event, player);
                    }
                } else if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource) {
                    if (shouldCancel(ClaimsChunk.of(event.getEntity().getChunk()), ClaimsChunk.of(blockProjectileSource.getBlock().getChunk()))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {
            if (shouldCancel(player, ClaimsChunk.of(event.getVehicle().getChunk()))) {
                event.setCancelled(true);
            }
        } else if (event.getAttacker() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                if (shouldCancel(player, ClaimsChunk.of(event.getVehicle().getChunk()))) {
                    event.setCancelled(true);
                }
            } else if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource) {
                if (shouldCancel(ClaimsChunk.of(event.getVehicle().getChunk()), ClaimsChunk.of(blockProjectileSource.getBlock().getChunk()))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void onEntityDamageByPlayer(EntityDamageByEntityEvent event, Player player) {
        if (event.getEntity() instanceof Player || event.getEntity() instanceof Monster) {
            event.setCancelled(false);
        } else if (event.getEntity() instanceof Animals || event.getEntity() instanceof Tameable || event.getEntity() instanceof NPC || event.getEntity() instanceof Hanging || event.getEntity() instanceof ArmorStand || event.getEntity() instanceof Vehicle) {
            if (shouldCancel(player, ClaimsChunk.of(event.getEntity().getLocation().getChunk()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null) {
            if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getClickedBlock().getChunk()))) {
                Material type = event.getClickedBlock().getType();
                if (ClaimsChunk.of(event.getClickedBlock().getChunk()).shouldIgnoreInteractable(type)) {
                    return;
                }

                if (type.isInteractable()) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getPlayer().getActiveItem().getType().equals(Material.POWDER_SNOW_BUCKET)) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            if (event.getClickedBlock() != null) {
                if (event.getClickedBlock().getType().equals(Material.FARMLAND)) {
                    if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getClickedBlock().getChunk()))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getRightClicked().getChunk()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getEntity().getChunk()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))) {
                event.setCancelled(true);
            }
        } else if (event.getRemover() instanceof Vehicle) { // Boats can destory hanging entities
            if (ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS)) { // Also includes Boats. There is no Event when a boat breaks a hanging entity
            if (ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))) {
                event.setCancelled(true);
            }
        } else if (event.getRemover() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                if (shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))) {
                    event.setCancelled(true);
                }
            } else {
                if (ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getRemover() instanceof Creeper) {
            if (ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getEntity().getChunk()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof Wither) {
            if (ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof WitherSkull) {
            if (ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof Boat) {
            if (ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof ThrownPotion thrownPotion) { // A Thrown potion can dowse fire
            if (thrownPotion.getShooter() instanceof Player player) {
                if (shouldCancel(player, ClaimsChunk.of(event.getBlock().getChunk()))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityInsideBlock(EntityInsideBlockEvent event) {
        ClaimsChunk chunk = ClaimsChunk.of(event.getBlock().getChunk());
        if (chunk.isClaimed()) {
            if (event.getEntity() instanceof Ravager) { // Ravagers break Crop Blocks
                event.setCancelled(true);
            } else if (event.getEntity() instanceof Projectile projectile) { // Only projectiles that are shot by trusted players can interact
                if (projectile.getShooter() instanceof Player player) {
                    if (shouldCancel(player, chunk)) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause().equals(BlockIgniteEvent.IgniteCause.ARROW)) {
            if (shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getBlock().getChunk()))) {
                if (!ClaimsChunk.of(event.getBlock().getChunk()).shouldIgnoreInteractable(Material.CAMPFIRE)) {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getBlock().getBlockData() instanceof Directional directional) {
            Chunk originChunk = event.getBlock().getChunk();
            Chunk destChunk = event.getBlock().getLocation().add(directional.getFacing().getDirection()).getChunk();

            if (shouldCancel(originChunk, destChunk)) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        Chunk originChunk = event.getSource().getChunk();
        Chunk destChunk = event.getBlock().getChunk();

        if (shouldCancel(originChunk, destChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Chunk originChunk = event.getBlock().getChunk();

        for (BlockState block : event.getBlocks()) {
            Chunk destChunk = block.getChunk();

            if (shouldCancel(originChunk, destChunk)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemFrameChange(PlayerItemFrameChangeEvent event) {
        ClaimsChunk claimsChunk = ClaimsChunk.of(event.getItemFrame().getChunk());
        if (shouldCancel(event.getPlayer(), claimsChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        Chunk originChunk = event.getBlock().getChunk();
        for (Block block : event.getBlocks()) {
            Chunk destChunk = block.getLocation().add(event.getDirection().getDirection()).getBlock().getChunk();
            if (shouldCancel(originChunk, destChunk)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Chunk originChunk = event.getBlock().getChunk();
        for (Block block : event.getBlocks()) {
            Chunk destChunk = block.getChunk();

            if (shouldCancel(originChunk, destChunk)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Chunk originChunk = event.getBlock().getChunk();
        Chunk destChunk = event.getToBlock().getChunk();

        if (shouldCancel(originChunk, destChunk)) {
            event.setCancelled(true);
        }
    }


}
