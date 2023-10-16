package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import org.bukkit.Material;

public class StoredBlockInteractable extends MaterialInteractable {

    public StoredBlockInteractable(Material blockMaterial, boolean state) {
        super(blockMaterial, state);
    }

    public void setState(boolean state) {
        this.interact = state;
    }
}
