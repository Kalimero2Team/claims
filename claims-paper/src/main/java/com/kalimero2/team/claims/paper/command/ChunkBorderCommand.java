package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkBorderCommand extends CommandHandler {
    public ChunkBorderCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("border")
                        .handler(this::toggleBorder)
        );
        commandManager.command(
                commandManager.commandBuilder("cb", "chunkborder")
                        .handler(this::toggleBorder)
        );
    }

    private void toggleBorder(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            if (ChunkBorders.show_border.contains(player)) {
                ChunkBorders.show_border.remove(player);
                plugin.getMessageUtil().sendMessage(player, "chunk.border_off");
            } else {
                ChunkBorders.show_border.add(player);
                plugin.getMessageUtil().sendMessage(player, "chunk.border_on");
            }
        }
    }

}
