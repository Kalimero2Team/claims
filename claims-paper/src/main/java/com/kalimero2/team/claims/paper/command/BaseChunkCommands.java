package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BaseChunkCommands extends CommandHandler {
    public BaseChunkCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk").handler(this::info)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("claim")
                        .argument(GroupArgument.optional("group"))
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
                plugin.getMessageUtil().sendMessage(player, "chunk.list_empty");
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
            claims = claims.subList(start, end);

            messageUtil.sendMessage(player, "chunk.list_start");
            for (Claim claim : claims) {
                Chunk chunk = claim.getChunk();
                Block block = chunk.getBlock(0, 0, 0);
                messageUtil.sendMessage(player, "chunk.list_entry",
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
                messageUtil.sendMessage(player, "chunk.paginated_list_footer",
                        Placeholder.unparsed("page", String.valueOf(page)),
                        Placeholder.unparsed("max_page", String.valueOf(maxPage)),
                        Placeholder.component("next", nextPage),
                        Placeholder.component("prev", prevPage)
                );

            }

        }
    }

    private void unClaim(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group owner = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(owner, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.OWNER)) {
                    api.unclaimChunk(player.getChunk());
                    plugin.getMessageUtil().sendMessage(player, "chunk.unclaim_success");
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.unclaim_fail_not_owner");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.unclaim_fail_not_claimed");
            }

        }
    }

    private void claim(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                plugin.getMessageUtil().sendMessage(player, "chunk.claim_fail_already_claimed");
            } else if (!plugin.getConfig().getStringList("claims.worlds").contains(player.getWorld().getWorldFolder().getName())) {
                plugin.getMessageUtil().sendMessage(player, "chunk.claim_fail_world_not_claimable");
            } else {
                Group playerGroup = api.getPlayerGroup(player);
                Group group = context.getOrDefault("group", playerGroup);
                int claims = api.getClaims(group).size();
                int maxClaims = group.getMaxClaims();


                if (api.claimChunk(player.getChunk(), group) && claims <= maxClaims) {
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim_success");
                    // TODO: Claim Success Message with Group Name
                } else {
                    TagResolver.Single count = Placeholder.unparsed("count", String.valueOf(claims));
                    TagResolver.Single max = Placeholder.unparsed("max_count", String.valueOf(group.getMaxClaims()));
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim_fail_too_many_claims", count, max);
                }

            }
        }
    }


    private void info(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            Server server = plugin.getServer();
            MessageUtil messageUtil = plugin.getMessageUtil();

            Chunk chunk = player.getChunk();
            messageUtil.sendMessage(player, "chunk.info", Placeholder.component("chunk_x", Component.text(chunk.getX())), Placeholder.component("chunk_z", Component.text(chunk.getZ())));

            if (claim != null) {
                Group owner = claim.getOwner();
                messageUtil.sendMessage(player, "chunk.claimed_true");

                messageUtil.sendMessage(player, "chunk.claim_owner", Placeholder.unparsed("player", owner.getName()));

                List<Group> players = claim.getMembers().stream().filter(Group::isPlayer).toList();
                List<Group> groups = claim.getMembers().stream().filter(group -> !group.isPlayer()).toList();

                if (!players.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.claim_trusted_player_header");
                    for (Group memberPlayer : players) {
                        if (memberPlayer.isPlayer()) {
                            messageUtil.sendMessage(player, "chunk.claim_trusted_player", Placeholder.unparsed("player", memberPlayer.getName()));
                        }
                    }
                }
                if (!groups.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.claim_trusted_group_header");
                    for (Group memberGroup : groups) {
                        if (!memberGroup.isPlayer()) {
                            messageUtil.sendMessage(player, "chunk.claim_trusted_group", Placeholder.unparsed("group", memberGroup.getName()));
                        }
                    }
                }


            } else {
                messageUtil.sendMessage(player, "chunk.claimed_false");
            }

        }
    }
}
