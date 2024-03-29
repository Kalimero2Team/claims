package com.kalimero2.team.claims.paper;

import com.kalimero2.team.claims.api.ClaimsApiHolder;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.command.CommandManager;
import com.kalimero2.team.claims.api.flag.ClaimsFlags;
import com.kalimero2.team.claims.paper.listener.ChunkProtectionListener;
import com.kalimero2.team.claims.paper.listener.ExplosionListener;
import com.kalimero2.team.claims.paper.listener.ItemListener;
import com.kalimero2.team.claims.paper.listener.MonsterSpawnListener;
import com.kalimero2.team.claims.paper.listener.PhysicsListener;
import com.kalimero2.team.claims.paper.listener.PlayerMoveListener;
import com.kalimero2.team.claims.paper.storage.Storage;
import com.kalimero2.team.claims.paper.util.ChunkBorders;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;

public class PaperClaims extends JavaPlugin {
    public static ClaimManager api;
    public ChunkBorders chunkBorders;
    private MessageUtil messageUtil;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        Locale locale = Locale.getDefault();
        String lang = getConfig().getString("language");
        if (lang != null) {
            locale = Locale.forLanguageTag(lang);
        }
        this.messageUtil = new MessageUtil(locale);
    }

    @Override
    public void onEnable() {
        api = new ClaimManager(this);
        ClaimsApiHolder.setApi(api);

        boolean ignored = ClaimsFlags.PVP.getDefaultState(); // This is just to make sure the class is loaded

        getServer().getPluginManager().registerEvents(new ChunkProtectionListener(api), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(api, this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(api), this); // Flag: EXPLOSIONS
        getServer().getPluginManager().registerEvents(new PhysicsListener(api), this); // Flag: NO_PHYSICS
        getServer().getPluginManager().registerEvents(new ItemListener(api), this); // Flag: ITEM_DROP, ITEM_PICKUP
        getServer().getPluginManager().registerEvents(new MonsterSpawnListener(api), this); // Flag: MONSTER_SPAWNING

        chunkBorders = new ChunkBorders(this);

        try {
            new CommandManager(this);
            getLogger().info("Commands registered");
        } catch (Exception e) {
            getLogger().warning("Failed to initialize command manager: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        api.shutdown();
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}
