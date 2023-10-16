package com.kalimero2.team.claims.api.interactable;

import org.bukkit.Material;

public abstract class MaterialInteractable {

    private final Material blockMaterial;
    protected boolean interact;

    public MaterialInteractable(Material blockMaterial, boolean interact) {
        this.blockMaterial = blockMaterial;
        this.interact = interact;
    }

    public Material getBlockMaterial(){
        return blockMaterial;
    }

    public boolean canInteract(){
        return interact;
    }
}
