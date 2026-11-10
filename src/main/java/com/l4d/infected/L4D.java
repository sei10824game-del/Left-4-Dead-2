package com.l4d.infected;

import com.l4d.infected.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Mod entry: registers every infected, projectile and spawn egg. */
@Mod(L4D.ID)
public class L4D {
    public static final String ID = "l4dinfected";
    static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    /** every infected entry (for attributes / spawn placement / eggs). */
    public static final List<Entry> INFECTED = new ArrayList<>();

    public record Entry(String name, RegistryObject<? extends EntityType<? extends Infected>> type,
                        Supplier<AttributeSupplier.Builder> attrs) {}

    @SuppressWarnings("unchecked")
    private static <T extends Infected> RegistryObject<EntityType<T>> inf(String n, EntityType.EntityFactory<T> f,
            float w, float h, int egg1, int egg2, Supplier<AttributeSupplier.Builder> attrs) {
        RegistryObject<EntityType<T>> r = ENTITIES.register(n, () -> EntityType.Builder.of(f, MobCategory.MONSTER)
                .sized(w, h).clientTrackingRange(10).build(ID + ":" + n));
        INFECTED.add(new Entry(n, r, attrs));
        ITEMS.register(n + "_spawn_egg", () -> new ForgeSpawnEggItem(r, egg1, egg2, new Item.Properties()));
        return r;
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> proj(String n, EntityType.EntityFactory<T> f,
            float w, float h, int range) {
        return ENTITIES.register(n, () -> EntityType.Builder.of(f, MobCategory.MISC).sized(w, h)
                .clientTrackingRange(range).updateInterval(2).build(ID + ":" + n));
    }

    // ---- common + uncommon ----
    public static final RegistryObject<EntityType<CommonInfected>> COMMON = inf("common", CommonInfected::new, 0.6f, 1.95f, 0x6b7a5a, 0x8a1c1c, CommonInfected::attrs);
    public static final RegistryObject<EntityType<CedaInfected>> CEDA = inf("ceda", CedaInfected::new, 0.6f, 1.95f, 0xd8d070, 0x3a3a3a, CedaInfected::attrs);
    public static final RegistryObject<EntityType<MudmanInfected>> MUDMAN = inf("mudman", MudmanInfected::new, 0.6f, 1.95f, 0x4a3a28, 0x2a2018, MudmanInfected::attrs);
    public static final RegistryObject<EntityType<RoadcrewInfected>> ROADCREW = inf("roadcrew", RoadcrewInfected::new, 0.6f, 1.95f, 0xe0701a, 0x303030, RoadcrewInfected::attrs);
    public static final RegistryObject<EntityType<FallenInfected>> FALLEN = inf("fallen", FallenInfected::new, 0.6f, 1.95f, 0x405030, 0xa0a0a0, FallenInfected::attrs);
    public static final RegistryObject<EntityType<RiotInfected>> RIOT = inf("riot", RiotInfected::new, 0.65f, 1.95f, 0x202838, 0xc0c0c0, RiotInfected::attrs);
    public static final RegistryObject<EntityType<ClownInfected>> CLOWN = inf("clown", ClownInfected::new, 0.6f, 1.95f, 0xd02040, 0x3070d0, ClownInfected::attrs);
    public static final RegistryObject<EntityType<JimmyInfected>> JIMMY = inf("jimmy", JimmyInfected::new, 0.7f, 2.0f, 0x1a3a8a, 0xe0c020, JimmyInfected::attrs);
    // ---- special ----
    public static final RegistryObject<EntityType<BoomerInfected>> BOOMER = inf("boomer", BoomerInfected::new, 0.9f, 1.9f, 0x7a8a50, 0xb0b060, BoomerInfected::attrs);
    public static final RegistryObject<EntityType<BoometteInfected>> BOOMETTE = inf("boomette", BoometteInfected::new, 0.85f, 1.85f, 0x806a60, 0xb0b060, BoometteInfected::attrs);
    public static final RegistryObject<EntityType<HunterInfected>> HUNTER = inf("hunter", HunterInfected::new, 0.6f, 1.8f, 0x303c58, 0x80304a, HunterInfected::attrs);
    public static final RegistryObject<EntityType<SmokerInfected>> SMOKER = inf("smoker", SmokerInfected::new, 0.6f, 2.0f, 0x504a58, 0x8a8a98, SmokerInfected::attrs);
    public static final RegistryObject<EntityType<SpitterInfected>> SPITTER = inf("spitter", SpitterInfected::new, 0.6f, 1.95f, 0x4a6a30, 0xa0c020, SpitterInfected::attrs);
    public static final RegistryObject<EntityType<JockeyInfected>> JOCKEY = inf("jockey", JockeyInfected::new, 0.55f, 1.0f, 0x6a4a30, 0xc09040, JockeyInfected::attrs);
    public static final RegistryObject<EntityType<ChargerInfected>> CHARGER = inf("charger", ChargerInfected::new, 0.95f, 2.1f, 0x5a4a58, 0xb04040, ChargerInfected::attrs);
    public static final RegistryObject<EntityType<TankInfected>> TANK = inf("tank", TankInfected::new, 1.2f, 2.9f, 0x5a5a40, 0x903020, TankInfected::attrs);
    public static final RegistryObject<EntityType<WitchInfected>> WITCH = inf("witch", WitchInfected::new, 0.6f, 1.6f, 0xe8e8e8, 0xb02020, WitchInfected::attrs);
    public static final RegistryObject<EntityType<MeatWallInfected>> MEATWALL = inf("meatwall", MeatWallInfected::new, 2.4f, 2.7f, 0x8a3a3a, 0x3a1010, MeatWallInfected::attrs);
    public static final RegistryObject<EntityType<ScreamerInfected>> SCREAMER = inf("screamer", ScreamerInfected::new, 0.6f, 1.95f, 0xcfc8b4, 0x46382a, ScreamerInfected::attrs);
    public static final RegistryObject<EntityType<CrawlerInfected>> CRAWLER = inf("crawler", CrawlerInfected::new, 0.7f, 0.8f, 0x6a604e, 0x702828, CrawlerInfected::attrs);
    // ---- projectiles / effects ----
    public static final RegistryObject<EntityType<AcidSpit>> ACID_SPIT = proj("acid_spit", AcidSpit::new, 0.4f, 0.4f, 8);
    public static final RegistryObject<EntityType<AcidPool>> ACID_POOL = proj("acid_pool", AcidPool::new, 3f, 0.3f, 8);
    public static final RegistryObject<EntityType<TankRock>> TANK_ROCK = proj("tank_rock", TankRock::new, 1.1f, 1.1f, 10);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("infected", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ID))
            .icon(() -> new ItemStack(ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(ID, "tank_spawn_egg"))))
            .displayItems((p, out) -> ITEMS.getEntries().forEach(i -> out.accept(i.get()))).build());

    public L4D() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(L4D::attributes);
        bus.addListener(L4D::modifyAttributes);
        bus.addListener(L4D::spawns);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new Hostility());
        if (System.getenv("L4D_SELFTEST") != null) net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SelfTest());
    }

    private static void attributes(EntityAttributeCreationEvent e) {
        for (Entry en : INFECTED) e.put(en.type().get(), en.attrs().get().build());
    }

    /** Every living type gets attack attributes so any mob can fight infected. */
    private static void modifyAttributes(EntityAttributeModificationEvent e) {
        for (EntityType<? extends LivingEntity> t : e.getTypes()) {
            if (!e.has(t, Attributes.ATTACK_DAMAGE)) e.add(t, Attributes.ATTACK_DAMAGE, 2.0);
            if (!e.has(t, Attributes.ATTACK_KNOCKBACK)) e.add(t, Attributes.ATTACK_KNOCKBACK, 0.3);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void spawns(SpawnPlacementRegisterEvent e) {
        for (Entry en : INFECTED)
            e.register((EntityType) en.type().get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, lvl, reason, pos, rnd) -> Monster.checkMonsterSpawnRules((EntityType<? extends Monster>) (EntityType<?>) type, lvl, reason, pos, rnd),
                    SpawnPlacementRegisterEvent.Operation.OR);
    }
}
