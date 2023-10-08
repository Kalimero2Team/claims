package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.command.argument.GroupArgument;
import org.bukkit.command.CommandSender;

public class GroupCommands extends CommandHandler {

    protected GroupCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("create")
                        .permission("claims.group.create")
                        .argument(StringArgument.of("name"))
                        .handler(this::createGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("delete")
                        .permission("claims.group.delete")
                        .argument(GroupArgument.of("target"))
                        .handler(this::deleteGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("rename")
                        .permission("claims.group.rename")
                        .argument(GroupArgument.of("target"))
                        .argument(StringArgument.of("name"))
                        .handler(this::renameGroup)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("add")
                        .permission("claims.group.member.add")
                        .argument(GroupArgument.of("target"))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::addMember)
        );
        commandManager.command(
                commandManager.commandBuilder("group")
                        .literal("member")
                        .literal("remove")
                        .permission("claims.group.member.remove")
                        .argument(GroupArgument.of("target"))
                        .argument(OfflinePlayerArgument.of("player"))
                        .handler(this::removeMember)
        );


    }

    private void createGroup(CommandContext<CommandSender> commandSenderCommandContext) {
    }

    private void deleteGroup(CommandContext<CommandSender> commandSenderCommandContext) {
    }

    private void renameGroup(CommandContext<CommandSender> commandSenderCommandContext) {
    }

    private void addMember(CommandContext<CommandSender> commandSenderCommandContext) {
    }

    private void removeMember(CommandContext<CommandSender> commandSenderCommandContext) {
    }

}
