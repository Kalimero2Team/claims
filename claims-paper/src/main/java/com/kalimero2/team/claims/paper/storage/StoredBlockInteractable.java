package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import org.bukkit.Material;

public class StoredBlockInteractable implements BlockInteractable {

    private final Material blockMaterial;
    private final boolean state;

    public StoredBlockInteractable(Material blockMaterial, boolean state) {
        this.blockMaterial = blockMaterial;
        this.state = state;
    }

    @Override
    public Material getBlockMaterial() {
        return blockMaterial;
    }

    @Override
    public boolean getState() {
        return state;
    }
}
