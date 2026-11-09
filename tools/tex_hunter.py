"""Hunter: ash-blue corpse skin, autopsy-stitched ribcage, bandaged forearms, olive tarp mantle + rag tail, bone claws."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *

m = Model('hunter'); t = Tex(m, seed=7); e = Tex(m)
SKIN, BRUISE, TARP, PANTS = (150, 160, 166), (95, 80, 110), (92, 88, 56), (52, 52, 58)
BONE, BLOOD, DARK, BAND = (205, 196, 165), (110, 14, 18), (18, 12, 14), (176, 170, 150)
GLOW = (200, 255, 110, 255)
SIDES = ('front', 'back', 'left', 'right')

def paint(part, cube, face, fn, tex=None):
    """fn(i, j, w, h) -> colour or None, per pixel of one face."""
    x, y, w, h = t.face(part, cube, face)
    for j in range(h):
        for i in range(w):
            c = fn(i, j, w, h)
            if c is not None: (tex or t).put(x + i, y + j, c)

def clear(part, cube, face, fn):
    x, y, w, h = t.face(part, cube, face)
    for j in range(h):
        for i in range(w):
            if fn(i, j, w, h): t.px[x + i, y + j] = (0, 0, 0, 0)

def n(c, a=.08): return shade(c, 1 + t.rng.uniform(-a, a))

# ---- base fills ----
for p in ('body', 'head', 'jaw', 'right_arm', 'left_arm', 'right_shin', 'left_shin'): t.fill(p, SKIN, 9)
t.fill('body', TARP, 10, cubes=[2])
t.fill('body', BONE, 6, cubes=[3, 4, 5])
for p in ('rag', 'rag_front'): t.fill(p, TARP, 12, grad=.35)
for p in ('right_fore', 'left_fore'): t.fill(p, BAND, 8)
for p in ('right_hand', 'left_hand'): t.fill(p, shade(SKIN, .7), 8, cubes=[0]); t.fill(p, BONE, 6, cubes=[1, 2, 3])
for p in ('right_leg', 'left_leg'): t.fill(p, PANTS, 10)
for p in ('right_foot', 'left_foot'): t.fill(p, shade(SKIN, .62), 10)
t.fill('head', BONE, 5, cubes=[2])
t.blot('body', BRUISE, 4, (2, 4), .45); t.blot('head', BRUISE, 3, (2, 3), .4)
for p in ('right_arm', 'left_arm', 'right_shin', 'left_shin'): t.blot(p, BRUISE, 5, (2, 4), .45)

# ---- torso: ribs, autopsy Y-incision with stitches ----
for f in SIDES:
    paint('body', 1, f, lambda i, j, w, h: shade(SKIN, .72) if j % 2 == 1 and 0 < i < w - 1 else None)
def incision(i, j, w, h):
    mid = w // 2
    if i == mid: return (90, 25, 30)
    if i in (mid - 1, mid + 1) and j % 2 == 0: return (30, 22, 26)
paint('body', 1, 'front', incision); paint('body', 0, 'front', incision)
paint('body', 1, 'front', lambda i, j, w, h: (90, 25, 30) if j == abs(i - w // 2) and j < 3 else None)  # Y fork
paint('body', 0, 'front', lambda i, j, w, h: shade(SKIN, .55) if j in (1, 2) and i in (1, w - 2) else None)  # hip hollows
for f in SIDES:  # ragged trouser waist with rope belt
    paint('body', 0, f, lambda i, j, w, h: n(PANTS) if j >= h - 2 else ((120, 100, 60) if j == h - 3 else None))
paint('body', 1, 'back', lambda i, j, w, h: shade(SKIN, .65) if abs(i - w / 2 + .5) < 1 else None)  # spine groove
t.blot('body', BLOOD, 3, (2, 4), .8, faces=['front', 'back'])

# ---- tarp mantle: folds, holes, ragged lower edge ----
for f in SIDES + ('top',):
    paint('body', 2, f, lambda i, j, w, h: shade(TARP, .78) if (i * 7 + j * 3) % 5 == 0 else None)
    if f != 'top': clear('body', 2, f, lambda i, j, w, h: j == h - 1 and t.rng.random() < .45)
clear('body', 2, 'back', lambda i, j, w, h: (i, j) in ((2, 1), (6, 0), (7, 1)))
t.blot('body', (60, 50, 30), 6, (2, 4), .6, faces=['top'])
for p in ('rag', 'rag_front'):
    for f in ('front', 'back'):
        paint(p, 0, f, lambda i, j, w, h: shade(TARP, .7) if i % 3 == 1 and t.rng.random() < .6 else None)
        clear(p, 0, f, lambda i, j, w, h: (j >= h - 1 - (i * 5 % 3)) or (j > 2 and i == w // 2 + 1 and j < h - 3))
    t.blot(p, BLOOD, 6, (2, 4), .7); t.blot(p, (55, 45, 25), 8, (2, 4), .6)

# ---- head: deep sockets, lipless muzzle, stringy hair ----
paint('head', 0, 'front', lambda i, j, w, h: (DARK if j in (1, 2) and i in (0, 1, 4, 5) else
                                            shade(SKIN, .6) if j == 0 and i in (0, 1, 4, 5) else
                                            shade(SKIN, .5) if j == 3 and i in (2, 3) else None))
paint('head', 0, 'front', lambda i, j, w, h: (225, 255, 150) if j == 2 and i in (1, 4) else None)
paint('head', 0, 'front', lambda i, j, w, h: GLOW if j == 2 and i in (1, 4) else None, e)
paint('head', 1, 'front', lambda i, j, w, h: (DARK if (i % 2 == 1 and j == 1) else (225, 210, 160)) if j >= 1 else (120, 40, 45))
for f in ('left', 'right'): paint('head', 1, f, lambda i, j, w, h: (120, 40, 45) if j == 0 else (210, 196, 150))
paint('jaw', 0, 'front', lambda i, j, w, h: ((225, 210, 160) if i % 2 == 0 else DARK) if j == 0 else None)
paint('jaw', 0, 'top', lambda i, j, w, h: (225, 210, 160) if i in (0, w - 1) or j == 0 else n((105, 30, 38)))
for f in ('left', 'right'): paint('jaw', 0, f, lambda i, j, w, h: (225, 210, 160) if j == 0 and i % 2 == 0 else None)
HAIR = (26, 22, 24)
paint('head', 0, 'top', lambda i, j, w, h: n(HAIR, .2) if (i + j * 2) % 3 != 0 else None)
for f in ('back', 'left', 'right'):
    paint('head', 0, f, lambda i, j, w, h: n(HAIR, .2) if j < 2 + ((i * 3) % 4) and (i + j) % 4 != 1 else None)
t.blot('head', BLOOD, 2, (1, 3), .7, faces=['front'])
t.blot('jaw', BLOOD, 4, (1, 3), .8, faces=['front', 'bottom', 'left', 'right'])

# ---- arms: sores, bandage wraps, bone claws darkening at the tips ----
for p in ('right_fore', 'left_fore'):
    for f in SIDES:
        paint(p, 0, f, lambda i, j, w, h: n(shade(BAND, .72)) if (i + j) % 3 == 0 else (n(SKIN) if j == h - 1 else None))
    t.blot(p, BLOOD, 6, (2, 4), .55); t.blot(p, (80, 70, 50), 6, (2, 3), .5)
for p in ('right_arm', 'left_arm'):
    t.blot(p, BLOOD, 3, (1, 3), .8)
    paint(p, 0, 'top', lambda i, j, w, h: n(TARP))
for p in ('right_hand', 'left_hand'):
    for c in (1, 2, 3):
        for f in SIDES: paint(p, c, f, lambda i, j, w, h: mix(BONE, (35, 25, 25), j / max(1, h - 1)))

# ---- legs: torn trousers, bare shins, clawed feet ----
for p in ('right_leg', 'left_leg'):
    for f in SIDES:
        paint(p, 0, f, lambda i, j, w, h: n(SKIN) if (j >= h - 2 - (i % 2) or (f == 'front' and 1 <= j <= 2 and i == 1)) else None)
    t.blot(p, (40, 38, 32), 6, (2, 4), .6)
for p in ('right_shin', 'left_shin'):
    t.blot(p, BLOOD, 3, (1, 3), .7)
for p in ('right_foot', 'left_foot'):
    paint(p, 0, 'top', lambda i, j, w, h: (30, 22, 22) if j == 0 else None)
    paint(p, 0, 'front', lambda i, j, w, h: (30, 22, 22))

t.save('hunter'); e.save('hunter_eyes')
if __name__ == '__main__': preview(m, 'hunter')
