"""Boomer: grotesquely bloated, hunched; gut + back tumour + throat pouch that inflate, tiny sunken head, stubby legs.
Writes models/entity/boomer.json (auto UVs) + textures/entity/boomer.png / boomer_eyes.png."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *
from speclib import *

LEG = [([-3, -1, -3], [6, 7, 6], .3), ([-2.5, 5, -2.5], [5, 4, 5], 0), ([-3, 8, -4.5], [6, 2, 6], 0)]
PARTS = [
    ('right_leg', None, [-3.6, 14, .5], [0, 0, 0], LEG),
    ('left_leg', None, [3.6, 14, .5], [0, 0, 0], LEG),
    ('body', None, [0, 14, 0], [10, 0, 0], [
        ([-7, -5, -4.5], [14, 6, 9], 0),            # pelvis
        ([-6.5, -15, -4], [13, 11, 9], 0),          # chest
        ([-6, -16, 0], [12, 8, 7], .3),             # fat hump
        ([-8.5, -15, -3.5], [4, 5, 7], 0), ([4.5, -15, -3.5], [4, 5, 7], 0)]),   # shoulder rolls
    ('belly', 'body', [0, -8, -4], [0, 0, 0], [
        ([-7.5, -4, -8], [15, 11, 9], 0),           # gut
        ([-6.5, 5, -7], [13, 5, 7], 0),             # sag
        ([-9.5, 0, -6], [3, 8, 6], 0), ([6.5, 0, -6], [3, 8, 6], 0)]),           # flanks
    ('pustules', 'belly', [0, 0, -8], [0, 0, 0], [
        ([2, -2, -1.5], [3, 3, 2], .2), ([-5, 3, -1], [2, 2, 1], .2), ([-1, 5, -1], [2, 2, 1], .1), ([4, 4, -1], [2, 2, 1], 0)]),
    ('back_sac', 'body', [1.5, -11, 6.5], [0, 0, 0], [([-3, -3, -1], [6, 6, 4], .2), ([-5.5, 1, -.5], [3, 3, 2], 0)]),
    ('head', 'body', [0, -14.5, -4.5], [-12, 0, 0], [([-3.5, -6.5, -4], [7, 7, 7], 0), ([-2, -7.5, -2.5], [4, 1, 4], 0)]),
    ('jaw', 'head', [0, -1.5, -1.5], [0, 0, 0], [([-3, 0, -3], [6, 2, 4], 0)]),
    ('drool', 'jaw', [0, 1.5, -2.5], [0, 0, 0], [([-.5, 0, -.5], [1, 4, 1], 0)]),
    ('throat', 'head', [0, 0, -.5], [0, 0, 0], [([-4.5, -.5, -3.5], [9, 4, 7], .2)]),
    ('right_arm', 'body', [-8.5, -12.5, -1], [0, 0, 14], [([-3, -2, -3], [5, 8, 6], .3)]),
    ('right_fore', 'right_arm', [-.5, 6, 0], [-10, 0, 0], [([-2, 0, -2], [4, 6, 4], 0), ([-2.5, 5, -2.5], [5, 3, 4], 0)]),
    ('left_arm', 'body', [8.5, -12.5, -1], [0, 0, -14], [([-2, -2, -3], [5, 8, 6], .3)]),
    ('left_fore', 'left_arm', [.5, 6, 0], [-10, 0, 0], [([-2, 0, -2], [4, 6, 4], 0), ([-2.5, 5, -2.5], [5, 3, 4], 0)]),
]
build('boomer', PARTS)

SKIN, MOLD, BRUISE, VEIN = (184, 172, 124), (120, 140, 74), (112, 78, 96), (78, 70, 92)
PANTS, BILE = (92, 74, 52), (196, 210, 70)
m = Model('boomer'); t = Tex(m, 7); g = Tex(m, 7)
flesh = ['body', 'belly', 'pustules', 'back_sac', 'head', 'jaw', 'throat', 'right_arm', 'right_fore', 'left_arm', 'left_fore']
for i, p in enumerate(flesh):
    t.fill(p, SKIN, 5, .35)
    mottle(t, p, MOLD, .5, 5, i); mottle(t, p, BRUISE, .4, 4, i + 20)
    veins(t, p, VEIN, 1.5, alpha=.45)
# trousers on legs + pelvis, a belt sunk into the overhanging gut
for p in ('right_leg', 'left_leg'):
    t.fill(p, PANTS, 10); grime(t, p, (40, 32, 24), .6, 2, 3)
    t.fill(p, (52, 44, 40), 12, cubes=[2])                          # boots
t.fill('body', PANTS, 10, cubes=[0]); grime(t, 'body', (40, 32, 24), .5, 2, 4, cubes=[0])
t.overlay('body', lambda f, x, y, w, h: f not in ('top', 'bottom') and t.rect(x, y, w, 1, (40, 30, 22)), cubes=[0])
# stretch marks on the gut: pale pink streaks
def stretch(f, x, y, w, h):
    if f in ('top', 'bottom'): return
    for _ in range(w * h // 18):
        sx, sy = x + t.rng.randrange(w), y + t.rng.randrange(h)
        for k in range(t.rng.randint(2, 4)):
            if sy + k < y + h: t.put(sx + (k > 1), sy + k, mix(t.px[sx, sy + k], (214, 150, 150), .55))
t.overlay('belly', stretch, cubes=[0, 1])
boils(t, 'belly', .5, (1, 3), glow=g); boils(t, 'back_sac', 3, (1, 2), glow=g)
boils(t, 'body', .7, (1, 2), cubes=[1, 2, 3, 4]); boils(t, 'right_arm', .8, (1, 1)); boils(t, 'left_arm', .8, (1, 1))
for p in ('pustules',):
    t.overlay(p, lambda f, x, y, w, h: [t.put(x + i, y + j, mix((236, 220, 110), (150, 60, 40), j / max(1, h))) for j in range(h) for i in range(w)])
    t.overlay(p, lambda f, x, y, w, h: [g.put(x + i, y + j, (220, 200, 80, 170)) for j in range(h) for i in range(w)], faces=['front'])
# navel + weeping split in the gut
fx, fy, fw, fh = t.face('belly', 0, 'front')
t.rect(fx + 7, fy + 7, 2, 1, (60, 30, 30)); t.put(fx + 7, fy + 8, (40, 20, 20))
for k in range(5): t.put(fx + 3 + k // 2, fy + 2 + k, (110, 30, 30)); t.put(fx + 4 + k // 2, fy + 2 + k, (210, 200, 90))
grime(t, 'belly', (150, 160, 60), .35, 2, 9)                       # dried bile streaks
# hands: dark swollen fingertips
for p in ('right_fore', 'left_fore'):
    t.fill(p, mix(SKIN, BRUISE, .45), 10, cubes=[1]); grime(t, p, (60, 40, 40), .7, 2, 5, cubes=[1])
# head: tiny sunk eyes, wet mouth, bile dribble
hx, hy, hw, hh = t.face('head', 0, 'front')
t.rect(hx, hy, hw, 3, shade(SKIN, .75))                            # heavy brow shadow
for ex in (hx + 1, hx + 4):
    t.rect(ex, hy + 3, 2, 1, (25, 20, 10)); t.put(ex + (ex > hx + 2), hy + 3, (230, 240, 90)); g.put(ex + (ex > hx + 2), hy + 3, (230, 255, 80, 255))
t.rect(hx + 1, hy + 5, 5, 2, (60, 20, 18)); t.rect(hx + 2, hy + 6, 3, 1, (180, 190, 60))
jx, jy, jw, jh = t.face('jaw', 0, 'front')
t.rect(jx, jy, jw, 1, (50, 18, 16)); [t.put(jx + i, jy, (210, 200, 160)) for i in range(0, jw, 2)]   # crooked teeth
t.fill('drool', BILE, 15); t.overlay('drool', lambda f, x, y, w, h: [g.put(x + i, y + j, BILE + (200,)) for j in range(h) for i in range(w)])
jx, jy, jw, jh = t.face('jaw', 0, 'top'); t.rect(jx, jy, jw, jh, (90, 30, 26))   # inside of mouth when open
tx, ty, tw, th = t.face('throat', 0, 'front'); [t.put(tx + i, ty + 1 + (i % 3 == 0), shade(SKIN, .6)) for i in range(tw)]   # chin fold
for p in flesh: t.blot(p, (100, 24, 20), .5, (1, 3), .6)
t.save('boomer'); g.save('boomer_eyes')
preview(m, 'boomer', scale=10, views=(0, 90, 160, 35))
