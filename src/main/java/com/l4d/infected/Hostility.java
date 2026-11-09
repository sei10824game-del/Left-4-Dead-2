package com.l4d.infected;

import com.l4d.infected.entity.Infected;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** All non-infected mobs fight infected (and vice versa, see Infected target goals). */
public class Hostility {
    @SubscribeEvent
    public void join(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide || !(e.getEntity() instanceof Mob m) || m instanceof Infected) return;
        m.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(m, Infected.class, 0, true, false, t -> true));
        if (m instanceof PathfinderMob p && p.goalSelector.getAvailableGoals().stream().noneMatch(
                w -> w.getGoal() instanceof MeleeAttackGoal || w.getGoal() instanceof RangedAttackGoal))
            p.goalSelector.addGoal(2, new MeleeAttackGoal(p, 1.25, true));
    }
}
