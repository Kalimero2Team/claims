package com.kalimero2.team.claims.paper.command;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class CommandHandler {
    protected final CommandManager commandManager;
    protected final PaperClaims plugin;
    protected final ClaimsApi api;
    protected final MessageUtil messageUtil;


    protected CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
        this.plugin = commandManager.plugin;
        this.api = ClaimsApi.getApi();
        this.messageUtil = plugin.getMessageUtil();
    }

    public abstract void register();

    protected boolean permissionCheck(Player player, @Nullable GroupMember groupMember, PermissionLevel permissionLevel) {
        if (ChunkAdminCommands.forcedPlayers.contains(player.getUniqueId())) {
            return true;
        }
        return groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(permissionLevel);
    }
}
