package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
                        .argument(GroupArgument.of("target"))
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("remove")
                        .argument(GroupArgument.of("target"))
                        .handler(this::removeMember)
        );
    }


    private void addMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                    Group target = context.get("target");
                    if (claim.getMembers().contains(target)) {
                        // TODO: Add Group name
                        plugin.getMessageUtil().sendMessage(player, "chunk.claim_add_fail_already_added", Placeholder.unparsed("target", target.toString()));
                        return;
                    }
                    api.addGroupToClaim(claim, target);
                    // TODO: Add Group name
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim_add_success", Placeholder.unparsed("target", target.toString()));
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }
        }
    }

    private void removeMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                    Group target = context.get("target");
                    if (!claim.getMembers().contains(target)) {
                        // TODO: Add Group name
                        plugin.getMessageUtil().sendMessage(player, "chunk.claim_remove_fail_already_removed", Placeholder.unparsed("target", target.toString()));
                        return;
                    }
                    api.removeGroupFromClaim(claim, target);
                    // TODO: Add Group name
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim_remove_success", Placeholder.unparsed("target", target.toString()));
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }
        }
    }


}
