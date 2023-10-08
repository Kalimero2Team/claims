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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Server;
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
            // TODO: pagination

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

                if (api.claimChunk(player.getChunk(), group)) {
                    plugin.getMessageUtil().sendMessage(player, "chunk.claim_success");
                    // TODO: Claim Success Message with Group Name
                } else {
                    int claims = api.getClaims(group).size();
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

                // TODO: Get Group Name
                // TODO: Differentiate between Group and Player
                messageUtil.sendMessage(player, "chunk.claim_owner", Placeholder.unparsed("player", owner.toString()));

                List<Group> members = claim.getMembers();
                if (!members.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.claim_trusted_start");
                    for (Group memberGroup : members) {
                        if (memberGroup.isPlayer()) {
                            // TODO: Get Player (Group) Name
                            messageUtil.sendMessage(player, "chunk.claim_trusted_player", Placeholder.unparsed("player", memberGroup.toString()));
                        } else {
                            // TODO: Get Group Name
                            messageUtil.sendMessage(player, "chunk.claim_trusted_group", Placeholder.unparsed("group", memberGroup.toString()));
                        }
                    }
                }
            } else {
                messageUtil.sendMessage(player, "chunk.claimed_false");
            }

        }
    }
}
