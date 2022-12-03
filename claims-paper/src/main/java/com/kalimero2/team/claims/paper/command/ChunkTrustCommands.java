package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import com.kalimero2.team.claims.paper.util.SerializableChunk;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

import static com.kalimero2.team.claims.paper.claim.ClaimManager.forcedPlayers;

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
        /*commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("addAll")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::trustAddAll)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("removeAll")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::trustRemoveAll)
        );*/
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


    private void trustAddAll(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Player target = context.get("target");
            HashSet<SerializableChunk> chunks = ClaimManager.getExtraPlayerData(player).chunks;
            chunks.forEach(chunk -> {
                ClaimsChunk claimsChunk = ClaimsChunk.of(chunk.toBukkitChunk());
                if (claimsChunk.hasOwner()) {
                    if (claimsChunk.getOwner().equals(player.getUniqueId())) {
                        claimsChunk.addTrusted(target.getUniqueId());
                    }
                }
            });
            PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.trustAddAll", Placeholder.unparsed("target", target.getName()));
        }
    }

    private void trustRemoveAll(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Player target = context.get("target");
            HashSet<SerializableChunk> chunks = ClaimManager.getExtraPlayerData(player).chunks;
            chunks.forEach(chunk -> {
                ClaimsChunk claimsChunk = ClaimsChunk.of(chunk.toBukkitChunk());
                if (claimsChunk.hasOwner()) {
                    if (claimsChunk.getOwner().equals(player.getUniqueId())) {
                        claimsChunk.removeTrusted(target.getUniqueId());
                    }
                }
            });
            PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.trustRemoveAll", Placeholder.unparsed("target", target.getName()));
        }
    }

}
