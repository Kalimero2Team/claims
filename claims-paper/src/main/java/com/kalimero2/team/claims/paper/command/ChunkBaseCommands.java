package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChunkBaseCommands extends CommandHandler {
    public ChunkBaseCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .handler(this::info)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("claim")
                        .argument(GroupCommands.getGroupArgument(api, "group", PermissionLevel.ADMIN).asOptional())
                        .handler(this::claim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("unclaim")
                        .handler(this::unclaim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("list")
                        .argument(IntegerArgument.optional("page"))
                        .handler(this::listClaims)
        );
    }

    private void listClaims(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            List<Claim> claims = api.getClaims(player);

            boolean hasClaims = !claims.isEmpty();
            boolean needsPagination = claims.size() > 10;

            if (!hasClaims) {
                plugin.getMessageUtil().sendMessage(player, "chunk.list.empty");
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

            messageUtil.sendMessage(player, "chunk.list.header",
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
                    nextPage = Component.text(">").clickEvent(ClickEvent.runCommand("/chunk list " + (page + 1)));
                }
                if (page > 1) {
                    prevPage = Component.text("<").clickEvent(ClickEvent.runCommand("/chunk list " + (page - 1)));
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

    private void unclaim(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group owner = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(owner, player);
                if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                    api.unclaimChunk(player.getChunk());
                    plugin.getMessageUtil().sendMessage(player, "chunk.unclaim.success");
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.unclaim.fail_not_owner");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.unclaim.fail_not_claimed");
            }

        }
    }

    private void claim(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                plugin.getMessageUtil().sendMessage(player, "chunk.claim.fail_already_claimed");
            } else if (!plugin.getConfig().getStringList("claims.worlds").contains(player.getWorld().getWorldFolder().getName()) && !ChunkAdminCommands.forcedPlayers.contains(player.getUniqueId())) {
                plugin.getMessageUtil().sendMessage(player, "chunk.claim.fail_world_not_claimable");
            } else {
                Group playerGroup = api.getPlayerGroup(player);
                Group group = context.getOrDefault("group", playerGroup);
                int claims = api.getClaimAmount(group);
                int maxClaims = group.getMaxClaims();

                GroupMember member = api.getGroupMember(group, player);

                if (member == null || !member.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_not_owner");
                    return;
                }


                if (claims < maxClaims) {
                    if (api.claimChunk(player.getChunk(), group)) {
                        if (group.isPlayer()) {
                            plugin.getMessageUtil().sendMessage(player, "chunk.claim.success");
                        } else {
                            plugin.getMessageUtil().sendMessage(player, "chunk.claim.success_group",
                                    Placeholder.unparsed("group", group.getName())
                            );
                        }
                    } else {
                        plugin.getLogger().warning("Failed to claim chunk for " + player.getName() + " at " + player.getChunk().getX() + " " + player.getChunk().getZ());
                    }
                } else {
                    TagResolver.Single count = Placeholder.unparsed("count", String.valueOf(claims));
                    TagResolver.Single max = Placeholder.unparsed("max_count", String.valueOf(group.getMaxClaims()));
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim.fail_too_many_claims", count, max);
                }

            }
        }
    }


    private void info(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            MessageUtil messageUtil = plugin.getMessageUtil();

            Chunk chunk = player.getChunk();
            messageUtil.sendMessage(player, "chunk.info.info", Placeholder.component("chunk_x", Component.text(chunk.getX())), Placeholder.component("chunk_z", Component.text(chunk.getZ())));

            if (claim != null) {
                Group owner = claim.getOwner();
                messageUtil.sendMessage(player, "chunk.info.claimed_true");

                if (owner.isPlayer()) {
                    messageUtil.sendMessage(player, "chunk.info.owner", Placeholder.unparsed("player", owner.getName()));
                } else {
                    messageUtil.sendMessage(player, "chunk.info.owner_group", Placeholder.unparsed("group", owner.getName()));
                }

                if(player.hasPermission("claims.admin.info")){
                    SimpleDateFormat dateFormat = new SimpleDateFormat(messageUtil.getMessageBundle().getString("generic.date_format"));
                    messageUtil.sendMessage(player, "chunk.info.claimed_since",
                            Placeholder.unparsed("date", dateFormat.format(new Date(claim.getClaimedSince())))
                    );
                }

                List<Group> players = claim.getMembers().stream().filter(Group::isPlayer).toList();
                List<Group> groups = claim.getMembers().stream().filter(group -> !group.isPlayer()).toList();

                if (!players.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.info.trusted_player_header");
                    for (Group memberPlayer : players) {
                        if (memberPlayer.isPlayer()) {
                            messageUtil.sendMessage(player, "chunk.info.trusted_player", Placeholder.unparsed("player", memberPlayer.getName()));
                        }
                    }
                }
                if (!groups.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.info.trusted_group_header");
                    for (Group memberGroup : groups) {
                        if (!memberGroup.isPlayer()) {
                            messageUtil.sendMessage(player, "chunk.info.trusted_group", Placeholder.unparsed("group", memberGroup.getName()));
                        }
                    }
                }
            } else {
                messageUtil.sendMessage(player, "chunk.info.claimed_false");
            }

        }
    }
}
