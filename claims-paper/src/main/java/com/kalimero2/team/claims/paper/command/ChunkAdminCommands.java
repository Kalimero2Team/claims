package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ChunkAdminCommands extends CommandHandler {
    public ChunkAdminCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
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
                        .literal("wipedata")
                        .senderType(Player.class)
                        .permission("claims.admin.wipe")
                        .handler(this::wipeData)
        );
    }

    private void wipeData(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk claimsChunk = ClaimsChunk.of(player.getChunk());
            claimsChunk.setOwner(null);
            claimsChunk.clearTrusted();
            claimsChunk.setClaimed(false);
            claimsChunk.setProperties(null);
            claimsChunk.clearIgnoredInteractables();
            PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.wipedata");
        }
    }

    private void teamClaim(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed()) {
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_fail_already_claimed");
            } else {
                chunk.setClaimed(true);
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_success");
            }
        }
    }

    private void listClaimsOther(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            OfflinePlayer target = context.get("target");
        }
    }

    private void toggleForceMode(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            if (forcedPlayers.contains(player.getUniqueId())) {
                forcedPlayers.remove(player.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.force_off");
            } else {
                forcedPlayers.add(player.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.force_on");
            }
        }
    }

    private void setOwner(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            if (context.getOptional("target").isPresent()) {
                OfflinePlayer target = context.get("target");
                ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
                chunk.setOwner(target.getUniqueId());
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.set_owner", target_placeholder);
            } else {
                ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
                chunk.setOwner(null);
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.set_owner", Placeholder.unparsed("target", "null"));
            }
        }
    }
}
