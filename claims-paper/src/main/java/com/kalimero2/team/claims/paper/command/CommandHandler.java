package com.kalimero2.team.claims.paper.command;

import com.kalimero2.team.claims.paper.PaperClaims;

public abstract class CommandHandler {
    protected final CommandManager commandManager;
    protected final PaperClaims plugin;

    protected CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
        this.plugin = commandManager.plugin;
    }

    public abstract void register();
}
