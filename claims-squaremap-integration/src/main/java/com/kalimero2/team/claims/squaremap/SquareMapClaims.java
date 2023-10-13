package com.kalimero2.team.claims.squaremap;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

public class SquareMapClaims extends JavaPlugin {

    @Override
    public void onEnable() {
        Squaremap squaremap = SquaremapProvider.get();

        squaremap.mapWorlds().forEach(mapWorld -> {
            mapWorld.layerRegistry().register(Key.of("claims"), new ClaimsLayer(mapWorld));
        });

    }
}
