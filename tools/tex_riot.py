"""Shield Infected (riot): slate-teal fatigues, steel-blue plates, smoked amber visor, forearm-strapped slab shield."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from kit_infected import *

s = Spec('riot').humanoid(inf_body=.25)
s.part('jaw', 'head', (0, -1.5, 0.6), cubes=[C((-3, 0, -5), (6, 2, 5))])
s.part('helmet', 'head', cubes=[C((-4.5, -9.2, -4.5), (9, 5, 9)), C((-4.5, -4.2, 3.4), (9, 3, 1))])
s.part('visor', 'helmet', (0, -6.2, -4.5), (-12, 0, 0), cubes=[C((-4.5, 0, -1), (9, 4, 1))])
s.part('plate', 'body', cubes=[C((-4.5, -11.6, -3), (9, 8, 1)), C((-4.5, -11.6, 2.1), (9, 9, 1)), C((-4.5, -2, -2.6), (9, 2, 5))])
s.part('pauldron_r', 'right_arm', (0, 0, 0), (0, 0, 8), cubes=[C((-3.6, -2.8, -2.6), (5, 3, 5))])
s.part('pauldron_l', 'left_arm', (0, 0, 0), (0, 0, -8), cubes=[C((-1.4, -2.8, -2.6), (5, 3, 5))])
s.part('shield', 'left_forearm', cubes=[C((-4, -3.5, 2.4), (8, 13, 1)), C((-1, 1, 2), (2, 3, 0.5) if False else (2, 3, 1))])
for sd in ('right', 'left'):
    s.part(sd + '_guard', sd + '_shin', cubes=[C((-2.5, -0.5, -2.7), (5, 5, 1))])
m = s.save(); check(m)

t = Paint(m, seed=7)
SKIN, FAT, STEEL, DARK = (128, 140, 112), (58, 70, 74), (88, 104, 120), (34, 38, 44)
t.fill('head', SKIN, 9); t.fill('jaw', SKIN, 9)
zombie_face(t, skin=SKIN, mouth=False)
x, y, w, h = t.face('jaw', 0, 'front'); t.rect(x + 1, y, 4, 1, (190, 180, 140)); t.put(x + 3, y, (90, 20, 20))
t.paint('jaw', lambda f, i, j, w, h, o: (110, 25, 25), ('top',))
x, y, w, h = t.face('head', 0, 'front'); t.rect(x + 2, y + 6, 4, 2, (60, 14, 12))
for p in ('body', 'right_arm', 'left_arm', 'right_leg', 'left_leg', 'right_shin', 'left_shin'):
    t.fill(p, FAT, 9); t.folds(p, .8)
for p in ('right_forearm', 'left_forearm'):
    t.fill(p, FAT, 9); t.band(p, 0, 2, (30, 30, 32), from_bottom=True)   # gloves
    t.paint(p, lambda f, i, j, w, h, o: (28, 28, 30), ('bottom',))
for p in ('right_shin', 'left_shin'):
    t.band(p, 0, 2, (26, 24, 24), from_bottom=True, noise=12); t.paint(p, lambda f, i, j, w, h, o: (22, 20, 20), ('bottom',))


def armor(p, base, cubes=None, scratches=6):
    t.fill(p, base, 6, .3, cubes=cubes)
    t.paint(p, lambda f, i, j, w, h, o: shade(o, 1.25) if (j == 0 and f not in ('top', 'bottom')) else
            (shade(o, .7) if j == h - 1 and h > 2 else None), None, cubes)            # bevel
    t.blot(p, (170, 175, 180), scratches, (1, 3), .6)                                  # chipped paint
    t.blot(p, (90, 60, 40), scratches // 2, (1, 2), .5)                                # rust


armor('helmet', STEEL); armor('plate', STEEL, [0, 1]); armor('plate', DARK, [2], 3)
armor('pauldron_r', STEEL); armor('pauldron_l', STEEL)
armor('right_guard', DARK, scratches=3); armor('left_guard', DARK, scratches=3)
# helmet: stencil id number on the sides, dent
for f in ('right', 'left'):
    x, y, w, h = t.face('helmet', 0, f)
    t.rect(x + 3, y + 2, 1, 2, (220, 215, 190)); t.rect(x + 5, y + 2, 1, 2, (220, 215, 190)); t.put(x + 4, y + 3, (220, 215, 190))
# visor: smoked amber glass with a crack
t.fill('visor', (70, 58, 30), 6, .5)
x, y, w, h = t.face('visor', 0, 'front')
for i in range(w): t.put(x + i, y, (140, 120, 70))
for i, j in ((6, 1), (7, 2), (7, 3), (5, 2)): t.put(x + i, y + j, (190, 180, 140))
t.rect(x + 1, y + 1, 2, 1, (40, 30, 16)); t.rect(x + 5, y + 1, 2, 1, (40, 30, 16))
# chest plate: chevron + unit stencil
x, y, w, h = t.face('plate', 0, 'front')
for i in range(w):
    t.put(x + i, y + 2 + abs(i - 4) // 2, (200, 120, 40))
t.rect(x + 2, y + 5, 5, 1, (210, 205, 180))
# belt pouches
x, y, w, h = t.face('plate', 2, 'front'); t.rect(x + 1, y, 2, 2, (50, 54, 40)); t.rect(x + 6, y, 2, 2, (50, 54, 40))
# shield: dented steel slab, viewport slit, orange hazard band, scrapes and blood smears
t.fill('shield', (96, 108, 118), 7, .25, cubes=[0])
x, y, w, h = t.face('shield', 0, 'back')            # +z face = outer face (faces the enemy when raised)
for i in range(w):
    t.put(x + i, y, (60, 66, 72)); t.put(x + i, y + h - 1, (140, 150, 160))         # wrist end = top when raised
    t.rect(x + i, y + h - 3, 1, 2, (210, 120, 30) if (i // 2) % 2 == 0 else (30, 30, 34))
t.rect(x + 1, y + h - 6, 6, 1, (70, 76, 84)); t.rect(x + 1, y + h - 5, 6, 1, (20, 20, 24))    # viewport slit
for j in range(h): t.put(x, y + j, (130, 140, 150)); t.put(x + w - 1, y + j, (60, 66, 72))
t.blot('shield', (100, 15, 12), 5, (2, 5), .8); t.blot('shield', (170, 175, 180), 6, (1, 3), .5)
t.fill('shield', (40, 40, 44), 6, cubes=[1])                                          # grip
for p in ('body', 'right_leg', 'left_leg', 'right_shin', 'left_shin', 'right_forearm'): t.grime(p, 5)
for p in ('body', 'head', 'right_arm', 'left_forearm', 'plate'): t.blood(p, 3)
t.save('riot')
glow(m, 'riot', [('visor', 'front', 1, 1, (255, 170, 60)), ('visor', 'front', 6, 1, (255, 170, 60))])
look(m, 'riot', 'riot')
look(m, 'riot', 'riot_guard', pose={'left_arm': (-1.45, 0.45, 0), 'left_forearm': (-1.5, 0, 0), 'body': (0.15, 0, 0)})
