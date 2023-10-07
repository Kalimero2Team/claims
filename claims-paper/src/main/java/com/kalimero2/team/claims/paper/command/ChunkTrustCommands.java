package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkTrustCommands extends CommandHandler {
    public ChunkTrustCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
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
    }


    private void trustAdd(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed() && ((chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId())) || forcedPlayers.contains(player.getUniqueId()) || (!chunk.hasOwner() && player.hasPermission("claims.admin.teamclaim")))) {
                OfflinePlayer target = context.get("target");
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                if (chunk.getTrustedList().contains(target.getUniqueId())) {
                    PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_add_fail_already_added", target_placeholder);
                    return;
                }
                chunk.addTrusted(target.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_add_success", target_placeholder);
            }
        }
    }

    private void trustRemove(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed() && ((chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId())) || forcedPlayers.contains(player.getUniqueId()) || (!chunk.hasOwner() && player.hasPermission("claims.admin.teamclaim")))) {
                OfflinePlayer target = context.get("target");
                TagResolver.Single target_placeholder = Placeholder.unparsed("target", target.getName() != null ? target.getName() : target.getUniqueId().toString());
                if (!chunk.getTrustedList().contains(target.getUniqueId())) {
                    PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_remove_fail_already_removed", target_placeholder);
                    return;
                }
                chunk.removeTrusted(target.getUniqueId());
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.claim_remove_success", target_placeholder);
            }
        }
    }


}
