"""Five uncommon infected: ceda (hazmat), mudman, roadcrew, fallen (soldier), clown. One script, five specs + textures."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from kit_infected import *

SKIN = (118, 126, 100)
def base(name, **k):
    s = Spec(name).humanoid(**k); s.part('jaw', 'head', (0, -1.5, 0.6), cubes=[C((-3, 0, -5), (6, 2, 5))]); return s
def face(t, skin=SKIN, mouth=True):
    t.fill('head', skin, 9); t.fill('jaw', skin, 9); zombie_face(t, skin=skin, mouth=mouth)
    t.paint('jaw', lambda f, i, j, w, h, o: (110, 25, 25), ('top',))
def limbs(t, arm, fore, leg, shin, skin_fore=True):
    for p in ('right_arm', 'left_arm'): t.fill(p, arm, 8); t.folds(p, .8)
    for p in ('right_forearm', 'left_forearm'): t.fill(p, SKIN if skin_fore else fore, 9)
    for p in ('right_leg', 'left_leg'): t.fill(p, leg, 7); t.folds(p, .85)
    for p in ('right_shin', 'left_shin'): t.fill(p, shin, 7)

# ---------- CEDA: yellow hazmat suit, filter mask, back tank ----------
s = base('ceda', inf_body=.4, inf_arm=.3, inf_leg=.3)
s.part('hood', 'head', cubes=[C((-4.5, -8.6, -4.5), (9, 8, 9))])
s.part('filter', 'head', (0, -3, -4.6), cubes=[C((-2.5, 0, -2), (5, 4, 3))])
s.part('tank', 'body', cubes=[C((-3.5, -11, 2.2), (7, 9, 4))])
s.part('hose', 'body', (0, -9, 2), (-0.5, 0, 0), cubes=[C((-0.5, 0, -4), (1, 1, 5))])
m = s.save(); check(m); t = Paint(m, seed=21); Y = (214, 186, 40)
face(t); t.fill('hood', Y, 6); t.paint('hood', lambda f, i, j, w, h, o: (0, 0, 0, 0) if (f == 'front' and 1 <= i <= 7 and 1 <= j <= 6) or f == 'bottom' else None, ('front', 'bottom'))
t.fill('body', Y, 7); t.folds('body', .78, 5); limbs(t, Y, Y, Y, Y, skin_fore=False)
for p in ('right_forearm', 'left_forearm'): t.band(p, 3, 6, (40, 40, 44), from_bottom=True)
for p in ('right_shin', 'left_shin'): t.band(p, 0, 2, (30, 30, 34), from_bottom=True)
t.fill('filter', (70, 74, 70), 6); t.fill('tank', (150, 160, 150), 6); t.band('tank', 0, 1, (200, 40, 30)); t.fill('hose', (30, 30, 32), 4)
t.band('body', 4, 5, (210, 210, 220), faces=('front',)); t.blot('body', (120, 140, 40), 8, (3, 5), .6)    # toxic stains
for p in ('hood', 'body', 'right_arm', 'left_leg'): t.grime(p, 8, (90, 120, 30), .55); t.blood(p, 2)
t.save('ceda'); glow(m, 'ceda', [('head', 'front', 1, 3, (255, 230, 60)), ('head', 'front', 6, 3, (255, 230, 60))]); look(m, 'ceda', 'ceda')

# ---------- MUDMAN: caked in mud, lumpy, hunched ----------
s = base('mudman', inf_body=.8, inf_arm=.6, inf_leg=.4)
s.part('lump_a', 'body', cubes=[C((-5, -13, -3), (6, 4, 5))]); s.part('lump_b', 'body', cubes=[C((0, -6, 1.5), (6, 5, 3))])
s.part('crust', 'head', cubes=[C((-4.7, -8.7, -4.7), (9, 5, 9))])
m = s.save(); check(m); t = Paint(m, seed=22); M = (84, 64, 44)
face(t, (96, 92, 74)); t.fill('crust', M, 14); t.paint('crust', lambda f, i, j, w, h, o: (0, 0, 0, 0) if (f == 'front' and j >= 3) or f == 'bottom' else None, ('front', 'bottom'))
t.fill('body', M, 14); limbs(t, M, M, (74, 58, 40), (70, 54, 38)); [t.fill(p, M, 14) for p in ('lump_a', 'lump_b')]
for p in ('right_forearm', 'left_forearm'): t.fill(p, (96, 92, 74), 9); t.band(p, 0, 3, M, noise=14)
for p in ('body', 'right_arm', 'left_arm', 'right_leg', 'left_leg', 'lump_a', 'crust'): t.grime(p, 12, (40, 30, 20), .7); t.grime(p, 6, (120, 96, 64), .6)
for p in ('body', 'right_arm'): t.blood(p, 2, .6)
t.save('mudman'); glow(m, 'mudman', [('head', 'front', 1, 3, (230, 220, 150)), ('head', 'front', 6, 3, (230, 220, 150))]); look(m, 'mudman', 'mudman')

# ---------- ROADCREW: orange vest, hard hat, ear defenders, pickaxe-like tool in the hand ----------
s = base('roadcrew', inf_body=.35, inf_arm=.2, inf_leg=.2)
s.part('hat', 'head', cubes=[C((-4, -8.8, -4), (8, 3, 8), .6), C((-3, -9.4, -7), (6, 1, 3))])
s.part('muff_r', 'head', cubes=[C((-5.8, -5.5, -1.5), (2, 4, 3))]); s.part('muff_l', 'head', cubes=[C((3.8, -5.5, -1.5), (2, 4, 3))])
s.part('vest', 'body', cubes=[C((-4, -12, -2), (8, 11, 4), .4)])
s.part('tool', 'right_forearm', (0, 5, -1), cubes=[C((-0.5, -12, -0.5), (1, 22, 1)), C((-4, -13, -0.5), (8, 2, 1))])
m = s.save(); check(m); t = Paint(m, seed=23); O = (232, 118, 24)
face(t); t.fill('hat', (240, 200, 30), 6); t.fill('muff_r', (50, 50, 56), 5); t.fill('muff_l', (50, 50, 56), 5)
t.fill('body', (92, 92, 100), 8); t.fill('vest', O, 7); t.band('vest', 5, 6, (225, 225, 200)); t.band('vest', 8, 9, (225, 225, 200))
t.paint('vest', lambda f, i, j, w, h, o: (0, 0, 0, 0) if f == 'bottom' else None, ('bottom',))
limbs(t, (92, 92, 100), SKIN, (64, 70, 84), (58, 62, 74)); t.fill('tool', (100, 76, 52), 6); t.paint('tool', lambda f, i, j, w, h, o: (150, 154, 160) if j < 2 else None, SIDES, cubes=[1]); t.fill('tool', (150, 154, 160), 5, cubes=[1])
for p in ('vest', 'hat', 'right_arm', 'right_leg', 'tool'): t.grime(p, 6, (50, 40, 30), .5); t.blood(p, 2)
t.save('roadcrew'); look(m, 'roadcrew', 'roadcrew')

# ---------- FALLEN: tattered tactical kit, plate carrier, helmet, bandolier ----------
s = base('fallen', inf_body=.4, inf_arm=.2, inf_leg=.3)
s.part('helmet', 'head', cubes=[C((-4, -9, -4), (8, 5, 8), .6)]); s.part('strap', 'head', cubes=[C((-0.5, -9.3, -4), (1, 1, 8), .5)])
s.part('carrier', 'body', cubes=[C((-4, -12, -2), (8, 9, 4), .6)]); s.part('pouch_r', 'body', cubes=[C((-4.6, -4, -3.2), (4, 3, 2)), C((0.6, -4, -3.2), (4, 3, 2))])
s.part('pack', 'body', cubes=[C((-3, -11, 2.4), (6, 8, 3))])
for sd in ('right', 'left'): s.part(sd + '_pad', sd + '_shin', cubes=[C((-2, 0, -2.6), (4, 3, 1), .3)])
m = s.save(); check(m); t = Paint(m, seed=24); G = (70, 78, 58)
face(t, (130, 124, 104)); t.fill('helmet', G, 7); t.paint('helmet', lambda f, i, j, w, h, o: (0, 0, 0, 0) if f == 'bottom' else None, ('bottom',)); t.fill('strap', (50, 54, 44), 4)
t.fill('body', (88, 94, 70), 8); t.fill('carrier', (60, 66, 52), 7); t.folds('carrier', .8); [t.fill(p, (52, 56, 46), 5) for p in ('pouch_r', 'pack')]
for p in ('pouch_r',): t.band(p, 1, 2, (30, 32, 28))
limbs(t, (88, 94, 70), SKIN, (82, 88, 64), (46, 44, 40)); [t.fill(p, (40, 42, 38), 5) for p in ('right_pad', 'left_pad')]
for p in ('right_forearm', 'left_forearm'): t.band(p, 0, 3, (36, 36, 34), from_bottom=True)
t.at('carrier', 'front', 3, 2, (200, 190, 80)); t.at('carrier', 'front', 4, 2, (200, 190, 80))
for p in ('helmet', 'carrier', 'body', 'right_leg', 'left_arm', 'pack'): t.grime(p, 8, (50, 44, 32), .5); t.blood(p, 3)
t.save('fallen'); glow(m, 'fallen', [('head', 'front', 1, 3, (255, 140, 60)), ('head', 'front', 6, 3, (255, 140, 60))]); look(m, 'fallen', 'fallen')

# ---------- CLOWN: ruffle collar, two-point jester hat with bells, baggy stripes, horn ----------
s = base('clown', inf_body=.5, inf_arm=.3, inf_leg=.4)
s.part('collar', 'body', cubes=[C((-5.5, -13, -3.5), (11, 3, 7))])
s.part('hat_a', 'head', (-2, -8, 0), (0, 0, 0.7), cubes=[C((-1.5, -6, -1.5), (3, 6, 3))]); s.part('hat_b', 'head', (2, -8, 0), (0, 0, -0.7), cubes=[C((-1.5, -6, -1.5), (3, 6, 3))])
s.part('hat_base', 'head', cubes=[C((-4, -9, -4), (8, 2, 8), .4)]); s.part('nose', 'head', cubes=[C((-1, -3.5, -5.5), (2, 2, 2))])
s.part('bell_a', 'hat_a', (0, -6, 0), cubes=[C((-1, -2, -1), (2, 2, 2))]); s.part('bell_b', 'hat_b', (0, -6, 0), cubes=[C((-1, -2, -1), (2, 2, 2))])
s.part('horn', 'left_forearm', (0, 6, -1), (-1.2, 0, 0), cubes=[C((-1, -2, -3), (2, 2, 3)), C((-2, -3, -6), (4, 4, 3))])
m = s.save(); check(m); t = Paint(m, seed=25); R, B, W = (200, 40, 70), (50, 100, 200), (232, 228, 214)
face(t, (214, 206, 190), mouth=False); x, y, w, h = t.face('head', 0, 'front'); t.rect(x + 1, y + 5, 6, 2, (200, 20, 40)); t.rect(x + 2, y + 6, 4, 1, (60, 8, 8))
t.fill('nose', (220, 40, 40), 4); t.fill('collar', W, 8); t.folds('collar', .7, 6); t.fill('hat_base', R, 6)
t.fill('hat_a', R, 6); t.fill('hat_b', B, 6); [t.fill(p, (230, 190, 50), 4) for p in ('bell_a', 'bell_b')]
t.fill('body', B, 7); t.band('body', 0, 12, R, cubes=[0], faces=('front', 'back')); t.paint('body', lambda f, i, j, w, h, o: B if (i // 2) % 2 else None, ('front', 'back'))
limbs(t, B, (214, 206, 190), R, R); t.paint('right_leg', lambda f, i, j, w, h, o: W if (j // 2) % 2 else None); t.paint('left_leg', lambda f, i, j, w, h, o: B if (j // 2) % 2 else None)
for p in ('right_forearm', 'left_forearm'): t.band(p, 0, 3, W, from_bottom=True)
for p in ('right_shin', 'left_shin'): t.band(p, 0, 2, (20, 20, 24), from_bottom=True)
t.fill('horn', (230, 190, 50), 5, cubes=[0]); t.fill('horn', (200, 60, 50), 5, cubes=[1])
for p in ('collar', 'body', 'head', 'right_arm', 'left_leg'): t.blood(p, 4); t.grime(p, 4, (60, 40, 30), .4)
t.save('clown'); glow(m, 'clown', [('head', 'front', 1, 3, (140, 255, 90)), ('head', 'front', 6, 3, (140, 255, 90))]); look(m, 'clown', 'clown')
