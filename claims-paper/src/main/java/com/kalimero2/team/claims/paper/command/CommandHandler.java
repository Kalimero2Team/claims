package com.kalimero2.team.claims.paper.command;


public abstract class CommandHandler {
    protected final CommandManager commandManager;

    protected CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public abstract void register();
}
