package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class ChunkAdminCommands extends CommandHandler {
    public ChunkAdminCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("listother")
                        .permission("claims.admin.listother")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.optional("page"))
                        .handler(this::listClaimsOther)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("setowner")
                        .permission("claims.admin.setOwner")
                        .argument(GroupArgument.of("target"))
                        .handler(this::setOwner)
        );

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("set")
                        .permission("claims.admin.limit.set")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::setLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("get")
                        .permission("claims.admin.limit.get")
                        .argument(GroupArgument.of("target"))
                        .handler(this::getLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("admin")
                        .literal("limit")
                        .literal("add")
                        .permission("claims.admin.limit.get")
                        .argument(GroupArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::addToLimit)
        );


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
            claims = claims.subList(start, end);

            messageUtil.sendMessage(player, "chunk.list.header_other", Placeholder.unparsed("target", target.getName()));
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

                if (page < maxPage) {
                    nextPage = Component.text(">").clickEvent(ClickEvent.runCommand("/chunk admin listother " + target.getName() + " " + (page + 1)));
                }
                if (page > 1) {
                    prevPage = Component.text("<").clickEvent(ClickEvent.runCommand("/chunk admin listother " + target.getName() + " " + (page - 1)));
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
}
