package com.kalimero2.team.claims.paper;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.paper.command.CommandManager;
import com.kalimero2.team.claims.paper.listener.ChunkLoadListener;
import com.kalimero2.team.claims.paper.listener.ChunkProtectionListener;
import com.kalimero2.team.claims.paper.listener.PlayerMoveListener;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import com.kalimero2.team.claims.paper.util.SerializableChunk;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PaperClaims extends JavaPlugin implements ClaimsApi {

    public static PaperClaims plugin;
    private MessageUtil messageUtil;
    public File playerDataFolder;
    public ChunkBorders chunkBorders;

    @Override
    public void onLoad() {
        if(plugin == null){
            plugin = this;
        }

        plugin.saveDefaultConfig();

        this.messageUtil = new MessageUtil( new File(this.getDataFolder()+"/"+ plugin.getConfig().getString("messages")));
        this.playerDataFolder = new File(this.getDataFolder()+ "/playerdata/");

        ConfigurationSerialization.registerClass(SerializableChunk.class);
    }

    @Override
    public void onEnable() {
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

    @Override
    public void onDisable() {

    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}
