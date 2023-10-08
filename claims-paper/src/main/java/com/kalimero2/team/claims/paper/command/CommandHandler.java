package com.kalimero2.team.claims.paper.command;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.util.MessageUtil;

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
}
