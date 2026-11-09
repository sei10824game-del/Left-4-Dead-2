"""Gridiron Brute (jimmy): maroon jersey #13, massive shoulder pads, gunmetal helmet with a bone-white cage."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from kit_infected import *

s = Spec('jimmy').humanoid(inf_body=.5, inf_arm=.3, inf_leg=.2)
s.part('jaw', 'head', (0, -1.5, 0.6), cubes=[C((-3, 0, -5), (6, 2, 5))])
s.part('helmet', 'head', cubes=[C((-4.5, -8.9, -4.5), (9, 8, 9))])
s.part('cage', 'helmet', (0, -4.5, -4.6), (8, 0, 0), cubes=[C((-3.5, 0, -1), (7, 4, 1))])
s.part('pads', 'body', cubes=[C((-6.5, -13.2, -3.5), (13, 3, 7)), C((-4, -10.4, -3.1), (8, 3, 1))])
s.part('pad_r', 'right_arm', (0, 0, 0), (0, 0, 12), cubes=[C((-4.6, -3.4, -3), (5, 4, 6))])
s.part('pad_l', 'left_arm', (0, 0, 0), (0, 0, -12), cubes=[C((-0.4, -3.4, -3), (5, 4, 6))])
for sd in ('right', 'left'):
    s.part(sd + '_knee', sd + '_shin', cubes=[C((-2, -1, -2.8), (4, 3, 1))])
m = s.save(); check(m)

t = Paint(m, seed=13)
SKIN, JER, BONE, GUN, PANT = (112, 122, 98), (112, 28, 40), (226, 216, 192), (72, 78, 88), (168, 166, 156)
for p in ('head', 'jaw'): t.fill(p, SKIN, 9)
zombie_face(t, skin=SKIN, mouth=False)
x, y, w, h = t.face('head', 0, 'front'); t.rect(x + 2, y + 6, 4, 2, (55, 12, 10))
x, y, w, h = t.face('jaw', 0, 'front'); t.rect(x + 1, y, 4, 1, (60, 120, 200)); t.put(x + 2, y, (40, 90, 160))   # mouthguard
t.paint('jaw', lambda f, i, j, w, h, o: (110, 25, 25), ('top',))
t.fill('body', JER, 8); t.folds('body', .8)
for p in ('right_arm', 'left_arm'): t.fill(p, JER, 8)
for p in ('right_forearm', 'left_forearm'):
    t.fill(p, SKIN, 9)
    t.band(p, 2, 3, (215, 212, 200), noise=8)                                      # wrist tape
    t.band(p, 0, 2, (40, 40, 44), from_bottom=True)                                # gloves
    t.paint(p, lambda f, i, j, w, h, o: (36, 36, 40), ('bottom',))
    t.blot(p, (120, 30, 25), 4, (1, 3), .7)
for p in ('right_leg', 'left_leg'): t.fill(p, PANT, 7); t.folds(p, .85)
for p in ('right_shin', 'left_shin'):
    t.fill(p, PANT, 7); t.band(p, 2, 6, JER, from_bottom=True)                       # socks
    t.band(p, 0, 2, (24, 24, 26), from_bottom=True, noise=14); t.paint(p, lambda f, i, j, w, h, o: (20, 20, 22), ('bottom',))
    t.band(p, 3, 4, BONE, from_bottom=True)                                         # sock stripe
# sleeves: two bone stripes
for p in ('right_arm', 'left_arm'): t.band(p, 3, 4, BONE); t.band(p, 5, 6, BONE)
# jersey numbers "13" (3x5 font) front and back, side stripes
DIG = {'1': ['010', '110', '010', '010', '111'], '3': ['111', '001', '011', '001', '111']}
for f, y0 in (('front', 7), ('back', 2)):
    x, y, w, h = t.face('body', 0, f)
    for k, ch in enumerate('13'):
        for r, row in enumerate(DIG[ch]):
            for c_, b in enumerate(row):
                if b == '1': t.put(x + 1 + k * 4 + c_, y + y0 + r, shade(BONE, .95 + t.rng.random() * .1))
for f in ('right', 'left'):
    x, y, w, h = t.face('body', 0, f); t.rect(x + 1, y, 2, h, BONE)
t.band('body', 0, 2, (40, 40, 44), from_bottom=True)                                 # belt
# helmet: gunmetal shell, maroon center stripe, face opening (cutout), chin strap
t.fill('helmet', GUN, 6, .3)
t.paint('helmet', lambda f, i, j, w, h, o: shade(o, 1.3) if f in ('top',) and 3 <= i <= 5 else None, ('top',))
t.paint('helmet', lambda f, i, j, w, h, o: JER if (f == 'top' and i == 4) or (f in ('front', 'back') and i == 4 and j < 3) else None, None)
t.paint('helmet', lambda f, i, j, w, h, o: (0, 0, 0, 0) if (1 <= i <= 7 and 2 <= j) else None, ('front',))
t.paint('helmet', lambda f, i, j, w, h, o: (0, 0, 0, 0) if f == 'bottom' else None, ('bottom',))
for f in ('right', 'left'):
    x, y, w, h = t.face('helmet', 0, f)
    t.rect(x + 3, y + 3, 3, 3, shade(GUN, .75)); t.put(x + 4, y + 4, (30, 30, 34))             # ear hole
    t.rect(x, y + h - 1, 3, 1, (40, 40, 44))
t.blot('helmet', (190, 195, 200), 4, (1, 2), .6)
# cage: bone bars, cutout gaps
t.fill('cage', BONE, 5)
t.paint('cage', lambda f, i, j, w, h, o: (0, 0, 0, 0) if (j in (1, 3) and i not in (0, 3, 6)) else None, ('front', 'back'))
# pads: maroon-covered with bone trim
t.fill('pads', JER, 7, cubes=[0]); t.fill('pads', JER, 7, cubes=[1])
for p in ('pad_r', 'pad_l'): t.fill(p, JER, 7)
for p in ('pads', 'pad_r', 'pad_l'):
    t.paint(p, lambda f, i, j, w, h, o: BONE if (f not in ('top', 'bottom') and j == h - 1) else None, None)
    t.blot(p, (60, 10, 14), 4, (2, 3), .6)
x, y, w, h = t.face('pads', 0, 'top')
for i in range(w): t.put(x + i, y + h // 2, shade(JER, .7))
for p in ('right_knee', 'left_knee'): t.fill(p, BONE, 6); t.edge(p, .75)
for p in ('body', 'right_leg', 'left_leg', 'right_shin', 'left_shin'): t.grime(p, 6, (70, 90, 40), .5)   # grass stains
for p in ('head', 'pads', 'right_arm', 'right_leg', 'right_forearm'): t.blood(p, 3)
t.blood('body', 2)
t.save('jimmy')
look(m, 'jimmy', 'jimmy')
look(m, 'jimmy', 'jimmy_tackle', pose={'body': (0.9, 0.4, 0), 'left_arm': (-0.4, 0, -0.3), 'right_arm': (-1.0, 0, 0.3),
                                         'right_leg': (0.6, 0, 0), 'left_leg': (-1.0, 0, 0), 'left_shin': (0.9, 0, 0)})
