"""Boomette: tall pear-shaped bloater - narrow sloped shoulders, a gut that sags to the knees, swollen thighs,
goitre sacs on the neck, lank hair down the back, torn stained smock. Same part names as the boomer (shared animation)."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *
from speclib import *

LEG = [([-3, -1, -3], [6, 6, 6], .3), ([-2, 5, -2], [4, 5, 4], 0), ([-2.5, 9, -3.5], [5, 2, 5], 0)]
PARTS = [
    ('right_leg', None, [-3, 13, 0], [0, 0, 0], LEG),
    ('left_leg', None, [3, 13, 0], [0, 0, 0], LEG),
    ('body', None, [0, 13, 0], [6, 0, 0], [
        ([-6.5, -4, -4], [13, 6, 8], 0),            # hips (smock skirt)
        ([-4.5, -15, -3], [9, 11, 6], 0)]),         # narrow chest
    ('belly', 'body', [0, -6, -3], [0, 0, 0], [
        ([-6, -3, -8], [12, 10, 8], .2),            # gut
        ([-5, 6, -6.5], [10, 4, 6], 0),             # sag to the knees
        ([-5, -7, -5], [10, 5, 5], 0)]),            # upper swell
    ('pustules', 'belly', [0, 2, -8], [0, 0, 0], [
        ([-4, -1, -1.5], [3, 3, 2], .2), ([1, 2, -1], [2, 2, 1], .2), ([2, -3, -1], [2, 2, 1], 0)]),
    ('back_sac', 'body', [0, -8, 4], [0, 0, 0], [([-3, -2, -.5], [5, 5, 3], .2), ([1, 1, 0], [3, 3, 2], 0)]),
    ('throat', 'body', [0, -15, -2], [0, 0, 0], [
        ([-3.5, -2, -2.5], [3, 3, 3], .2), ([.5, -2.5, -3], [3, 4, 3], .2), ([-1, -1, -4], [2, 2, 2], 0)]),
    ('head', 'body', [0, -16, -1], [-6, 0, 0], [([-3, -7, -3.5], [6, 7, 6], 0), ([-3.5, -7.5, -3.5], [7, 2, 7], 0)]),
    ('hair', 'head', [0, -6, 2], [8, 0, 0], [([-3.5, -1, -.5], [7, 11, 2], 0)]),
    ('jaw', 'head', [0, -1.5, -1.5], [0, 0, 0], [([-2.5, 0, -2.5], [5, 2, 4], 0)]),
    ('drool', 'jaw', [.5, 1.5, -2], [0, 0, 0], [([-.5, 0, -.5], [1, 3, 1], 0)]),
    ('right_arm', 'body', [-5.5, -13.5, 0], [0, 0, 10], [([-2, -1, -1.5], [3, 7, 3], 0)]),
    ('right_fore', 'right_arm', [-.5, 6, 0], [-12, 0, 0], [([-1.5, 0, -1.5], [3, 6, 3], 0), ([-2, 5, -2], [4, 3, 3], 0)]),
    ('left_arm', 'body', [5.5, -13.5, 0], [0, 0, -10], [([-1, -1, -1.5], [3, 7, 3], 0)]),
    ('left_fore', 'left_arm', [.5, 6, 0], [-12, 0, 0], [([-1.5, 0, -1.5], [3, 6, 3], 0), ([-2, 5, -2], [4, 3, 3], 0)]),
]
build('boomette', PARTS)

SKIN, BRUISE, MOLD, VEIN = (170, 156, 150), (112, 86, 118), (150, 150, 92), (90, 60, 96)
SMOCK, HAIR, BILE = (128, 92, 84), (36, 30, 30), (196, 210, 70)
m = Model('boomette'); t = Tex(m, 11); g = Tex(m, 11)
flesh = ['body', 'belly', 'pustules', 'back_sac', 'throat', 'head', 'jaw', 'right_arm', 'right_fore', 'left_arm', 'left_fore', 'right_leg', 'left_leg']
for i, p in enumerate(flesh):
    t.fill(p, SKIN, 5, .35)
    mottle(t, p, BRUISE, .55, 4, i + 40); mottle(t, p, MOLD, .4, 5, i + 60)
    veins(t, p, VEIN, 1.5, alpha=.5)
# torn smock: chest + hips + top of gut, ragged hem, stains
def smock(p, cubes, hem=True):
    t.fill(p, SMOCK, 8, .25, cubes=cubes); grime(t, p, (70, 60, 40), .5, 2, 7, cubes=cubes)
    if hem:
        def rag(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for i in range(w):
                for j in range(h - t.rng.randint(0, 3), h): t.put(x + i, y + j, mix(SKIN, BRUISE, .4))
        t.overlay(p, rag, cubes=cubes)
smock('body', [0, 1], False); smock('belly', [2])
t.overlay('body', lambda f, x, y, w, h: f in ('front', 'back') and [t.put(x + i, y + h - 1 - (i * 7 % 3), (40, 30, 26)) for i in range(w)], cubes=[0])
for p in ('right_leg', 'left_leg'):
    grime(t, p, (60, 40, 44), .7, 2, 13); t.fill(p, (60, 50, 46), 10, cubes=[2])                  # bare filthy legs, slippers
t.blot('body', (150, 160, 60), 3, (2, 5), .55); t.blot('belly', (150, 160, 60), 1.5, (2, 5), .55)  # bile down the front
# gut: shiny taut skin with dark stretch lines + big pustules
t.overlay('belly', lambda f, x, y, w, h: f in ('front', 'left', 'right') and [t.put(x + i, y + 2 + (i * 5) % (h - 3), mix(t.px[x + i, y + 2 + (i * 5) % (h - 3)], (90, 50, 70), .5)) for i in range(0, w, 3)], cubes=[0, 1])
boils(t, 'belly', .6, (1, 3), glow=g, cubes=[0, 1]); boils(t, 'back_sac', 3, (1, 2), glow=g); boils(t, 'throat', 2, (1, 1), glow=g)
boils(t, 'right_leg', .5, (1, 1), cubes=[0]); boils(t, 'left_leg', .5, (1, 1), cubes=[0])
t.overlay('pustules', lambda f, x, y, w, h: [t.put(x + i, y + j, mix((236, 220, 110), (140, 60, 60), j / max(1, h))) for j in range(h) for i in range(w)])
t.overlay('pustules', lambda f, x, y, w, h: [g.put(x + i, y + j, (220, 200, 80, 170)) for j in range(h) for i in range(w)], faces=['front'])
# hair: lank strands over the skull, matted at the tips
for p, cs in (('hair', None), ('head', [1])):
    t.fill(p, HAIR, 14, .1, cubes=cs)
    t.overlay(p, lambda f, x, y, w, h: [t.put(x + i, y + j, shade(HAIR, 1.5)) for i in range(0, w, 2) for j in range(h) if (i + j * 3) % 5 == 0], cubes=cs)
hx, hy, hw, hh = t.face('hair', 0, 'front'); t.rect(hx, hy, hw, hh, shade(HAIR, .6))
# face: hair fringe, milky glowing eyes, sagging mouth
hx, hy, hw, hh = t.face('head', 0, 'front')
for i in range(hw): t.rect(hx + i, hy, 1, 1 + (i % 3 == 1) + (i in (0, hw - 1)) * 3, HAIR)
for ex in (hx + 1, hx + 4):
    t.rect(ex, hy + 3, 1, 2, (30, 16, 24)); t.put(ex, hy + 3, (220, 235, 120)); g.put(ex, hy + 3, (220, 255, 110, 255))
t.rect(hx + 1, hy + 6, 4, 1, (60, 20, 26))
jx, jy, jw, jh = t.face('jaw', 0, 'front'); t.rect(jx, jy, jw, 1, (50, 18, 20)); [t.put(jx + i, jy, (200, 190, 150)) for i in (0, 2, 3)]
jx, jy, jw, jh = t.face('jaw', 0, 'top'); t.rect(jx, jy, jw, jh, (96, 32, 34))
t.fill('drool', BILE, 15); t.overlay('drool', lambda f, x, y, w, h: [g.put(x + i, y + j, BILE + (200,)) for j in range(h) for i in range(w)])
for p in ('right_fore', 'left_fore'): grime(t, p, (70, 40, 50), .8, 2, 3, cubes=[1])
for p in flesh: t.blot(p, (100, 24, 20), .4, (1, 3), .6)
t.save('boomette'); g.save('boomette_eyes')
preview(m, 'boomette', scale=10, views=(0, 90, 160, 35))
