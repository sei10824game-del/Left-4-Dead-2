package com.l4d.infected.client;

import com.google.gson.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.io.InputStreamReader;
import java.util.*;

/**
 * Models live in assets/l4dinfected/models/entity/NAME.json so the Java model and the python texture painter
 * share one source of truth (cube UVs can never drift apart).
 * part: {name,parent?,pivot:[x,y,z],rot:[deg x,y,z],cubes:[{uv:[u,v],from:[x,y,z],size:[w,h,d],inflate?,mirror?}]}
 */
public final class JsonModel {
    public record Part(String name, String parent) {}
    public static final Map<String, List<Part>> ORDER = new HashMap<>();

    public static LayerDefinition load(String name) {
        JsonObject o;
        try (var in = JsonModel.class.getResourceAsStream("/assets/l4dinfected/models/entity/" + name + ".json")) {
            o = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        } catch (Exception e) { throw new RuntimeException("model " + name, e); }
        MeshDefinition md = new MeshDefinition();
        Map<String, PartDefinition> defs = new HashMap<>();
        List<Part> order = new ArrayList<>();
        for (JsonElement pe : o.getAsJsonArray("parts")) {
            JsonObject p = pe.getAsJsonObject();
            String n = p.get("name").getAsString();
            String par = p.has("parent") && !p.get("parent").isJsonNull() ? p.get("parent").getAsString() : null;
            float[] piv = f(p, "pivot"), rot = f(p, "rot");
            CubeListBuilder cb = CubeListBuilder.create();
            if (p.has("cubes")) for (JsonElement ce : p.getAsJsonArray("cubes")) {
                JsonObject c = ce.getAsJsonObject();
                float[] uv = f(c, "uv"), fr = f(c, "from"), sz = f(c, "size");
                float inf = c.has("inflate") ? c.get("inflate").getAsFloat() : 0;
                boolean mir = c.has("mirror") && c.get("mirror").getAsBoolean();
                cb.texOffs((int) uv[0], (int) uv[1]).mirror(mir).addBox(fr[0], fr[1], fr[2], sz[0], sz[1], sz[2], new CubeDeformation(inf));
            }
            PartPose pose = PartPose.offsetAndRotation(piv[0], piv[1], piv[2],
                    (float) Math.toRadians(rot[0]), (float) Math.toRadians(rot[1]), (float) Math.toRadians(rot[2]));
            PartDefinition pd = (par == null ? md.getRoot() : defs.get(par)).addOrReplaceChild(n, cb, pose);
            defs.put(n, pd);
            order.add(new Part(n, par));
        }
        ORDER.put(name, order);
        int[] tex = o.has("tex") ? new int[]{o.getAsJsonArray("tex").get(0).getAsInt(), o.getAsJsonArray("tex").get(1).getAsInt()} : new int[]{64, 64};
        return LayerDefinition.create(md, tex[0], tex[1]);
    }

    private static float[] f(JsonObject o, String k) {
        float[] r = new float[3];
        if (o.has(k)) { JsonArray a = o.getAsJsonArray(k); for (int i = 0; i < Math.min(3, a.size()); i++) r[i] = a.get(i).getAsFloat(); }
        return r;
    }
}
