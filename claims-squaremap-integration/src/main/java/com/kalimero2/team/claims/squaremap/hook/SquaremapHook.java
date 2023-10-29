package com.kalimero2.team.claims.squaremap.hook;

import java.util.HashMap;
import java.util.Map;

import com.kalimero2.team.claims.squaremap.SquaremapClaims;
import com.kalimero2.team.claims.squaremap.task.SquaremapTask;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;

import static xyz.jpenilla.squaremap.api.Key.key;

public final class SquaremapHook {
    private static final Key CLAIM_CHUNK_LAYER_KEY = key("claims");

    private final Map<WorldIdentifier, SquaremapTask> tasks = new HashMap<>();

    public SquaremapHook(final SquaremapClaims plugin) {
        for (final MapWorld world : SquaremapProvider.get().mapWorlds()) {
            final SimpleLayerProvider provider = SimpleLayerProvider.builder(plugin.config().controlLabel)
                .showControls(plugin.config().controlShow)
                .defaultHidden(plugin.config().controlHide)
                .build();

            world.layerRegistry().register(CLAIM_CHUNK_LAYER_KEY, provider);

            final SquaremapTask task = new SquaremapTask(plugin, world, provider);
            task.runTaskTimerAsynchronously(plugin, 20L, 20L * plugin.config().updateInterval);

            this.tasks.put(world.identifier(), task);
        }
    }

    public void disable() {
        this.tasks.values().forEach(SquaremapTask::disable);
        this.tasks.clear();
    }
}
