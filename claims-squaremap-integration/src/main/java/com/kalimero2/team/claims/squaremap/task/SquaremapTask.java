package com.kalimero2.team.claims.squaremap.task;

import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.squaremap.SquaremapClaims;
import com.kalimero2.team.claims.squaremap.data.Group;
import com.kalimero2.team.claims.squaremap.data.Claim;
import com.kalimero2.team.claims.squaremap.hook.ClaimsHook;
import com.kalimero2.team.claims.squaremap.util.RectangleMerge;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Polygon;
import xyz.jpenilla.squaremap.api.marker.Rectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.jpenilla.squaremap.api.Key.key;

public final class SquaremapTask extends BukkitRunnable {
    private final World bukkitWorld;
    private final SimpleLayerProvider provider;
    private final SquaremapClaims plugin;

    private boolean stop;

    public SquaremapTask(
            final SquaremapClaims plugin,
            final MapWorld world,
            final SimpleLayerProvider provider
    ) {
        this.plugin = plugin;
        this.bukkitWorld = BukkitAdapter.bukkitWorld(world);
        this.provider = provider;
    }

    @Override
    public void run() {
        if (this.stop) {
            this.cancel();
        }
        this.updateClaims();
    }

    void updateClaims() {
        this.provider.clearMarkers(); // TODO track markers instead of clearing them
        final List<com.kalimero2.team.claims.api.Claim> dataClaims = ClaimsHook.getClaims(this.bukkitWorld);

        if (dataClaims.isEmpty()) {
            return;
        }

        // show simple markers (marker per chunk)
        if (this.plugin.config().showChunks) {
            dataClaims.forEach(this::drawChunk);
            return;
        }

        // show combined chunks into polygons
        final List<Claim> claims = dataClaims.stream()
                .map(claim -> new Claim(
                        claim.getChunk().getX(),
                        claim.getChunk().getZ(),
                        claim.getOwner().getName(),
                        claim.getOwner().isPlayer(),
                        ClaimsApi.getApi().getFlagState(claim, ClaimsHook.TEAM_FLAG)
                ))
                .toList();

        final List<Group> groups = this.groupClaims(claims);
        for (Group group : groups) {
            this.drawGroup(group);
        }
    }

    private List<Group> groupClaims(List<Claim> claims) {
        // break groups down by owner
        Map<String, List<Claim>> byOwner = new HashMap<>();
        for (Claim claim : claims) {
            List<Claim> list = byOwner.getOrDefault(claim.owner(), new ArrayList<>());
            list.add(claim);
            byOwner.put(claim.owner(), list);
        }

        // combine touching claims
        Map<String, List<Group>> groups = new HashMap<>();
        for (Map.Entry<String, List<Claim>> entry : byOwner.entrySet()) {
            String owner = entry.getKey();
            List<Claim> list = entry.getValue();
            next1:
            for (Claim claim : list) {
                List<Group> groupList = groups.getOrDefault(owner, new ArrayList<>());
                for (Group group : groupList) {
                    if (group.isTouching(claim)) {
                        group.add(claim);
                        continue next1;
                    }
                }
                groupList.add(new Group(claim, owner));
                groups.put(owner, groupList);
            }
        }

        // combined touching groups
        List<Group> combined = new ArrayList<>();
        for (List<Group> list : groups.values()) {
            next:
            for (Group group : list) {
                for (Group toChk : combined) {
                    if (toChk.isTouching(group)) {
                        toChk.add(group);
                        continue next;
                    }
                }
                combined.add(group);
            }
        }

        return combined;
    }

    private void drawGroup(Group group) {
        final Polygon polygon = RectangleMerge.getPoly(group.claims());
        final MarkerOptions.Builder options = options(group.owner(), group.isPlayer(), group.hasTeamFlag());
        polygon.markerOptions(options);

        final Key markerKey = key("claims_chunk_" + group.id());
        this.provider.addMarker(markerKey, polygon);
    }

    private void drawChunk(com.kalimero2.team.claims.api.Claim claim) {
        int minX = claim.getChunk().getX() << 4;
        int maxX = (claim.getChunk().getX() + 1) << 4;
        int minZ = claim.getChunk().getZ() << 4;
        int maxZ = (claim.getChunk().getZ() + 1) << 4;

        final Rectangle rect = Marker.rectangle(Point.of(minX, minZ), Point.of(maxX, maxZ));
        final MarkerOptions.Builder options = options(claim.getOwner().getName(), claim.getOwner().isPlayer(), ClaimsApi.getApi().getFlagState(claim, ClaimsHook.TEAM_FLAG));
        rect.markerOptions(options);

        final Key markerKey = key("claims_chunk_" + minX + "_" + minZ);
        this.provider.addMarker(markerKey, rect);
    }

    private MarkerOptions.Builder options(String owner, boolean isPlayer, boolean hasTeamFlag) {
        Color fillColor = this.plugin.config().fillColor;
        if(hasTeamFlag){
            fillColor = this.plugin.config().teamColor;
        } else if(!isPlayer){
            fillColor = this.plugin.config().fillColorGroup;
        }

        return MarkerOptions.builder()
                .strokeColor(fillColor)
                .strokeWeight(this.plugin.config().strokeWeight)
                .strokeOpacity(this.plugin.config().strokeOpacity)
                .fillColor(fillColor)
                .fillOpacity(this.plugin.config().fillOpacity)
                .clickTooltip(
                        this.plugin.config().claimTooltip
                                .replace("{world}", this.bukkitWorld.getName())
                                .replace("{owner}", owner + (isPlayer ? "" : " <i>(Gruppe)</i>"))
                );
    }

    public void disable() {
        this.cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}
