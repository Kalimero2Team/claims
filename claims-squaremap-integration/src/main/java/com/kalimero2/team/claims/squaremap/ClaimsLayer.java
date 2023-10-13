package com.kalimero2.team.claims.squaremap;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.squaremap.api.LayerProvider;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.marker.Marker;

import java.util.Collection;

public class ClaimsLayer implements LayerProvider {
    public ClaimsLayer(MapWorld mapWorld) {

    }

    @Override
    public String getLabel() {
        return "Claims";
    }

    @Override
    public int layerPriority() {
        return 0;
    }

    @Override
    public @NonNull Collection<Marker> getMarkers() {
        return null;
    }
}
