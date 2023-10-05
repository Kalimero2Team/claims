package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;


public class ChunkLimitCommands extends CommandHandler {
    public ChunkLimitCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("limit")
                        .literal("set")
                        .permission("claims.admin.limit.set")
                        .argument(OfflinePlayerArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::setLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("limit")
                        .literal("get")
                        .permission("claims.admin.limit.get")
                        .argument(OfflinePlayerArgument.of("target"))
                        .handler(this::getLimit)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("limit")
                        .literal("add")
                        .permission("claims.admin.limit.get")
                        .argument(OfflinePlayerArgument.of("target"))
                        .argument(IntegerArgument.of("limit"))
                        .handler(this::addToLimit)
        );
    }

    private void addToLimit(CommandContext<CommandSender> context) {
        OfflinePlayer target = context.get("target");
        int limit = context.get("limit");
        String name = target.getName();
        if (name == null) {
            name = target.getUniqueId().toString();
        }
    }

    private void getLimit(CommandContext<CommandSender> context) {
        OfflinePlayer target = context.get("target");

        String name = target.getName();
        if (name == null) {
            name = target.getUniqueId().toString();
        }
    }

    private void setLimit(CommandContext<CommandSender> context) {
        OfflinePlayer target = context.get("target");
        int limit = context.get("limit");

        String name = target.getName();
        if (name == null) {
            name = target.getUniqueId().toString();
        }
    }

}
