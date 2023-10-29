package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.command.argument.FlagArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChunkFlagCommands extends CommandHandler {
    protected ChunkFlagCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("flag")
                        .literal("set")
                        .argument(FlagArgument.of("flag"))
                        .argument(BooleanArgument.of("state"))
                        .handler(this::flagSet)
        );

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("flag")
                        .literal("unset")
                        .argument(FlagArgument.<CommandSender>builder("flag")
                                .withSuggestionsProvider((context, s) -> {
                                            if (context.getSender() instanceof Player player) {
                                                Claim claim = api.getClaim(player.getChunk());
                                                if (claim != null) {
                                                    Set<Flag> flags = claim.getFlags().keySet();
                                                    List<String> suggestions = new ArrayList<>();
                                                    for (Flag flag : flags) {
                                                        suggestions.add(flag.getKeyString());
                                                    }
                                                    return suggestions;
                                                }
                                            }
                                            return List.of();
                                        }
                                )
                                .build())
                        .handler(this::flagUnset)
        );

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("flag")
                        .literal("list")
                        .handler(this::flagList)
        );
    }

    private void flagList(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());

            if (claim != null) {
                Set<Flag> flags = claim.getFlags().keySet();
                if(flags.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.flag.list.empty");
                    return;
                }
                flags.forEach(flag -> {
                    boolean state = api.getFlagState(claim, flag);
                    messageUtil.sendMessage(player, "chunk.flag.list.entry",
                            Placeholder.unparsed("flag", flag.getKeyString()),
                            Placeholder.unparsed("state", String.valueOf(state))
                    );
                });
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    private void flagSet(CommandContext<CommandSender> context) {
        Flag flag = context.get("flag");

        if(flag.getPermission() != null && !context.hasPermission(flag.getPermission())){
            messageUtil.sendMessage(context.getSender(),"chunk.generic.fail_no_permission");
            return;
        }

        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());

            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                    boolean state = context.get("state");
                    api.setFlagState(claim, flag, state);
                    messageUtil.sendMessage(player, "chunk.flag.set_success", Placeholder.unparsed("flag", flag.getKeyString()), Placeholder.unparsed("state", String.valueOf(state)));
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }

    }


    private void flagUnset(CommandContext<CommandSender> context) {
        Flag flag = context.get("flag");

        if(flag.getPermission() != null && !context.hasPermission(flag.getPermission())){
            messageUtil.sendMessage(context.getSender(),"chunk.generic.fail_no_permission");
            return;
        }

        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());

            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.MODERATOR)) {
                    api.unsetFlagState(claim, flag);
                    messageUtil.sendMessage(player, "chunk.flag.unset_success", Placeholder.unparsed("flag", flag.getKeyString()));
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }
}
