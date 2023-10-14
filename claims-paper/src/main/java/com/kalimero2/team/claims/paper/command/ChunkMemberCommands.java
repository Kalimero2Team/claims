package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChunkMemberCommands extends CommandHandler {
    public ChunkMemberCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        CommandArgument.Builder<@NonNull CommandSender, @NonNull Group> playerGroupArgument = GroupArgument.<CommandSender>builder("target")
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    for (Group group : api.getGroups()) {
                        if (group.isPlayer()) {
                            if (group.getMembers().get(0).getPlayer().isOnline()) {
                                suggestions.add(group.getName());
                            }
                        }
                    }
                    return suggestions;
                });

        CommandArgument.Builder<@NonNull CommandSender, @NonNull Group> groupsArgument = GroupArgument.<CommandSender>builder("target")
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    for (Group group : api.getGroups()) {
                        if (!group.isPlayer()) {
                            suggestions.add(group.getName());
                        }
                    }
                    return suggestions;
                });

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("add")
                        .literal("player")
                        .argument(playerGroupArgument)
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("add")
                        .literal("group")
                        .argument(groupsArgument)
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("remove")
                        .literal("player")
                        .argument(playerGroupArgument)
                        .handler(this::removeMember)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("remove")
                        .literal("group")
                        .argument(groupsArgument)
                        .handler(this::removeMember)
        );
    }


    private void addMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                    Group target = context.get("target");
                    if (claim.getMembers().contains(target)) {
                        plugin.getMessageUtil().sendMessage(player, "chunk.add.fail_already_added",
                                Placeholder.unparsed("target", target.getName())
                        );
                        return;
                    }
                    api.addGroupToClaim(claim, target);
                    plugin.getMessageUtil().sendMessage(player, "chunk.add.success",
                            Placeholder.unparsed("target", target.getName())
                    );
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    private void removeMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                    Group target = context.get("target");
                    if (!claim.getMembers().contains(target)) {
                        plugin.getMessageUtil().sendMessage(player, "chunk.remove.fail_already_removed",
                                Placeholder.unparsed("target", target.getName())
                        );
                        return;
                    }
                    api.removeGroupFromClaim(claim, target);
                    plugin.getMessageUtil().sendMessage(player, "chunk.remove.success",
                            Placeholder.unparsed("target", target.getName())
                    );
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }


}
