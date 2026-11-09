"""Helpers for the hand-built special infected: model spec writer with auto-packed box UVs + organic paint helpers.
build(): parts = [(name, parent|None, pivot, rot_deg, [(from, size, inflate), ...]), ...]  (cube sizes must be ints)"""
import json, math, os
from texlib import MODELS, mix

def build(name, parts, tex=(128, 128)):
    cubes = [c for p in parts for c in p[4]]
    order = sorted(range(len(cubes)), key=lambda i: -(cubes[i][1][2] + cubes[i][1][1]))
    uv, x, y, shelf = {}, 0, 0, 0
    for i in order:
        w, h, d = cubes[i][1]
        bw, bh = 2 * (w + d), d + h
        if x + bw > tex[0]: x, y, shelf = 0, y + shelf, 0
        uv[i] = [x, y]; x += bw; shelf = max(shelf, bh)
    assert y + shelf <= tex[1], f'{name}: UVs overflow ({y + shelf} > {tex[1]})'
    names, out, k = set(), [], 0
    for n, par, piv, rot, cs in parts:
        assert n not in names and (par is None or par in names), n
        names.add(n)
        jc = []
        for frm, size, inf in cs:
            c = {'uv': uv[k], 'from': frm, 'size': size}
            if inf: c['inflate'] = inf
            jc.append(c); k += 1
        p = {'name': n, 'pivot': piv, 'rot': rot, 'cubes': jc}
        if par: p['parent'] = par
        out.append(p)
    json.dump({'tex': list(tex), 'parts': out}, open(os.path.join(MODELS, name + '.json'), 'w'), indent=1)

def vnoise(x, y, s=4.0, seed=0):
    """smooth value noise 0..1."""
    xi, yi = math.floor(x / s), math.floor(y / s); fx, fy = x / s - xi, y / s - yi
    h = lambda a, b: (((a * 73856093) ^ (b * 19349663) ^ (seed * 83492791)) & 0xffff) / 65535
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    top = h(xi, yi) + (h(xi + 1, yi) - h(xi, yi)) * fx
    bot = h(xi, yi + 1) + (h(xi + 1, yi + 1) - h(xi, yi + 1)) * fx
    return top + (bot - top) * fy

def mottle(t, part, color, amt=0.5, s=3.0, seed=0, cubes=None, faces=None):
    """blend painted pixels toward color in organic blotches."""
    def f(face, x, y, w, h):
        for j in range(h):
            for i in range(w):
                old = t.px[x + i, y + j]
                if old[3] == 0: continue
                n = max(0, vnoise(x + i, y + j, s, seed) * 1.6 - 0.6)
                t.put(x + i, y + j, mix(old, color, amt * n))
    t.overlay(part, f, faces, cubes)

def veins(t, part, color, n=3, length=(5, 12), alpha=0.6, cubes=None, faces=None):
    """wandering vein lines on each face."""
    def f(face, x, y, w, h):
        if w < 3 or h < 3: return
        for _ in range(max(1, int(n * w * h / 64))):
            px, py, dx = x + t.rng.randrange(w), y + t.rng.randrange(h), t.rng.choice((-1, 0, 1))
            for _ in range(t.rng.randint(*length)):
                if not (x <= px < x + w and y <= py < y + h): break
                t.put(px, py, mix(t.px[px, py], color, alpha))
                py += 1 if t.rng.random() < .6 else 0
                px += dx if t.rng.random() < .5 else t.rng.choice((-1, 1)) * (t.rng.random() < .2)
    t.overlay(part, f, faces, cubes)

def boil(t, x, y, r, core=(232, 214, 96), rim=(120, 40, 30), glow=None):
    """round pustule: dark inflamed rim, bright wet core, white specular. glow: optional emissive Tex."""
    for j in range(-r, r + 1):
        for i in range(-r, r + 1):
            d = math.hypot(i, j) / max(r, .5)
            if d > 1.05 or not (0 <= x + i < t.img.width and 0 <= y + j < t.img.height): continue
            old = t.px[x + i, y + j]
            if old[3] == 0: continue
            t.put(x + i, y + j, mix(core, rim, min(1, d * d * 1.2)) if d < .8 else mix(old, rim, .7))
            if glow and d < .6: glow.put(x + i, y + j, core + (190,))
    if r > 1: t.put(x - 1, y - 1, (250, 248, 220))

def boils(t, part, n, r=(1, 2), cubes=None, faces=None, **k):
    """scatter boils over every face (density n per 64 px)."""
    def f(face, x, y, w, h):
        for _ in range(max(0, round(n * w * h / 64 + t.rng.random() - .5))):
            rr = t.rng.randint(*r)
            if w > 2 * rr and h > 2 * rr:
                boil(t, x + rr + t.rng.randrange(w - 2 * rr), y + rr + t.rng.randrange(h - 2 * rr), rr, **k)
    t.overlay(part, f, faces, cubes)

def grime(t, part, color, amt, s=2.0, seed=0, cubes=None, faces=None):
    """darken towards the bottom of side faces + blotchy stains."""
    def f(face, x, y, w, h):
        if face == 'top': return
        for j in range(h):
            for i in range(w):
                old = t.px[x + i, y + j]
                if old[3] == 0: continue
                a = (j / max(1, h - 1)) ** 2 * amt * (face != 'bottom') + max(0, vnoise(x + i, y + j, s, seed) - .7) * amt
                t.put(x + i, y + j, mix(old, color, min(1, a)))
    t.overlay(part, f, faces, cubes)
