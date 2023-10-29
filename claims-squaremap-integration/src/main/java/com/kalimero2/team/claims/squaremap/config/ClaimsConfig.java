package com.kalimero2.team.claims.squaremap.config;

import java.awt.Color;
import org.bukkit.plugin.Plugin;

public final class ClaimsConfig extends Config<ClaimsConfig, WorldConfig> {
    public String controlLabel = "Claims";
    public boolean controlShow = true;
    public boolean controlHide = false;
    public int updateInterval = 300;
    public Color strokeColor = Color.RED;
    public int strokeWeight = 2;
    public double strokeOpacity = 0.75D;
    public Color fillColor = Color.GREEN;
    public Color fillColorGroup = Color.ORANGE;
    public Color teamColor = Color.BLUE;
    public double fillOpacity = 0.5D;
    public String claimTooltip = "Grundstücksbesitzer: {owner}";
    public boolean showChunks = true;

    @SuppressWarnings("unused")
    private void init() {
        this.controlLabel = this.getString("settings.control.label", this.controlLabel);
        this.controlShow = this.getBoolean("settings.control.show", this.controlShow);
        this.controlHide = this.getBoolean("settings.control.hide-by-default", this.controlHide);
        this.updateInterval = this.getInt("settings.update-interval", this.updateInterval);
        this.strokeColor = this.getColor("settings.style.stroke.color", this.strokeColor);
        this.strokeWeight = this.getInt("settings.style.stroke.weight", this.strokeWeight);
        this.strokeOpacity = this.getDouble("settings.style.stroke.opacity", this.strokeOpacity);
        this.fillColor = this.getColor("settings.style.fill.color", this.fillColor);
        this.fillColorGroup = this.getColor("settings.style.fill.color-group", this.fillColorGroup);
        this.teamColor = this.getColor("settings.style.fill.color-team", this.teamColor);
        this.fillOpacity = this.getDouble("settings.style.fill.opacity", this.fillOpacity);
        this.claimTooltip = this.getString("settings.claim.tooltip", this.claimTooltip);
        this.showChunks = this.getBoolean("settings.claim.show-chunks", this.showChunks);
    }

    public ClaimsConfig(final Plugin plugin) {
        super(ClaimsConfig.class, plugin);
    }
}
