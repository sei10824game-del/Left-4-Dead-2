package com.l4d.infected;

import com.l4d.infected.entity.Infected;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/** Dev only (env L4D_SELFTEST=1): spawns every infected next to a villager for 600 ticks and logs what happened. */
public class SelfTest {
    private final List<Infected> mobs = new ArrayList<>();
    private final Map<Infected, Villager> prey = new HashMap<>();
    private final Map<Infected, Set<Integer>> actions = new HashMap<>();
    private int tick;

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        ServerLevel lvl = e.getServer().overworld();
        tick++;
        if (tick == 40) {
            int i = 0;
            for (L4D.Entry en : L4D.INFECTED) {
                double x = -60 + (i % 9) * 14, z = 20 + (i / 9) * 24; i++;
                Infected m = (Infected) en.type().get().spawn(lvl, new net.minecraft.core.BlockPos((int) x, lvl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int) x, (int) z), (int) z), MobSpawnType.COMMAND);
                Villager v = EntityType.VILLAGER.spawn(lvl, new net.minecraft.core.BlockPos((int) x + 7, m.getBlockY(), (int) z + 3), MobSpawnType.COMMAND);
                if (m == null || v == null) { System.out.println("L4DTEST spawn FAILED " + en.name()); continue; }
                v.setNoAi(true);
                m.setTarget(v);
                mobs.add(m); prey.put(m, v); actions.put(m, new TreeSet<>());
            }
        }
        for (Infected m : mobs) if (m.isAlive()) actions.get(m).add(m.action());
        // force the Meat Wall's scripted attacks and its death explosion so their code paths run at least once
        for (Infected m : mobs) if (m instanceof com.l4d.infected.entity.MeatWallInfected w && w.isAlive()) {
            if (tick == 300) w.startAction(com.l4d.infected.entity.MeatWallInfected.CRACK5, 48);
            if (tick == 400) w.startAction(com.l4d.infected.entity.MeatWallInfected.CRACK3, 40);
            if (tick == 500) w.startAction(com.l4d.infected.entity.MeatWallInfected.GRAB, 76);
            if (tick == 650) w.kill();
        }
        if (tick == 700) {
            for (Infected m : mobs) {
                Villager v = prey.get(m);
                System.out.println("L4DTEST " + m.getType().getDescriptionId() + " alive=" + m.isAlive() + " preyHp=" + (v.isAlive() ? v.getHealth() : -1) + " actions=" + actions.get(m));
            }
            System.out.println("L4DTEST DONE");
            e.getServer().halt(false);
        }
    }
}
