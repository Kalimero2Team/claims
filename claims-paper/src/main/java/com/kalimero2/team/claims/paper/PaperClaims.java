package com.kalimero2.team.claims.paper;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.ClaimsApiHolder;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.listener.ChunkLoadListener;
import com.kalimero2.team.claims.paper.listener.ChunkProtectionListener;
import com.kalimero2.team.claims.paper.listener.PlayerMoveListener;
import com.kalimero2.team.claims.paper.storage.Storage;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PaperClaims extends JavaPlugin {
    public static ClaimManager api;
    public ChunkBorders chunkBorders;
    private MessageUtil messageUtil;
    private Storage storage;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        this.messageUtil = new MessageUtil(this, new File(this.getDataFolder() + "/" + getConfig().getString("messages")));
    }

    @Override
    public void onEnable() {
        storage = new Storage(this, new File(this.getDataFolder() + "/" + getConfig().getString("database")));
        api = new ClaimManager(storage);
        ClaimsApiHolder.setApi(api);

        getServer().getPluginManager().registerEvents(new ChunkProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

        chunkBorders = new ChunkBorders(this);

        /*
        try {
            new CommandManager();
            getLogger().info("Commands registered");
        } catch (Exception e) {
            getLogger().warning("Failed to initialize command manager: " + e.getMessage());
        }
         */
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}
