package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.paper.command.argument.FlagArgument;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import com.kalimero2.team.claims.paper.command.argument.PlayerGroupArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChunkAdminCommands extends CommandHandler {

    public static final ArrayList<UUID> forcedPlayers = new ArrayList<>();

    public ChunkAdminCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("listother")
                        .literal("group")
                        .permission("claims.admin.listother")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.optional("page"))
                        .handler(this::listClaimsOther)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("listother")
                        .literal("player")
                        .permission("claims.admin.listother")
                        .argument(PlayerGroupArgument.of("target"))
                        .argument(IntegerArgument.optional("page"))
                        .handler(this::listClaimsOther)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("setowner")
                        .literal("group")
                        .permission("claims.admin.setowner")
                        .argument(GroupArgument.of("target"))
                        .handler(this::setOwner)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("setowner")
                        .literal("player")
                        .permission("claims.admin.setowner")
                        .argument(PlayerGroupArgument.of("target"))
                        .handler(this::setOwner)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("set")
                        .literal("group")
                        .permission("claims.admin.limit.set")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::setLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("set")
                        .literal("player")
                        .permission("claims.admin.limit.set")
                        .argument(PlayerGroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::setLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("get")
                        .literal("group")
                        .permission("claims.admin.limit.get")
                        .argument(GroupArgument.of("target"))
                        .handler(this::getLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("get")
                        .literal("player")
                        .permission("claims.admin.limit.get")
                        .argument(PlayerGroupArgument.of("target"))
                        .handler(this::getLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("add")
                        .literal("group")
                        .permission("claims.admin.limit.set")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::addToLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("add")
                        .literal("player")
                        .permission("claims.admin.limit.set")
                        .argument(PlayerGroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::addToLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("batch")
                        .literal("flag")
                        .literal("set")
                        .permission("claims.admin.batch.flag")
                        .argument(GroupArgument.of("target"))
                        .argument(FlagArgument.of("flag"))
                        .argument(BooleanArgument.of("state"))
                        .handler(this::batchFlagSet)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("batch")
                        .literal("flag")
                        .literal("unset")
                        .permission("claims.admin.batch.flag")
                        .argument(GroupArgument.of("target"))
                        .argument(FlagArgument.of("flag"))
                        .handler(this::batchFlagUnset)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("force")
                        .permission("claims.admin.force")
                        .handler(this::toggleForceMode)
        );
    }

    private void toggleForceMode(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            if (forcedPlayers.contains(player.getUniqueId())) {
                forcedPlayers.remove(player.getUniqueId());
                messageUtil.sendMessage(player, "chunk.admin.force.false");
            } else {
                forcedPlayers.add(player.getUniqueId());
                messageUtil.sendMessage(player, "chunk.admin.force.true");
            }
        }
    }

    private void listClaimsOther(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Group target = context.get("target");
            List<Claim> claims = api.getClaims(target);

            boolean hasClaims = !claims.isEmpty();
            boolean needsPagination = claims.size() > 10;

            if (!hasClaims) {
                messageUtil.sendMessage(player, "chunk.list.empty_other", Placeholder.unparsed("target", target.getName()));
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
                    Placeholder.unparsed("target", target.getName()),
                    Placeholder.unparsed("count", String.valueOf(claims.size()))
            );

            claims = claims.subList(start, end);

            for (Claim claim : claims) {
                Chunk chunk = claim.getChunk();
                Block block = chunk.getBlock(0, 0, 0);
                Component message = messageUtil.getMessage("chunk.list.entry",
                        Placeholder.component("x", Component.text(block.getX())),
                        Placeholder.component("z", Component.text(block.getZ())),
                        Placeholder.component("chunk_x", Component.text(chunk.getX())),
                        Placeholder.component("chunk_z", Component.text(chunk.getZ())));
                int y = block.getWorld().getHighestBlockYAt(block.getX(), block.getZ()) + 1;
                message = message.clickEvent(ClickEvent.runCommand("/tp " + block.getX() + " " + y + " " + block.getZ()));
                message = message.hoverEvent(HoverEvent.showText(messageUtil.getMessage("chunk.list.tp_hover")));
                player.sendMessage(message);
            }

            if (needsPagination) {
                Component nextPage = Component.text("");
                Component prevPage = Component.text("");

                String command = "/chunk admin listother " + (target.isPlayer() ? "player " : "group ");
                if (page < maxPage) {
                    nextPage = Component.text(">").clickEvent(ClickEvent.runCommand(command + target.getName() + " " + (page + 1)));
                }
                if (page > 1) {
                    prevPage = Component.text("<").clickEvent(ClickEvent.runCommand(command + target.getName() + " " + (page - 1)));
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

    private void setOwner(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group target = context.get("target");
                api.setOwner(player.getChunk(), target);
                plugin.getMessageUtil().sendMessage(player, "chunk.admin.set_owner", Placeholder.unparsed("target", target.getName()));
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    private void addToLimit(CommandContext<CommandSender> context) {
        Group target = context.get("target");
        int oldLimit = target.getMaxClaims();
        int limit = context.get("limit");

        int maxClaims = target.getMaxClaims();
        api.setMaxClaims(target, maxClaims + limit);

        plugin.getMessageUtil().sendMessage(context.getSender(), "chunk.admin.limit.add",
                Placeholder.unparsed("count", String.valueOf(limit)),
                Placeholder.unparsed("old_count", String.valueOf(oldLimit)),
                Placeholder.unparsed("target", target.getName())
        );
    }

    private void getLimit(CommandContext<CommandSender> context) {
        Group target = context.get("target");

        int maxClaims = target.getMaxClaims();

        plugin.getMessageUtil().sendMessage(context.getSender(), "chunk.admin.limit.get",
                Placeholder.unparsed("count", String.valueOf(maxClaims)),
                Placeholder.unparsed("target", target.getName())
        );
    }

    private void setLimit(CommandContext<CommandSender> context) {
        Group target = context.get("target");
        int oldLimit = target.getMaxClaims();
        int limit = context.get("limit");

        api.setMaxClaims(target, limit);

        plugin.getMessageUtil().sendMessage(context.getSender(), "chunk.admin.limit.set",
                Placeholder.unparsed("count", String.valueOf(limit)),
                Placeholder.unparsed("old_count", String.valueOf(oldLimit)),
                Placeholder.unparsed("target", target.getName())
        );
    }


    private void batchFlagSet(CommandContext<CommandSender> context) {
        Flag flag = context.get("flag");
        Group group = context.get("target");
        Boolean state = context.get("state");

        if (flag.getPermission() != null && !context.hasPermission(flag.getPermission())) {
            messageUtil.sendMessage(context.getSender(), "chunk.generic.fail_no_permission");
            return;
        }

        api.getClaims(group).forEach(claim -> {
            if (api.setFlagState(claim, flag, state)) {
                messageUtil.sendMessage(context.getSender(), "chunk.flag.set_success", Placeholder.unparsed("flag", flag.getKeyString()), Placeholder.unparsed("state", String.valueOf(state)));
            } else {
                context.getSender().sendMessage("Couldnt set flag :(");
            }
        });
    }

    private void batchFlagUnset(CommandContext<CommandSender> context) {
        Flag flag = context.get("flag");
        Group group = context.get("target");

        if (flag.getPermission() != null && !context.hasPermission(flag.getPermission())) {
            messageUtil.sendMessage(context.getSender(), "chunk.generic.fail_no_permission");
            return;
        }

        api.getClaims(group).forEach(claim -> {
            api.unsetFlagState(claim, flag);
            messageUtil.sendMessage(context.getSender(), "chunk.flag.unset_success", Placeholder.unparsed("flag", flag.getKeyString()));
        });
    }
}
