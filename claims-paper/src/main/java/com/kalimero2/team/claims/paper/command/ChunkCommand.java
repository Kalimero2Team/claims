package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
                commandManager.commandBuilder("auto")
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
                        .literal("border")
                        .handler(this::toggleBorder)
        );
        commandManager.command(
                commandManager.commandBuilder("cb","chunkborder")
                        .handler(this::toggleBorder)
        );
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

    }

    private void autoClaim(CommandContext<CommandSender> context) {
    }

    private void unClaim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.getOwner().equals(player.getUniqueId())){
                ClaimManager.unclaimChunk(chunk,player);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_success");
            }else{
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
                ClaimManager.claimChunk(chunk, player);
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_success");
            }
        }
    }

    private void trustRemove(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())) {
                OfflinePlayer target = context.get("target");
                chunk.removeTrusted(target.getUniqueId());
            }
        }
    }

    private void trustAdd(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())) {
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
            String owner_name = server.getOfflinePlayer(chunk.getOwner()).getName();
            if(owner_name == null){
                owner_name = chunk.getOwner().toString();
            }
            PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.info", Placeholder.unparsed("owner", owner_name));
        }
    }
}
