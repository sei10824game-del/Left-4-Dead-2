package com.l4d.infected.client;

import com.l4d.infected.L4D;
import com.l4d.infected.entity.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = L4D.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    record Reg<T extends Infected>(String name, RegistryObject<EntityType<T>> type, Function<ModelPart, InfectedModel<T>> model,
                                   float shadow, float scale, int variants) {
        ModelLayerLocation layer() { return new ModelLayerLocation(new ResourceLocation(L4D.ID, name), "main"); }
    }

    static final List<Reg<?>> ALL = new ArrayList<>();
    static {
        ALL.add(new Reg<>("common", L4D.COMMON, CommonModel::new, 0.5f, 1f, CommonInfected.VARIANTS));
        ALL.add(new Reg<>("ceda", L4D.CEDA, CedaModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("mudman", L4D.MUDMAN, MudmanModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("roadcrew", L4D.ROADCREW, RoadcrewModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("fallen", L4D.FALLEN, FallenModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("riot", L4D.RIOT, RiotModel::new, 0.55f, 1f, 0));
        ALL.add(new Reg<>("clown", L4D.CLOWN, ClownModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("jimmy", L4D.JIMMY, JimmyModel::new, 0.6f, 1.05f, 0));
        ALL.add(new Reg<>("boomer", L4D.BOOMER, BoomerModel::new, 0.9f, 1f, 0));
        ALL.add(new Reg<>("boomette", L4D.BOOMETTE, BoometteModel::new, 0.8f, 1f, 0));
        ALL.add(new Reg<>("hunter", L4D.HUNTER, HunterModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("smoker", L4D.SMOKER, SmokerModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("spitter", L4D.SPITTER, SpitterModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("jockey", L4D.JOCKEY, JockeyModel::new, 0.4f, 1f, 0));
        ALL.add(new Reg<>("charger", L4D.CHARGER, ChargerModel::new, 0.9f, 1f, 0));
        ALL.add(new Reg<>("tank", L4D.TANK, TankModel::new, 1.4f, 1f, 0));
        ALL.add(new Reg<>("meatwall", L4D.MEATWALL, MeatWallModel::new, 1.8f, 1f, 0));
        ALL.add(new Reg<>("screamer", L4D.SCREAMER, ScreamerModel::new, 0.5f, 1f, 0));
        ALL.add(new Reg<>("crawler", L4D.CRAWLER, CrawlerModel::new, 0.4f, 1f, 0));
        ALL.add(new Reg<>("witch", L4D.WITCH, WitchModel::new, 0.5f, 1f, 2));
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        for (Reg<?> r : ALL) e.registerLayerDefinition(r.layer(), () -> JsonModel.load(r.name()));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        for (Reg<?> r : ALL) reg(e, r);
        e.registerEntityRenderer(L4D.ACID_SPIT.get(), AcidRenderer.Spit::new);
        e.registerEntityRenderer(L4D.TANK_ROCK.get(), TankRockRenderer::new);
        e.registerEntityRenderer(L4D.ACID_POOL.get(), AcidRenderer.Pool::new);
    }

    private static <T extends Infected> void reg(EntityRenderersEvent.RegisterRenderers e, Reg<T> r) {
        e.registerEntityRenderer(r.type().get(), (EntityRendererProvider.Context c) ->
                new InfectedRenderer<>(c, r.model().apply(c.bakeLayer(r.layer())), r.name(), r.shadow(), r.scale(), r.variants()));
    }
}
