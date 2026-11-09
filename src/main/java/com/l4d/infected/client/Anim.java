package com.l4d.infected.client;

/** Tiny easing / keyframe helpers shared by every model. */
public final class Anim {
    private Anim() {}
    public static float seg(float t, float a, float b) { return Math.max(0f, Math.min(1f, (t - a) / (b - a))); }
    public static float smooth(float x) { return x * x * (3 - 2 * x); }
    public static float in(float x) { return x * x * x; }
    public static float out(float x) { float i = 1 - x; return 1 - i * i * i; }
    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    /** 0 -> 1 -> 0 bump between a..b (peak at m). */
    public static float bump(float t, float a, float m, float b) { return t < m ? smooth(seg(t, a, m)) : 1 - smooth(seg(t, m, b)); }
    /** piecewise keyframes: times ascending, values same length, smoothstep between keys. */
    public static float keys(float t, float[] ts, float[] vs) {
        if (t <= ts[0]) return vs[0];
        for (int i = 1; i < ts.length; i++)
            if (t <= ts[i]) return lerp(vs[i - 1], vs[i], smooth(seg(t, ts[i - 1], ts[i])));
        return vs[vs.length - 1];
    }
    /** like keys but each segment eased-in (accelerating, for strikes). */
    public static float keysIn(float t, float[] ts, float[] vs) {
        if (t <= ts[0]) return vs[0];
        for (int i = 1; i < ts.length; i++)
            if (t <= ts[i]) return lerp(vs[i - 1], vs[i], in(seg(t, ts[i - 1], ts[i])));
        return vs[vs.length - 1];
    }
}
