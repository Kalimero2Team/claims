package com.kalimero2.team.claims.paper.command;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.paper.PaperClaims;

public abstract class CommandHandler {
    protected final CommandManager commandManager;
    protected final PaperClaims plugin;
    protected final ClaimsApi api;

    protected CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
        this.plugin = commandManager.plugin;
        this.api = ClaimsApi.getApi();
    }

    public abstract void register();
}
