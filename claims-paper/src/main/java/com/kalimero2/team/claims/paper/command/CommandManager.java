package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.command.CommandSender;

import java.util.function.UnaryOperator;


public class CommandManager extends PaperCommandManager<CommandSender> {

    public PaperClaims plugin;

    public CommandManager(PaperClaims plugin) throws Exception {
        super(plugin, CommandExecutionCoordinator.simpleCoordinator(), UnaryOperator.identity(), UnaryOperator.identity());
        this.plugin = plugin;

        if (this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                this.registerBrigadier();
                final CloudBrigadierManager<?, ?> brigManager = this.brigadierManager();
                if (brigManager != null) {
                    brigManager.setNativeNumberSuggestions(false);
                }
            } catch (final Exception e) {
                plugin.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            }
        }

        if (this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.registerAsynchronousCompletions();
        }

        new BaseChunkCommands(this).register();
        new ChunkAdminCommands(this).register();
        new ChunkBorderCommand(this).register();
        new ChunkTrustCommands(this).register();
        new ChunkIgnoredInteractablesCommands(this).register();

    }

}
