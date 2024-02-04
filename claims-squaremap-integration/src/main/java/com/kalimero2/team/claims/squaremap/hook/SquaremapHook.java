package com.kalimero2.team.claims.squaremap.hook;

import com.kalimero2.team.claims.squaremap.SquaremapClaims;
import com.kalimero2.team.claims.squaremap.task.SquaremapTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;

import java.util.HashMap;
import java.util.Map;

import static xyz.jpenilla.squaremap.api.Key.key;

public final class SquaremapHook implements Listener {
    private static final Key CLAIM_CHUNK_LAYER_KEY = key("claims");

    private final Map<WorldIdentifier, SquaremapTask> tasks = new HashMap<>();

    public SquaremapHook(final SquaremapClaims plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        final MapWorld world = SquaremapProvider.get().mapWorlds().stream()
                .filter(mapWorld -> BukkitAdapter.bukkitWorld(mapWorld).getUID().equals(event.getWorld().getUID()))
                .findFirst()
                .orElse(null);
        if (world != null) {
            final SimpleLayerProvider provider = SimpleLayerProvider.builder(SquaremapClaims.getPlugin(SquaremapClaims.class).config().controlLabel)
                    .showControls(SquaremapClaims.getPlugin(SquaremapClaims.class).config().controlShow)
                    .defaultHidden(SquaremapClaims.getPlugin(SquaremapClaims.class).config().controlHide)
                    .build();

            world.layerRegistry().register(CLAIM_CHUNK_LAYER_KEY, provider);

            final SquaremapTask task = new SquaremapTask(SquaremapClaims.getPlugin(SquaremapClaims.class), world, provider);
            task.runTaskTimerAsynchronously(SquaremapClaims.getPlugin(SquaremapClaims.class), 20L, 20L * SquaremapClaims.getPlugin(SquaremapClaims.class).config().updateInterval);

            this.tasks.put(world.identifier(), task);
        }
    }

    public void disable() {
        this.tasks.values().forEach(SquaremapTask::disable);
        this.tasks.clear();
    }
}
