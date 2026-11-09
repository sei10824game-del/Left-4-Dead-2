"""Common infected: model spec (common.json) + 6 skin variants common_0..5
(office shirt, hoodie, work coveralls, scrubs, tank-top, suit). Reference for the uncommon scripts."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from kit_infected import *


def spec(name='common', tex=(64, 64)):
    s = Spec(name, tex).humanoid()
    s.by['head']['rot'] = [0, 0, 6]                       # asymmetric head tilt
    s.by['right_arm']['pivot'][1] = -9.5                  # dropped right shoulder
    s.part('hair', 'head', cubes=[C((-4, -8, -4), (8, 8, 8), .35)])
    s.part('jaw', 'head', (0, -1.5, 0.6), cubes=[C((-3, 0, -5), (6, 2, 5))])
    s.part('flap_front', 'body', (-1.5, -1, -2.05), cubes=[C((-2, 0, 0), (4, 4, 0))])
    s.part('flap_back', 'body', (0.5, -1, 2.05), cubes=[C((-3, 0, 0), (6, 5, 0))])
    return s


SKINS = [(150, 150, 118), (160, 140, 112), (130, 138, 112), (170, 150, 125), (140, 125, 105), (150, 160, 130)]
# (torso, arms (None = bare), legs, accent)
OUTFITS = [((200, 200, 205), (200, 200, 205), (60, 62, 75), (120, 30, 30)),     # white shirt + tie
           ((70, 78, 95), (70, 78, 95), (50, 55, 70), (20, 20, 25)),             # hoodie jeans
           ((200, 120, 30), (200, 120, 30), (200, 120, 30), (40, 40, 40)),       # coveralls
           ((90, 160, 160), (90, 160, 160), (90, 160, 160), (200, 200, 200)),    # scrubs
           ((150, 40, 40), None, (110, 100, 70), (20, 20, 20)),                  # tank top, bare arms
           ((35, 35, 42), (35, 35, 42), (35, 35, 42), (200, 200, 200))]          # suit
HAIR = [(40, 30, 25), (20, 18, 16), (110, 90, 50), (55, 35, 25), None, (160, 160, 160)]


def ragged(t, part, depth=2):
    """tear the bottom edge of a cloth plane (transparent notches)."""
    def f(fc, i, j, w, h, o):
        if j >= h - 1 - (hh((i, part)) % (depth + 1)): return (0, 0, 0, 0)
    t.paint(part, f, ('front', 'back'))


def make(v, m):
    t = Paint(m, seed=10 + v)
    skin = SKINS[v]; torso, arms, legs, acc = OUTFITS[v]
    for p in ('head', 'jaw'): t.fill(p, skin, 9)
    t.fill('body', torso, 8); t.folds('body')
    for s in ('right', 'left'):
        t.fill(s + '_arm', arms or skin, 9); t.fill(s + '_forearm', arms or skin, 9)
        t.band(s + '_forearm', 0, 2, shade(skin, .85), from_bottom=True)        # hands
        t.paint(s + '_forearm', lambda f, i, j, w, h, o: shade(skin, .8) if f == 'bottom' else None, ('bottom',))
        if arms and v in (0, 1, 3):                                               # torn sleeve -> bare forearm
            def tear(f, i, j, w, h, o):
                if j >= (hh((i, f, s)) % 3): return shade(skin, .9 + t.rng.random() * .1)
            t.paint(s + '_forearm', tear)
        t.fill(s + '_leg', legs, 8); t.fill(s + '_shin', legs, 8); t.folds(s + '_shin', .85)
        t.band(s + '_shin', 0, 2, (40, 36, 34), from_bottom=True, noise=14)       # shoes
        t.paint(s + '_shin', lambda f, i, j, w, h, o: (34, 30, 28), ('bottom',))
    for fl in ('flap_front', 'flap_back'):
        t.fill(fl, shade(torso, .92), 8, faces=('front', 'back')); ragged(t, fl)
    # face + veins + jaw teeth / gums
    t.blot('head', (80, 90, 110), 5, (2, 4), .5)
    zombie_face(t, skin=skin, mouth=False)
    x, y, w, h = t.face('head', 0, 'front')
    t.rect(x + 2, y + 6, 4, 1, (60, 15, 12)); t.put(x + 3, y + 7, (40, 8, 8)); t.put(x + 4, y + 7, (40, 8, 8))
    x, y, w, h = t.face('jaw', 0, 'front')
    t.rect(x + 1, y, 4, 1, (200, 190, 150)); t.put(x + 2, y, (120, 30, 25)); t.rect(x, y + 1, 6, 1, shade(skin, .8))
    t.paint('jaw', lambda f, i, j, w, h, o: (110, 25, 25) if f == 'top' else None, ('top',))
    # hair overlay: top + upper sides only; ragged fringe
    hair = HAIR[v]
    if hair:
        def hf(f, i, j, w, h, o):
            if f == 'top': return shade(hair, .9 + t.rng.random() * .2)
            if f == 'bottom': return None
            lim = 2 + (hh((i, f, v)) % 2) if f == 'front' else 3 + (hh((i, f)) % 3)
            if j < lim: return shade(hair, .85 + t.rng.random() * .2)
        t.paint('hair', hf, None)
    # outfit details on torso front (8x12)
    bx, by, _, _ = t.face('body', 0, 'front')
    if v == 0: t.rect(bx + 3, by, 2, 6, acc); t.rect(bx + 3, by + 6, 2, 1, shade(acc, .7)); t.rect(bx + 3, by, 2, 1, (230, 230, 230))
    if v == 1: t.rect(bx + 1, by + 7, 6, 4, shade(torso, .75)); t.rect(bx + 3, by, 2, 3, shade(torso, 1.2))
    if v == 2: t.rect(bx, by + 8, 8, 1, (210, 210, 40)); t.rect(bx + 3, by, 2, 12, shade(torso, .8))
    if v == 3: t.rect(bx + 2, by + 1, 4, 3, shade(torso, 1.15)); t.rect(bx + 3, by + 2, 2, 1, (170, 30, 30))
    if v == 4: t.rect(bx + 1, by, 6, 3, skin); t.rect(bx + 2, by + 3, 4, 1, shade(skin, .8))
    if v == 5: t.rect(bx + 3, by, 2, 12, (230, 230, 235)); t.rect(bx + 3, by + 2, 2, 8, acc)
    # gash in torso: exposed ribs
    t.rect(bx + 5, by + 5, 2, 3, (110, 20, 18)); t.put(bx + 5, by + 6, (200, 180, 160)); t.put(bx + 6, by + 7, (200, 180, 160))
    t.band('body', 0, 1, shade(legs, .6), from_bottom=True)                       # waistband
    for p in ('body', 'right_arm', 'left_arm', 'right_forearm', 'left_forearm', 'right_leg', 'left_leg', 'head', 'flap_back'):
        t.blood(p, 4 if p == 'body' else 3)
    for p in ('body', 'right_leg', 'left_leg', 'right_shin', 'left_shin'): t.grime(p, 5)
    t.save(f'common_{v}')


if __name__ == '__main__':
    m = spec().save(); check(m)
    for v in range(6): make(v, m)
    look(m, 'common_0', 'common_0'); look(m, 'common_4', 'common_4')
