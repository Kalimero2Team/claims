package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import com.kalimero2.team.claims.paper.util.ExtraPlayerData;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ChunkCommand extends CommandHandler{
    private final ArrayList<UUID> forcedPlayers = new ArrayList<>();
    public ChunkCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk").handler(this::info)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("auto")
                        .handler(this::autoClaim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("add")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::trustAdd)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("remove")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::trustRemove)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("claim")
                        .handler(this::claim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("unclaim")
                        .handler(this::unClaim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("list")
                        .handler(this::listClaims)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("listother")
                        .permission("claims.admin.listother")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::listClaimsOther)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("setowner")
                        .permission("claims.admin.setOwner")
                        .argument(OfflinePlayerArgument.optional("target"))
                        .handler(this::setOwner)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("force")
                        .permission("claims.admin.force")
                        .handler(this::toggleForceMode)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("teamclaim")
                        .permission("claims.admin.teamclaim")
                        .handler(this::teamClaim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("border")
                        .handler(this::toggleBorder)
        );
        commandManager.command(
                commandManager.commandBuilder("cb","chunkborder")
                        .handler(this::toggleBorder)
        );
    }

    private void teamClaim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.isClaimed()){
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_already_claimed");
            }else{
                chunk.setClaimed(true);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_success");
            }
        }
    }

    private void listClaimsOther(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            OfflinePlayer target = context.get("target");
            ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(target);
            player.sendMessage("Claims von ("+target.getName()+"): "+extraPlayerData.chunks.size());
            extraPlayerData.chunks.forEach(serializableChunk -> {
                TagResolver.Single chunk_x = Placeholder.unparsed("chunk_x", String.valueOf(serializableChunk.x()));
                TagResolver.Single chunk_z = Placeholder.unparsed("chunk_z", String.valueOf(serializableChunk.z()));
                Chunk obj = serializableChunk.toBukkitChunk();
                Location location = obj.getBlock(0, 0, 0).getLocation();
                TagResolver.Single x = Placeholder.unparsed("x", String.valueOf(location.getBlockX()));
                TagResolver.Single z = Placeholder.unparsed("z", String.valueOf(location.getBlockZ()));
                Component component = PaperClaims.plugin.getMessageUtil().getMessage("chunk.list", chunk_x, chunk_z, x, z).clickEvent(ClickEvent.runCommand("/tp " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()));
                player.sendMessage(component);
            });
        }
    }

    private void toggleForceMode(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(forcedPlayers.contains(player.getUniqueId())){
                forcedPlayers.remove(player.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.force_off");
            }else{
                forcedPlayers.add(player.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.force_on");
            }
        }
    }

    private void toggleBorder(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(ChunkBorders.show_border.contains(player)){
                ChunkBorders.show_border.remove(player);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.border_off");
            }else{
                ChunkBorders.show_border.add(player);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.border_on");
            }
        }
    }

    private void setOwner(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(context.getOptional("target").isPresent()){
                OfflinePlayer target = context.get("target");
                ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
                chunk.setOwner(target.getUniqueId());
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.set_owner",  target_placeholder);
            }
        }
    }

    private void listClaims(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(player);
            player.sendMessage("Claims: "+extraPlayerData.chunks.size());
            extraPlayerData.chunks.forEach(serializableChunk -> {
                TagResolver.Single chunk_x = Placeholder.unparsed("chunk_x", String.valueOf(serializableChunk.x()));
                TagResolver.Single chunk_z = Placeholder.unparsed("chunk_z", String.valueOf(serializableChunk.z()));
                Chunk obj = serializableChunk.toBukkitChunk();
                Location location = obj.getBlock(0, 0, 0).getLocation();
                TagResolver.Single x = Placeholder.unparsed("x", String.valueOf(location.getBlockX()));
                TagResolver.Single z = Placeholder.unparsed("z", String.valueOf(location.getBlockZ()));
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.list", chunk_x, chunk_z,x,z);
            });
        }
    }

    private void autoClaim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            player.sendMessage("Es konnte kein freier Chunk gefunden werden.");
        }
    }

    private void unClaim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())){
                ClaimManager.unclaimChunk(chunk,player);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_success");
            } else if (!chunk.hasOwner() && player.hasPermission("claims.admin.teamClaim")) {
                chunk.setClaimed(false);
                chunk.setProperties(new HashMap<>());
                chunk.setTrusted(new UUID[0]);

                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.team_unclaim_success");
            } else{
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_fail_not_owner");
            }
        }
    }

    private void claim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.isClaimed()){
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_already_claimed");
            }else{
                if(ClaimManager.claimChunk(chunk, player)){
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_success");
                }else{
                    ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(player);
                    TagResolver.Single count = Placeholder.unparsed("count", String.valueOf(extraPlayerData.chunks.size()));
                    TagResolver.Single max = Placeholder.unparsed("max_count", String.valueOf(extraPlayerData.maxclaims));
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_too_many_claims",count,max);
                }

            }
        }
    }

    private void trustRemove(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())) {
                OfflinePlayer target = context.get("target");
                chunk.removeTrusted(target.getUniqueId());
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_remove_success", target_placeholder);
            }
        }
    }

    private void trustAdd(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())) {
                OfflinePlayer target = context.get("target");
                chunk.addTrusted(target.getUniqueId());
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_add_success", target_placeholder);
            }
        }
    }

    private void info(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            Server server = PaperClaims.plugin.getServer();
            MessageUtil messageUtil = PaperClaims.plugin.getMessageUtil();

            messageUtil.sendMessage(player,"chunk.info", Placeholder.component("chunk_x", Component.text(chunk.getChunkX())), Placeholder.component("chunk_z", Component.text(chunk.getChunkZ())));

            if(chunk.isClaimed()){
                messageUtil.sendMessage(player, "chunk.claimed_true");
                if(chunk.hasOwner()){
                    String owner_name = server.getOfflinePlayer(chunk.getOwner()).getName();
                    if(owner_name == null){
                        owner_name = chunk.getOwner().toString();
                    }
                    messageUtil.sendMessage(player, "chunk.claim_owner", Placeholder.unparsed("player", owner_name));
                }else{
                    messageUtil.sendMessage(player, "chunk.team_chunk");
                }

                UUID[] trusted = chunk.getTrusted();
                if(trusted.length >= 1){
                    messageUtil.sendMessage(player, "chunk.claim_trusted_start");
                    for(UUID trustedPlayer:trusted){
                        OfflinePlayer offlinePlayer = server.getOfflinePlayer(trustedPlayer);
                        messageUtil.sendMessage(player, "chunk.claim_trusted_player", Placeholder.unparsed("player", Objects.requireNonNullElse(offlinePlayer.getName(),"player")));
                    }
                }
            }else{
                messageUtil.sendMessage(player, "chunk.claimed_false");
            }

        }
    }
}
