"""Spitter: emaciated, hunched, bald; a long forward-thrust neck carrying a glowing acid sac, split tusked jaw,
spine nubs, long clawed arms, acid-burnt skin with glowing veins. Writes spitter.json + spitter.png / spitter_eyes.png."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *
from speclib import *

LEG = [([-1.5, 0, -1.5], [3, 7, 3], 0), ([-1, 6, -1], [2, 5, 2], 0), ([-1.5, 10, -3.5], [3, 2, 5], 0)]
FORE = [([-1, 0, -1], [2, 7, 2], 0), ([-1.5, 6, -1.5], [3, 2, 3], 0), ([-1.5, 8, -1], [3, 3, 1], 0)]
PARTS = [
    ('right_leg', None, [-2, 12, 0], [0, 0, 0], LEG),
    ('left_leg', None, [2, 12, 0], [0, 0, 0], LEG),
    ('body', None, [0, 12, 0], [16, 0, 0], [
        ([-3.5, -3, -2], [7, 4, 4], 0),             # hips
        ([-2.5, -8, -1.5], [5, 5, 3], 0),           # starved waist
        ([-4, -15, -2.5], [8, 7, 5], 0),            # ribcage
        ([-3.5, -14.5, 2], [3, 3, 1], 0), ([.5, -14.5, 2], [3, 3, 1], 0),       # shoulder blades
        ([-.5, -12, 2.2], [1, 1, 1], 0), ([-.5, -9, 1.5], [1, 1, 1], 0), ([-.5, -6, 1.2], [1, 1, 1], 0)]),  # spine
    ('rag', 'body', [0, 0, -2], [0, 0, 0], [([-2.5, 0, -.8], [5, 5, 1], 0)]),
    ('neck', 'body', [0, -14.5, -1], [34, 0, 0], [([-1.5, -7, -1.5], [3, 7, 3], 0), ([-.5, -6, 1.3], [1, 1, 1], 0), ([-.5, -3, 1.3], [1, 1, 1], 0)]),
    ('sac', 'neck', [0, -3, -1.5], [0, 0, 0], [([-2, -2, -3], [4, 4, 3], .3), ([-1, 1.5, -2.5], [2, 2, 2], 0)]),
    ('head', 'neck', [0, -7, 0], [-48, 0, 0], [([-2.5, -5, -5], [5, 5, 7], 0), ([-1.5, -6, -3], [3, 1, 6], 0)]),
    ('jaw', 'head', [0, -1, -1], [0, 0, 0], [([-2, 0, -4.5], [4, 2, 5], 0), ([-2.6, -1, -4.5], [1, 2, 1], 0), ([1.6, -1, -4.5], [1, 2, 1], 0)]),
    ('right_arm', 'body', [-5, -14, 0], [0, 0, 8], [([-1.5, -1, -1.5], [3, 7, 3], 0)]),
    ('right_fore', 'right_arm', [0, 6, 0], [-15, 0, 0], FORE),
    ('left_arm', 'body', [5, -14, 0], [0, 0, -8], [([-1.5, -1, -1.5], [3, 7, 3], 0)]),
    ('left_fore', 'left_arm', [0, 6, 0], [-15, 0, 0], FORE),
]
build('spitter', PARTS)

SKIN, ASH, BURN, ACID = (138, 146, 124), (92, 96, 94), (176, 190, 70), (170, 255, 60)
m = Model('spitter'); t = Tex(m, 21); g = Tex(m, 21)
flesh = ['body', 'neck', 'head', 'jaw', 'right_arm', 'right_fore', 'left_arm', 'left_fore', 'right_leg', 'left_leg']
for i, p in enumerate(flesh):
    t.fill(p, SKIN, 5, .4)
    mottle(t, p, ASH, .6, 4, i + 80); mottle(t, p, (120, 90, 80), .35, 3, i + 90)
    veins(t, p, (70, 80, 70), 1.2, alpha=.5)
# chemical burns: raw yellow-green patches with a faint glow at their wet centres
def burns(p, n):
    def f(face, x, y, w, h):
        for _ in range(max(0, round(n * w * h / 64 + t.rng.random() - .5))):
            cx, cy, r = x + t.rng.randrange(w), y + t.rng.randrange(h), t.rng.uniform(1, 2.2)
            for j in range(h):
                for i in range(w):
                    d = ((x + i - cx) ** 2 + (y + j - cy) ** 2) ** .5 / r
                    if d < 1: t.put(x + i, y + j, mix(t.px[x + i, y + j], mix(BURN, (110, 60, 40), d), .85))
                    if d < .35: g.put(x + i, y + j, ACID + (120,))
    t.overlay(p, f)
for p in flesh: burns(p, .35)
# ribs: dark grooves across the front and sides of the ribcage
t.overlay('body', lambda f, x, y, w, h: f in ('front', 'left', 'right') and [t.put(x + i, y + j, shade(t.px[x + i, y + j], .62)) for j in range(1, h - 1, 2) for i in range(w) if not (f == 'front' and w // 2 - 1 <= i <= w // 2)], cubes=[2])
# glowing acid veins running from the sac down the chest + neck
def acid_veins(p, n, cubes=None):
    def f(face, x, y, w, h):
        if face in ('top', 'bottom', 'back'): return
        for _ in range(n):
            px, py = x + t.rng.randrange(w), y
            while py < y + h:
                t.put(px, py, mix(t.px[px, py], ACID, .7)); g.put(px, py, ACID + (150,))
                py += 1; px = min(x + w - 1, max(x, px + t.rng.choice((-1, 0, 0, 1))))
    t.overlay(p, f, cubes=cubes)
acid_veins('neck', 1, [0]); acid_veins('body', 2, [2])
# the sac: translucent, glowing, darker veins over it
t.fill('sac', (190, 230, 70), 8, .2)
t.overlay('sac', lambda f, x, y, w, h: [g.put(x + i, y + j, mix(ACID, (255, 255, 160), .3 * ((i + j) % 2)) + (235,)) for j in range(h) for i in range(w)])
veins(t, 'sac', (70, 110, 30), 3, (3, 6), .8)
# ragged hip wrap
for p, cs in (('rag', None), ('body', [0])):
    t.fill(p, (70, 74, 86), 10, .2, cubes=cs); grime(t, p, (40, 36, 30), .6, 2, 5, cubes=cs)
t.overlay('rag', lambda f, x, y, w, h: f in ('front', 'back') and [t.put(x + i, y + h - 1, (0, 0, 0, 0)) for i in range(w) if i % 2])
# claws + feet: blackened
for p in ('right_fore', 'left_fore'):
    t.fill(p, (40, 36, 32), 12, cubes=[2]); grime(t, p, (60, 50, 40), .6, 2, 3, cubes=[1])
for p in ('right_leg', 'left_leg'): grime(t, p, (50, 44, 38), .8, 2, 6, cubes=[1, 2])
# head: scabbed scalp, deep sockets with green pinpricks, lipless mouth, glowing gullet
t.blot('head', (90, 50, 40), 3, (2, 4), .6)
hx, hy, hw, hh = t.face('head', 0, 'front')
for ex in (hx, hx + 3):
    t.rect(ex, hy + 1, 2, 2, (18, 16, 14)); t.put(ex + (ex > hx), hy + 2, ACID); g.put(ex + (ex > hx), hy + 2, ACID + (255,))
t.rect(hx, hy + 4, hw, 1, (40, 20, 16)); [t.put(hx + i, hy + 4, (200, 196, 150)) for i in range(0, hw, 2)]
for c in (1, 2): t.overlay('jaw', lambda f, x, y, w, h: [t.put(x + i, y + j, (220, 210, 170) if j < h - 1 else (120, 110, 80)) for j in range(h) for i in range(w)], cubes=[c])
jx, jy, jw, jh = t.face('jaw', 0, 'top'); t.rect(jx, jy, jw, jh, (60, 30, 20))
for i in range(jw):
    for j in range(jh): g.put(jx + i, jy + j, (150, 255, 60, 140 if (i + j) % 2 else 200))
t.save('spitter'); g.save('spitter_eyes')
preview(m, 'spitter', scale=10, views=(0, 90, 160, 35))

# ---- acid decals: pool (flat, irregular edge, bubbles) and spit glob (billboard) ----
import random as _r
def acid_blob(size, seed, bubbles):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0)); px = img.load(); c = size / 2; rng = _r.Random(seed)
    for y in range(size):
        for x in range(size):
            a = math.atan2(y - c, x - c); d = math.hypot(x - c + .5, y - c + .5) / c
            edge = .78 + .16 * vnoise(math.cos(a) * 6 + 9, math.sin(a) * 6 + 9, 2.2, seed)
            if d > edge: continue
            k = d / edge; n = vnoise(x, y, size / 10, seed + 1)
            col = mix(mix((235, 255, 120), (120, 220, 40), k ** 1.5), (60, 120, 20), max(0, k - .75) * 3)
            col = mix(col, (200, 255, 90), .35 * max(0, n - .5) * 2)
            px[x, y] = col + (int(255 * min(1, (1 - k) * 4) * (.75 + .25 * n)),)
    for _ in range(bubbles):
        bx, by, br = rng.uniform(.3, .7) * size, rng.uniform(.3, .7) * size, rng.uniform(1, size / 18)
        for y in range(size):
            for x in range(size):
                if px[x, y][3] and abs(math.hypot(x - bx, y - by) - br) < .7: px[x, y] = (250, 255, 200, 255)
    return img
acid_blob(64, 3, 9).save(os.path.join(TEXDIR, 'acid_pool.png'))
acid_blob(16, 5, 1).save(os.path.join(TEXDIR, 'acid_spit.png'))
