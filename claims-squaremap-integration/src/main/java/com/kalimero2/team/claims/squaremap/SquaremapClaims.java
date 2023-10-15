package com.kalimero2.team.claims.squaremap;

import com.kalimero2.team.claims.squaremap.config.ClaimsConfig;
import com.kalimero2.team.claims.squaremap.hook.ClaimsHook;
import com.kalimero2.team.claims.squaremap.hook.SquaremapHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class SquaremapClaims extends JavaPlugin {
    private SquaremapHook squaremapHook;
    private ClaimsConfig config;

    @Override
    public void onEnable() {
        this.config = new ClaimsConfig(this);
        this.config.reload();

        ClaimsHook.registerFlag();

        this.squaremapHook = new SquaremapHook(this);
    }

    @Override
    public void onDisable() {
        if (this.squaremapHook != null) {
            this.squaremapHook.disable();
        }
    }

    public ClaimsConfig config() {
        return this.config;
    }
}
