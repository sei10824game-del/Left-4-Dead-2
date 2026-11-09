package com.l4d.infected.client;

import com.l4d.infected.entity.TankInfected;
import net.minecraft.client.model.geom.ModelPart;

/** model spec: assets/l4dinfected/models/entity/tank.json */
public class TankModel extends InfectedModel<TankInfected> {
    public TankModel(ModelPart root) { super(root, "tank"); }
}
