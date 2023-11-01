package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.kalimero2.team.claims.paper.command.ChunkAdminCommands.forcedPlayers;

public class GroupCommands extends CommandHandler {

    protected GroupCommands(CommandManager commandManager) {
        super(commandManager);
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
                        .argument(getGroupArgument(api, "group", PermissionLevel.OWNER))
                        .handler(this::deleteGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("rename")
                        .argument(getGroupArgument(api, "group", PermissionLevel.OWNER))
                        .argument(StringArgument.of("name"))
                        .handler(this::renameGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("add")
                        .argument(getGroupArgument(api, "group", PermissionLevel.MODERATOR))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("remove")
                        .argument(getGroupArgument(api, "group", PermissionLevel.MODERATOR))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::removeMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("list")
                        .argument(getGroupArgument(api, "group", PermissionLevel.MEMBER))
                        .handler(this::listMembers)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("promote")
                        .argument(getGroupArgument(api, "group", PermissionLevel.ADMIN).build())
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::promoteMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("demote")
                        .argument(getGroupArgument(api, "group", PermissionLevel.ADMIN).build())
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::demoteMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("claims")
                        .literal("list")
                        .argument(getGroupArgument(api, "group", PermissionLevel.MEMBER).build())
                        .argument(IntegerArgument.optional("page"))
                        .handler(this::listClaims)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("claims")
                        .literal("transfer")
                        .argument(getGroupArgument(api, "group", PermissionLevel.MEMBER).build())
                        .argument(IntegerArgument.optional("amount"))
                        .argument(StringArgument.optional("confirm"))
                        .handler(this::transferClaims)
        );
    }

    private void transferClaims(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            int amount = context.getOrDefault("amount", 0);
            String confirm = context.getOrDefault("confirm", "");

            if (amount <= 0) {
                messageUtil.sendMessage(player, "group.claims.transfer.fail_amount_required");
                return;
            }

            if (!confirm.equals("confirm")) {
                messageUtil.sendMessage(player, "group.claims.transfer.fail_confirm_required",
                        Placeholder.unparsed("group", group.getName()),
                        Placeholder.unparsed("amount", String.valueOf(amount))
                );
                return;
            }

            int maxClaims = api.getPlayerGroup(player).getMaxClaims();
            int currentClaims = api.getClaims(player).size();

            int free = maxClaims - currentClaims;

            if (free < 2) {
                messageUtil.sendMessage(player, "group.claims.transfer.fail_not_enough_claims");
                return;
            }

            int newClaimAmount = group.getMaxClaims() + amount;
            api.setMaxClaims(group, newClaimAmount);

            api.setMaxClaims(api.getPlayerGroup(player), maxClaims - amount);

            messageUtil.sendMessage(player, "group.claims.transfer.success",
                    Placeholder.unparsed("group", group.getName()),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("new_amount", String.valueOf(newClaimAmount))
            );
        }
    }

    private void listClaims(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            List<Claim> claims = api.getClaims(group);

            boolean hasClaims = !claims.isEmpty();
            boolean needsPagination = claims.size() > 10;

            if (!hasClaims) {
                messageUtil.sendMessage(player, "chunk.list.empty_other", Placeholder.unparsed("target", group.getName()));
                return;
            }

            int page = context.getOrDefault("page", 1);
            int maxPage = (int) Math.ceil(claims.size() / 10.0);
            if (page > maxPage) {
                page = maxPage;
            }
            if (page < 1) {
                page = 1;
            }
            int start = (page - 1) * 10;
            int end = Math.min(start + 10, claims.size());

            messageUtil.sendMessage(player, "chunk.list.header_other",
                    Placeholder.unparsed("target", group.getName()),
                    Placeholder.unparsed("count", String.valueOf(claims.size()))
            );

            claims = claims.subList(start, end);

            for (Claim claim : claims) {
                Chunk chunk = claim.getChunk();
                Block block = chunk.getBlock(0, 0, 0);
                messageUtil.sendMessage(player, "chunk.list.entry",
                        Placeholder.component("x", Component.text(block.getX())),
                        Placeholder.component("z", Component.text(block.getZ())),
                        Placeholder.component("chunk_x", Component.text(chunk.getX())),
                        Placeholder.component("chunk_z", Component.text(chunk.getZ()))
                );
            }

            if (needsPagination) {
                Component nextPage = Component.text("");
                Component prevPage = Component.text("");

                if (page < maxPage) {
                    nextPage = Component.text(">").clickEvent(ClickEvent.runCommand("/group claims list " + group.getName() + " " + (page + 1)));
                }
                if (page > 1) {
                    prevPage = Component.text("<").clickEvent(ClickEvent.runCommand("/group claims list " + group.getName() + " " + (page - 1)));
                }
                messageUtil.sendMessage(player, "chunk.list.footer",
                        Placeholder.unparsed("page", String.valueOf(page)),
                        Placeholder.unparsed("max_page", String.valueOf(maxPage)),
                        Placeholder.component("next", nextPage),
                        Placeholder.component("prev", prevPage)
                );

            }

        }
    }

    private void createGroup(CommandContext<CommandSender> context) {
        String name = context.get("name");
        if (context.getSender() instanceof Player player) {
            int size = api.getGroups(player).size() - 1; // -1 because of the player group
            int maxGroups = plugin.getConfig().getInt("claims.max-groups");
            if (size >= maxGroups) {
                messageUtil.sendMessage(player, "group.create.fail_too_many_groups",
                        Placeholder.unparsed("max_count", String.valueOf(maxGroups)),
                        Placeholder.unparsed("count", String.valueOf(size))
                );
                return;
            }
            Group group = api.createGroup(player, name);

            if (group != null) {
                api.addGroupMember(group, player, PermissionLevel.OWNER);
                messageUtil.sendMessage(context.getSender(), "group.create.success", Placeholder.unparsed("name", name));
            } else {
                messageUtil.sendMessage(context.getSender(), "group.create.fail",
                        Placeholder.unparsed("name", name)
                );
            }
        }
    }

    private void deleteGroup(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);

            int size = api.getClaimAmount(group);
            if (size > 0) {
                messageUtil.sendMessage(player, "group.delete.fail_chunks_still_claimed",
                        Placeholder.unparsed("name", group.getName()),
                        Placeholder.unparsed("count", String.valueOf(size))
                );
                return;
            }
            if (permissionCheck(player, groupMember, PermissionLevel.OWNER)) {
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
            if (permissionCheck(player, groupMember, PermissionLevel.OWNER)) {
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
            if (permissionCheck(player, groupMember, PermissionLevel.MODERATOR)) {
                OfflinePlayer target = context.get("player");
                if (api.addGroupMember(group, target, PermissionLevel.MEMBER) != null) {
                    messageUtil.sendMessage(context.getSender(), "group.member.add.success",
                            Placeholder.unparsed("player", playerName(target)),
                            Placeholder.unparsed("group", group.getName())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.added",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", playerName(target))
                            );
                        }
                    });
                } else {
                    messageUtil.sendMessage(context.getSender(), "group.member.add.fail",
                            Placeholder.unparsed("player", playerName(target)),
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
            if (permissionCheck(player, groupMember, PermissionLevel.MODERATOR)) {
                OfflinePlayer target = context.get("player");
                if (!forcedPlayers.contains(player.getUniqueId()) && player.equals(target)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.fail_to_remove_self",
                            Placeholder.unparsed("group", group.getName())
                    );
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                if (targetMember != null && groupMember != null && !forcedPlayers.contains(player.getUniqueId()) && targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.fail_to_remove_higher",
                            Placeholder.unparsed("player", playerName(target))
                    );
                    return;
                }
                if (api.removeGroupMember(group, api.getGroupMember(group, target))) {
                    messageUtil.sendMessage(context.getSender(), "group.member.remove.success",
                            Placeholder.unparsed("player", playerName(target)),
                            Placeholder.unparsed("group", group.getName())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.removed",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", playerName(target))
                            );
                        }
                    });
                    if (target.getPlayer() != null) {
                        messageUtil.sendMessage(target.getPlayer(), "group.broadcast.removed",
                                Placeholder.unparsed("group", group.getName()),
                                Placeholder.unparsed("player", playerName(target))
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
                    Placeholder.unparsed("player", playerName(member.getPlayer())),
                    Placeholder.parsed("level", "<level_" + member.getPermissionLevel().name().toLowerCase() + ">")
            );
        }
    }

    private void promoteMember(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group group = context.get("group");
            GroupMember groupMember = api.getGroupMember(group, player);
            if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                OfflinePlayer target = context.get("player");
                if (!forcedPlayers.contains(player.getUniqueId()) && player.equals(target)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_to_promote_self");
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                assert targetMember != null;

                if (groupMember != null && !forcedPlayers.contains(player.getUniqueId()) && targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_higher_level",
                            Placeholder.unparsed("player", playerName(target))
                    );
                    return;
                }

                PermissionLevel next = targetMember.getPermissionLevel().next();

                if (groupMember != null && !forcedPlayers.contains(player.getUniqueId()) && next.isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.fail_would_equal_or_exceed_own_level",
                            Placeholder.unparsed("player", playerName(target)),
                            Placeholder.unparsed("level", next.name().toLowerCase())
                    );
                    return;
                }

                if (api.setPermissionLevel(group, targetMember, next)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.promote.success",
                            Placeholder.unparsed("player", playerName(target)),
                            Placeholder.unparsed("level", next.name().toLowerCase())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.promoted",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", playerName(target)),
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
            if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                OfflinePlayer target = context.get("player");
                if (player.equals(target) && !forcedPlayers.contains(player.getUniqueId())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.fail_to_demote_self");
                    return;
                }
                GroupMember targetMember = api.getGroupMember(group, target);

                assert targetMember != null;

                if (groupMember != null && !forcedPlayers.contains(player.getUniqueId()) && targetMember.getPermissionLevel().isHigherOrEqual(groupMember.getPermissionLevel())) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.fail_higher_level",
                            Placeholder.unparsed("player", playerName(target))
                    );
                    return;
                }

                PermissionLevel previous = targetMember.getPermissionLevel().previous();

                if (api.setPermissionLevel(group, targetMember, previous)) {
                    messageUtil.sendMessage(context.getSender(), "group.member.demote.success",
                            Placeholder.unparsed("player", playerName(target)),
                            Placeholder.unparsed("level", previous.name().toLowerCase())
                    );

                    group.getMembers().forEach(member -> {
                        Player memberPlayer = member.getPlayer().getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            messageUtil.sendMessage(memberPlayer, "group.broadcast.demoted",
                                    Placeholder.unparsed("group", group.getName()),
                                    Placeholder.unparsed("player", playerName(target)),
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

    private final HashMap<UUID, PlayerProfile> cachedPlayerProfiles = new HashMap<>();

    private @NotNull String playerName(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.getName() != null) {
            return offlinePlayer.getName();
        }

        if (cachedPlayerProfiles.containsKey(offlinePlayer.getUniqueId())) {
            PlayerProfile playerProfile = cachedPlayerProfiles.get(offlinePlayer.getUniqueId());
            String name = playerProfile.getName();
            if (name != null) {
                return name;
            }
        }
        PlayerProfile playerProfile = offlinePlayer.getPlayerProfile();
        if (playerProfile.getName() != null) {
            cachedPlayerProfiles.put(offlinePlayer.getUniqueId(), playerProfile);
            return playerProfile.getName();
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerProfile newProfile = playerProfile.update().join();
                    cachedPlayerProfiles.put(offlinePlayer.getUniqueId(), newProfile);
                    plugin.getLogger().info("Updated player profile for " + newProfile.getName());
                }
            }.runTaskAsynchronously(plugin);

            return Objects.requireNonNullElse(playerProfile.getName(), offlinePlayer.getUniqueId().toString());
        }
    }
}
