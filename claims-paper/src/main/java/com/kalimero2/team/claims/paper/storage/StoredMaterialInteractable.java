package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import org.bukkit.Material;

public class StoredMaterialInteractable extends MaterialInteractable {

    protected final boolean originalInteract;

    public StoredMaterialInteractable(Material blockMaterial, boolean interact) {
        super(blockMaterial, interact);
        this.originalInteract = interact;
    }

    public static StoredMaterialInteractable cast(MaterialInteractable materialInteractable){
        return (StoredMaterialInteractable) materialInteractable;
    }

    public void setInteractable(boolean interact) {
        this.interact = interact;
    }


}
