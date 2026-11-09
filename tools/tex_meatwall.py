"""Meat Wall: a wide slab of fused, stitched flesh with tiny sunken head, huge fists and bulging flesh masses."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from kit_infected import *
s = Spec('meatwall', (256, 128))
s.part('body', None, (0, 6, 0), cubes=[C((-14, -22, -7), (28, 22, 14), .5)])
s.part('mass_a', 'body', (0, 0, 0), cubes=[C((-16, -18, -4), (8, 12, 8)), C((8, -24, -4), (8, 10, 8))])
s.part('mass_b', 'body', (0, 0, 0), cubes=[C((-6, -26, 5), (12, 8, 5)), C((-10, -8, -9), (8, 6, 4))])
s.part('head', 'body', (0, -21, -2), cubes=[C((-4, -7, -4), (8, 7, 8))])
s.part('jaw', 'head', (0, -2, 0), cubes=[C((-3.5, 0, -4.5), (7, 3, 5))])
for n, sx in (('right', -1), ('left', 1)):
    s.part(n + '_arm', 'body', (17 * sx, -18, 0), cubes=[C((-5 if sx < 0 else -5, -2, -5), (10, 16, 10), .4)])
    s.part(n + '_fist', n + '_arm', (0, 14, 0), cubes=[C((-6.5, 0, -6.5), (13, 11, 13))])
    s.part(n + '_leg', None, (9 * sx, 6, 0), cubes=[C((-5, 0, -5), (10, 18, 10))])
m = s.save(); check(m); t = Paint(m, seed=33)
FL, DK, BN = (166, 80, 78), (96, 38, 40), (214, 200, 170)
for p in ('body', 'mass_a', 'mass_b', 'right_arm', 'left_arm', 'right_leg', 'left_leg', 'right_fist', 'left_fist'):
    t.fill(p, FL, 14); t.folds(p, .72, 6); t.grime(p, 14, DK, .7); t.blood(p, 8)
for p in ('right_fist', 'left_fist'): t.grime(p, 8, BN, .8)                                   # exposed knuckle bone
t.fill('head', (150, 120, 110), 9); t.fill('jaw', (130, 70, 70), 9); zombie_face(t, skin=(150, 120, 110), mouth=False)
t.paint('jaw', lambda f, i, j, w, h, o: (200, 190, 160) if f == 'front' and i % 2 == 0 and j == 0 else None)  # teeth row
# stitching across the torso
x, y, w, h = t.face('body', 0, 'front')
for i in range(0, w, 2): t.put(x + i, y + h // 2, (30, 14, 14)); t.put(x + i, y + h // 2 + 1, (200, 190, 160))
t.save('meatwall'); glow(m, 'meatwall', [('head', 'front', 1, 3, (255, 200, 60)), ('head', 'front', 6, 3, (255, 200, 60))])
look(m, 'meatwall', 'meatwall')
