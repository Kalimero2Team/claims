package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandHandler{
    protected SpawnCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("bed").handler(this::spawn)
        );
    }

    private void spawn(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            player.teleport(player.getWorld().getSpawnLocation());
            PaperClaims.plugin.getMessageUtil().sendMessage(player,"teleported_to_spawn");
        }
    }
}
