package com.kalimero2.team.claims.paper.listener;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Steerable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ChunkProtectionListener implements Listener {

    private final ClaimsApi api;

    public ChunkProtectionListener(ClaimsApi api) {
        this.api = api;
    }

    private boolean shouldCancel(Player player, Chunk bukkitChunk) {
        return shouldCancel(player, api.getClaim(bukkitChunk));
    }

    private boolean shouldCancel(Player player, Claim claim) {
        if (claim != null) {
            List<Group> members = claim.getMembers();

            Optional<Group> any = members.stream().filter(group -> api.getGroupMember(group, player) != null).findAny();
            return any.isEmpty();
        }
        return false;
    }

    private boolean shouldCancel(Chunk originChunk, Chunk destChunk) {
        return shouldCancel(api.getClaim(originChunk), api.getClaim(destChunk));
    }

    private boolean shouldCancel(@Nullable Claim originClaim, @Nullable Claim destClaim) {
        if (originClaim != null && originClaim.equals(destClaim)) {
            return false;
        }
        if (destClaim != null) { // destChunk is claimed
            if (originClaim != null) { // both chunks are claimed
                if (originClaim.getOwner().equals(destClaim.getOwner())) { // both chunks are claimed by the same group
                    return false;
                }
            } else {
                return true; // originChunk is not claimed, dest chunk is claimed
            }
        }
        return false; // destChunk is not claimed
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (shouldCancel(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (shouldCancel(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFishEvent(PlayerBucketEntityEvent event) {
        if (shouldCancel(event.getPlayer(), event.getEntity().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBookEvent(PlayerTakeLecternBookEvent event) {
        if (shouldCancel(event.getPlayer(), event.getLectern().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldCancel(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (shouldCancel(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Claim claim = api.getClaim(event.getBlock().getChunk());
        if (shouldCancel(event.getPlayer(), claim)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlocksPlace(BlockMultiPlaceEvent event) {
        for (BlockState blockState : event.getReplacedBlockStates()) {
            Claim claim = api.getClaim(blockState.getChunk());
            if (shouldCancel(event.getPlayer(), claim)) {
                event.setCancelled(true);
            }
        }
    }



    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (shouldCancel(event.getPlayer(), event.getRightClicked().getChunk())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            if (shouldCancel(event.getPlayer(), event.getTo().getChunk())) {
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
                    if (shouldCancel(player, api.getClaim(event.getEntity().getChunk()))) {
                        onEntityDamageByPlayer(event, player);
                    }
                } else if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource) {
                    if (shouldCancel(event.getEntity().getChunk(), blockProjectileSource.getBlock().getChunk())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player) {
            if (shouldCancel(player, event.getVehicle().getChunk())) {
                event.setCancelled(true);
            }
        } else if (event.getAttacker() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                if (shouldCancel(player, event.getVehicle().getChunk())) {
                    event.setCancelled(true);
                }
            } else if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource) {
                if (shouldCancel(event.getVehicle().getChunk(), blockProjectileSource.getBlock().getChunk())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void onEntityDamageByPlayer(EntityDamageByEntityEvent event, Player player) {
        Entity target = event.getEntity();
        Claim claim = api.getClaim(target.getChunk());

        // Chunk EntityInteractables overrides
        List<EntityType> damageAllowed = List.of();
        List<EntityType> damageDenied = List.of();

        if (claim != null) {
            List<EntityInteractable> entityInteractables = claim.getEntityInteractables();
            damageAllowed = entityInteractables.stream().filter(EntityInteractable::isDamage).map(EntityInteractable::getEntityType).toList();
            damageDenied = entityInteractables.stream().filter(entityInteractable -> !entityInteractable.isDamage()).map(EntityInteractable::getEntityType).toList();
        }

        if (damageDenied.contains(target.getType())) {
            event.setCancelled(true);
            return;
        }

        if (damageAllowed.contains(target.getType())) {
            event.setCancelled(false);
            return;
        }

        // Defaults and PVP Flag
        if (target instanceof Monster) {
            event.setCancelled(false);
        } else if (target instanceof Player) {
            if (claim != null) {
                boolean pvpOn = api.getFlagState(claim, ClaimsFlags.PVP);
                event.setCancelled(!pvpOn);
            }
            event.setCancelled(true);
        } else if (target instanceof Animals || target instanceof NPC || target instanceof Hanging || target instanceof ArmorStand || target instanceof Vehicle) {
            if (shouldCancel(player, target.getChunk())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null) {
            if (shouldCancel(event.getPlayer(), event.getClickedBlock().getChunk())) {
                Material type = event.getClickedBlock().getType();

                Claim claim = api.getClaim(event.getClickedBlock().getChunk());

                if (claim == null) {
                    return;
                }

                if (claim.getBlockInteractables().stream().filter(BlockInteractable::getState).map(BlockInteractable::getBlockMaterial).toList().contains(type)) {
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
                    if (shouldCancel(event.getPlayer(), event.getClickedBlock().getChunk())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Claim claim = api.getClaim(event.getRightClicked().getChunk());

        if (claim == null) {
            return;
        }

        List<EntityType> interactAllowed = claim.getEntityInteractables().stream().filter(EntityInteractable::isInteract).map(EntityInteractable::getEntityType).toList();

        if (interactAllowed.contains(event.getRightClicked().getType())) {
            event.setCancelled(false);
            return;
        }

        if (shouldCancel(event.getPlayer(), event.getRightClicked().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (shouldCancel(event.getPlayer(), event.getEntity().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (shouldCancel(player, event.getEntity().getChunk())) {
                event.setCancelled(true);
            }
        } else if (event.getRemover() instanceof Vehicle) { // Boats can destory hanging entities
            if (api.getClaim(event.getEntity().getChunk()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS)) { // Also includes Boats. There is no Event when a boat breaks a hanging entity
            if (api.getClaim(event.getEntity().getChunk()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (shouldCancel(player, event.getEntity().getChunk())) {
                event.setCancelled(true);
            }
        } else if (event.getRemover() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                if (shouldCancel(player, event.getEntity().getChunk())) {
                    event.setCancelled(true);
                }
            } else {
                if (api.getClaim(event.getEntity().getChunk()) != null) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getRemover() instanceof Creeper) {
            if (api.getClaim(event.getEntity().getChunk()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (shouldCancel(event.getPlayer(), event.getEntity().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof Wither) {
            if (api.getClaim(event.getBlock().getChunk()) != null) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof WitherSkull) {
            if (api.getClaim(event.getBlock().getChunk()) != null) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof Boat) {
            if (api.getClaim(event.getBlock().getChunk()) != null) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof ThrownPotion thrownPotion) { // A Thrown potion can dowse fire
            if (thrownPotion.getShooter() instanceof Player player) {
                if (shouldCancel(player, event.getBlock().getChunk())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityInsideBlock(EntityInsideBlockEvent event) {
        Claim claim = api.getClaim(event.getBlock().getChunk());
        if (claim != null) {
            if (event.getEntity() instanceof Ravager) { // Ravagers break Crop Blocks
                event.setCancelled(true);
            } else if (event.getEntity() instanceof Projectile projectile) { // Only projectiles that are shot by trusted players can interact
                if (projectile.getShooter() instanceof Player player) {
                    if (shouldCancel(player, claim)) {
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
            Claim claim = api.getClaim(event.getBlock().getChunk());
            if (shouldCancel(event.getPlayer(), claim)) {
                // TODO: Check for BlockInteractable state
                if (claim.getBlockInteractables().stream().map(BlockInteractable::getBlockMaterial).toList().contains(Material.CAMPFIRE)) {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (api.getClaim(event.getBlock().getChunk()) != null) {
            if (event.getEntity() instanceof Steerable steerable) {
                // TODO: Remove this (temporary) because GeyserHacks isn't working in 1.19.4+
                // EntityInteractEvent isn't called when a Java Player is riding with a pig over a pressure plate but when a Bedrock Player is riding with a pig over a pressure plate the event is fired (because GeyserHacks is "faking" the Riding and causes the event to be fired). For Java players it is probably another event or a Bukkit bug.
                Optional<Entity> any = steerable.getPassengers().stream().filter(entity -> entity instanceof Player).findAny();
                if (any.isEmpty()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
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
        if (shouldCancel(event.getPlayer(), event.getItemFrame().getChunk())) {
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
