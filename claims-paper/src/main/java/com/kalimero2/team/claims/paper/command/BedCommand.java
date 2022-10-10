package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BedCommand extends CommandHandler {
    protected BedCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("bed").handler(this::bed)
        );
    }

    private void bed(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            player.teleport(player.getBedLocation());
            PaperClaims.plugin.getMessageUtil().sendMessage(player,"teleported_to_bed");
        }
        }
}
