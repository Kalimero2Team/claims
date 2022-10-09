package com.kalimero2.team.claims.paper.listener;

import com.google.common.collect.Lists;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.List;

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

    private boolean shouldCancel(Chunk originChunk, Chunk destChunk) {
        if(!originChunk.equals(destChunk)){
            ClaimsChunk dest = ClaimsChunk.of(destChunk);
            if(dest.isClaimed()){
                ClaimsChunk origin = ClaimsChunk.of(originChunk);
                if(origin.isClaimed()){
                    if(dest.hasOwner()){
                        if(origin.hasOwner()){
                            return !dest.getOwner().equals(origin.getOwner());
                        }else {
                            return true;
                        }
                    }else {
                        return true;
                    }
                }else {
                    return true;
                }
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
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if(shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getRightClicked().getChunk()))){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)){
            if(shouldCancel(event.getPlayer(),ClaimsChunk.of(event.getTo().getChunk()))){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(event.isCancelled()) return;

        if(event.getDamager() instanceof Player player){
            onEntityDamageByPlayer(event, player);
        }else{
            if(event.getDamager() instanceof Projectile projectile){
                if(event.getEntity() instanceof Hanging){
                    event.setCancelled(true);
                }
                if(projectile.getShooter() instanceof Player player){
                    if(shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))){
                        onEntityDamageByPlayer(event, player);
                    }
                }
            }
        }
    }

    private void onEntityDamageByPlayer(EntityDamageByEntityEvent event, Player player) {
        if(event.getEntity() instanceof Player || event.getEntity() instanceof Monster){
            event.setCancelled(false);
        }else if(event.getEntity() instanceof Animals || event.getEntity() instanceof Tameable || event.getEntity() instanceof NPC || event.getEntity() instanceof Hanging || event.getEntity() instanceof ArmorStand){
            if(shouldCancel(player, ClaimsChunk.of(event.getEntity().getLocation().getChunk()))){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null){
            if(shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getClickedBlock().getChunk()))){
                Material material = event.getClickedBlock().getType();
                List<Material> protected_materials = Lists.newArrayList(Material.CHEST,Material.TRAPPED_CHEST,Material.BARREL,Material.SHULKER_BOX,Material.WHITE_SHULKER_BOX,Material.ORANGE_SHULKER_BOX,Material.MAGENTA_SHULKER_BOX,Material.LIGHT_BLUE_SHULKER_BOX,Material.YELLOW_SHULKER_BOX,Material.LIME_SHULKER_BOX,Material.PINK_SHULKER_BOX,Material.GRAY_SHULKER_BOX,Material.LIGHT_GRAY_SHULKER_BOX,Material.CYAN_SHULKER_BOX,Material.PURPLE_SHULKER_BOX,Material.BLUE_SHULKER_BOX,Material.BROWN_SHULKER_BOX,Material.GREEN_SHULKER_BOX,Material.RED_SHULKER_BOX,Material.BLACK_SHULKER_BOX,Material.FURNACE,Material.BLAST_FURNACE,Material.SMOKER,Material.BREWING_STAND,Material.DAMAGED_ANVIL,Material.JUKEBOX,Material.HOPPER,Material.DROPPER,Material.DISPENSER,Material.CAULDRON,Material.NOTE_BLOCK,Material.BEACON,Material.COMPARATOR,Material.REPEATER,Material.REDSTONE);

                if(protected_materials.contains(material)){
                    event.setCancelled(true);
                    return;
                }

                if (event.getPlayer().getActiveItem().getType().equals(Material.POWDER_SNOW_BUCKET)) {
                    event.setCancelled(true);
                }
            }
        }else if(event.getAction().equals(Action.PHYSICAL)){
            if(event.getClickedBlock() != null){
                if(event.getClickedBlock().getType().equals(Material.FARMLAND)){
                    if(shouldCancel(event.getPlayer(), ClaimsChunk.of(event.getClickedBlock().getChunk()))){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){
        event.setCancelled(true);
    }


    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        if(event.getRemover() instanceof Player player){
            if(shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))){
                event.setCancelled(true);
            }
        }else if (event.getRemover() instanceof Projectile projectile){
            if(projectile.getShooter() instanceof Player player){
                if(shouldCancel(player, ClaimsChunk.of(event.getEntity().getChunk()))){
                    event.setCancelled(true);
                }
            }else{
                if(ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()){
                    event.setCancelled(true);
                }
            }
        }else if(event.getRemover() instanceof Creeper){
            if(ClaimsChunk.of(event.getEntity().getChunk()).isClaimed()){
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
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(event.getEntity().getType().equals(EntityType.ENDERMAN)){
            event.setCancelled(true);
        }else if(event.getEntity().getType().equals(EntityType.BOAT)){
            if(ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onEntityInteract(EntityInteractEvent event){
        if(ClaimsChunk.of(event.getBlock().getChunk()).isClaimed()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event){
        if (event.getBlock().getBlockData() instanceof Directional directional) {
            Chunk originChunk = event.getBlock().getChunk();
            Chunk destChunk = event.getBlock().getLocation().add(directional.getFacing().getDirection()).getChunk();

            if(shouldCancel(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }




    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        Chunk originChunk = event.getSource().getChunk();
        Chunk destChunk = event.getBlock().getChunk();

        if(shouldCancel(originChunk, destChunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event){
        Chunk originChunk = event.getBlock().getChunk();

        for(BlockState block:event.getBlocks()){
            Chunk destChunk = block.getChunk();

            if(shouldCancel(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }



}
