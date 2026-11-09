package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/** The common infected. Variant picks one of several skins. */
public class CommonInfected extends Infected {
    public static final int VARIANTS = 6;
    public CommonInfected(EntityType<? extends CommonInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(30, 3, 0.31); }

    @Nullable @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor w, DifficultyInstance d, MobSpawnType r, @Nullable SpawnGroupData g, @Nullable net.minecraft.nbt.CompoundTag n) {
        setVariant(random.nextInt(VARIANTS));
        return super.finalizeSpawn(w, d, r, g, n);
    }
}
