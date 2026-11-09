package com.l4d.infected.client;

import com.l4d.infected.entity.FallenInfected;
import net.minecraft.client.model.geom.ModelPart;

/** model spec: assets/l4dinfected/models/entity/fallen.json */
public class FallenModel extends InfectedModel<FallenInfected> {
    public FallenModel(ModelPart root) { super(root, "fallen"); }
}
