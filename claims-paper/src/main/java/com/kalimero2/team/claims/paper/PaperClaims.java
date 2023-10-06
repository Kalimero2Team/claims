package com.kalimero2.team.claims.paper;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.ClaimsApiHolder;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import com.kalimero2.team.claims.paper.command.CommandManager;
import com.kalimero2.team.claims.paper.listener.ChunkLoadListener;
import com.kalimero2.team.claims.paper.listener.ChunkProtectionListener;
import com.kalimero2.team.claims.paper.listener.PlayerMoveListener;
import com.kalimero2.team.claims.paper.storage.Storage;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class PaperClaims extends JavaPlugin {
    public static PaperClaims plugin;
    public ChunkBorders chunkBorders;
    private MessageUtil messageUtil;
    private Storage storage;

    @Override
    public void onLoad() {
        if (plugin == null) {
            plugin = this;
        }

        ClaimsApiHolder.setApi(null);

        saveDefaultConfig();

        this.messageUtil = new MessageUtil(new File(this.getDataFolder() + "/" + getConfig().getString("messages")));
    }

    @Override
    public void onEnable() {
        storage = new Storage(this, new File(this.getDataFolder() + "/" + getConfig().getString("database")));

        getServer().getPluginManager().registerEvents(new ChunkProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

        chunkBorders = new ChunkBorders(this);

        try {
            new CommandManager();
            getLogger().info("Commands registered");
        } catch (Exception e) {
            getLogger().warning("Failed to initialize command manager: " + e.getMessage());
        }
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public com.kalimero2.team.claims.api.ClaimsChunk getChunk(int x, int z, UUID worldUUID) {
        World world = getServer().getWorld(worldUUID);
        if (world == null) {
            return null;
        }
        return ClaimsChunk.of(world.getChunkAt(x, z));
    }
}
