package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GroupCommands extends CommandHandler {

    protected GroupCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("create")
                        .argument(StringArgument.of("name"))
                        .handler(this::createGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("delete")
                        .argument(getGroupArgument(api,"group", PermissionLevel.OWNER))
                        .handler(this::deleteGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("rename")
                        .argument(getGroupArgument(api,"group", PermissionLevel.OWNER))
                        .argument(StringArgument.of("name"))
                        .handler(this::renameGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("add")
                        .argument(getGroupArgument(api,"group", PermissionLevel.MODERATOR))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("remove")
                        .argument(getGroupArgument(api,"group", PermissionLevel.MODERATOR))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::removeMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("list")
                        .argument(getGroupArgument(api,"group", PermissionLevel.MEMBER))
                        .handler(this::listMembers)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("promote")
                        .argument(getGroupArgument(api,"group", PermissionLevel.ADMIN).build())
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::promoteMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("demote")
                        .argument(getGroupArgument(api,"group", PermissionLevel.ADMIN).build())
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::demoteMember)
        );


    }


    public static CommandArgument.Builder<CommandSender, Group> getGroupArgument(ClaimsApi api, String name, PermissionLevel permissionLevel) {
        return GroupArgument.<CommandSender>builder(name)
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    if (context.getSender() instanceof Player player) {
                        for (Group group : api.getGroups(player)) {
                            GroupMember groupMember = api.getGroupMember(group, player);
                            if (!group.isPlayer() && groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(permissionLevel)) {
                                suggestions.add(group.getName());
                            }
                        }
                    }
                    return suggestions;
                });
    }

    private void createGroup(CommandContext<CommandSender> context) {
        String name = context.get("name");
        if (context.getSender() instanceof Player player) {
            Group group = api.createGroup(player, name);
            if (group != null) {
                api.addGroupMember(group, player, PermissionLevel.OWNER);
                messageUtil.sendMessage(context.getSender(), "group.create.success", Placeholder.unparsed("name", name));
            } else {
                messageUtil.sendMessage(context.getSender(), "group.create.fail");
            }
        }
    }

    private void deleteGroup(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.OWNER)) {
                if (api.deleteGroup(group)) {
                    messageUtil.sendMessage(player, "group.delete.success", Placeholder.unparsed("name", group.getName()));
                } else {
                    messageUtil.sendMessage(player, "group.delete.fail");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }

        }
    }

    private void renameGroup(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.OWNER)) {
                String oldName = group.getName();
                String name = context.get("name");
                if (api.renameGroup(group, name)) {
                    messageUtil.sendMessage(context.getSender(), "group.rename.success", Placeholder.unparsed("old_name", oldName), Placeholder.unparsed("new_name", name));
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.rename.fail");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }
        }
    }

    private void addMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                OfflinePlayer target = context.get("player");
                if (api.addGroupMember(group, target, PermissionLevel.MEMBER) != null) {
                    messageUtil.sendMessage(context.getSender(), "group.member.add.success",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("group", group.getName())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if(memberPlayer != null && !memberPlayer.equals(player)){
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.added",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", target.getName())
                            );
                        }
                    });
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.member.add.fail",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("group", group.getName())
                    );
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }
        }
    }

    private void removeMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                OfflinePlayer target = context.get("player");
                if (player.equals(target)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.fail_to_remove_self");
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                if (targetMember != null && targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.fail_to_remove_higher",
                            Placeholder.unparsed("player", target.getName())
                    );
                    return;
                }
                if (api.removeGroupMember(group, api.getGroupMember(group, target))) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.success",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("group", group.getName())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if(memberPlayer != null && !memberPlayer.equals(player)){
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.removed",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", target.getName())
                            );
                        }
                    });
                    if(target.getPlayer() != null){
                        messageUtil.sendMessage(target.getPlayer(), "group.broadcast.removed",
                                Placeholder.unparsed("group", group.getName()),
                                Placeholder.unparsed("player", target.getName())
                        );
                    }
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.fail");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }
        }
    }

    private void listMembers(CommandContext<CommandSender> context) {
        Group group = context.get("group");
        messageUtil.sendMessage(context.getSender(), "group.member.list.header", Placeholder.unparsed("group", group.getName()));
        for (GroupMember member : group.getMembers()) {
            messageUtil.sendMessage(context.getSender(), "group.member.list.entry",
                    Placeholder.unparsed("player", member.getPlayer().getName()),
                    Placeholder.unparsed("level", member.getPermissionLevel().name().toLowerCase())
            );
        }
    }

    private void promoteMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                OfflinePlayer target = context.get("player");
                if (player.equals(target)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_to_promote_self");
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                assert targetMember != null;

                if (targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_higher_level", Placeholder.unparsed("player", target.getName()));
                    return;
                }

                PermissionLevel next = targetMember.getPermissionLevel().next();

                if (next.isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_would_equal_or_exceed_own_level",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("level", next.name().toLowerCase())
                    );
                    return;
                }

                if (api.setPermissionLevel(group, targetMember, next)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.success",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("level", next.name().toLowerCase())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if(memberPlayer != null && !memberPlayer.equals(player)){
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.promoted",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", target.getName()),
                                    Placeholder.unparsed("level", next.name().toLowerCase())
                            );
                        }
                    });
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }
        }
    }

    private void demoteMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                OfflinePlayer target = context.get("player");
                if (player.equals(target)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.fail_to_demote_self");
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                assert targetMember != null;

                if (targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.fail_higher_level",
                            Placeholder.unparsed("player", target.getName())
                    );
                    return;
                }

                PermissionLevel previous = targetMember.getPermissionLevel().previous();

                if (api.setPermissionLevel(group, targetMember, previous)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.success",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("level", previous.name().toLowerCase())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if(memberPlayer != null && !memberPlayer.equals(player)){
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.demoted",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", target.getName()),
                                    Placeholder.unparsed("level", previous.name().toLowerCase())
                            );
                        }
                    });
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.fail");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
            }
        }
    }
}
