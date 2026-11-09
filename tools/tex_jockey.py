"""Jockey: jaundiced liver-spotted skin, tumour-crusted hump, frog mouth of needle teeth, mismatched eyes, shredded teal smock."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *

m = Model('jockey'); t = Tex(m, seed=23); e = Tex(m)
SKIN, SPOT, TUMOR, PUS = (168, 146, 92), (110, 88, 50), (176, 88, 92), (220, 205, 110)
SMOCK, ROPE, BLOOD, DARK, TOOTH = (58, 92, 88), (130, 108, 66), (105, 14, 16), (20, 12, 12), (230, 220, 180)
SIDES = ('front', 'back', 'left', 'right')

def paint(part, cube, face, fn, tex=None):
    x, y, w, h = t.face(part, cube, face)
    for j in range(h):
        for i in range(w):
            c = fn(i, j, w, h)
            if c is not None: (tex or t).put(x + i, y + j, c)

def n(c, a=.08): return shade(c, 1 + t.rng.uniform(-a, a))

for p in ('body', 'head', 'jaw', 'right_arm', 'left_arm', 'right_fore', 'left_fore', 'right_leg', 'left_leg', 'right_shin', 'left_shin'):
    t.fill(p, SKIN, 10)
t.fill('body', shade(SKIN, .95), 12, cubes=[1, 5])
t.fill('body', TUMOR, 12, cubes=[2, 3, 4])
for p in ('right_hand', 'left_hand'): t.fill(p, shade(SKIN, .8), 8, cubes=[0]); t.fill(p, (40, 30, 28), 8, cubes=[1, 2, 3])
for p in ('right_foot', 'left_foot'): t.fill(p, shade(SKIN, .7), 8)
for p in ('right_ear', 'left_ear'): t.fill(p, SKIN, 8)
t.fill('rag', SMOCK, 12, grad=.35)
for p in ('body', 'head', 'right_arm', 'left_arm', 'right_leg', 'left_leg'): t.blot(p, SPOT, 5, (1, 3), .55)

# ---- smock remnants on the torso, rope belt, ragged hem ----
for f in SIDES:
    paint('body', 0, f, lambda i, j, w, h: (None if j < 1 + (i * 7) % 3 else ROPE if j == h - 2 else n(SMOCK)))
    paint('body', 0, f, lambda i, j, w, h: n(SKIN) if j == h - 1 and i % 3 == 0 else None)
paint('body', 5, 'front', lambda i, j, w, h: n(SMOCK) if j < 2 and (i + j) % 3 else None)
paint('body', 0, 'front', lambda i, j, w, h: n(SKIN, .05) if 2 <= j <= 3 and i in (2, 3) else None)  # tear over chest
t.blot('body', BLOOD, 3, (2, 4), .75, faces=['front'])
# ---- hump: lumpy shading, veins, pus-tipped tumours ----
for f in SIDES + ('top',):
    paint('body', 1, f, lambda i, j, w, h: shade(SKIN, .78) if (i * 5 + j * 3) % 7 == 0 else ((120, 70, 90) if (i + 2 * j) % 9 == 4 else None))
for c in (2, 3, 4):
    for f in SIDES + ('top',): paint('body', c, f, lambda i, j, w, h: PUS if (i + j) % 3 == 0 else None)
for f in ('front', 'back'):
    x, y, w, h = t.face('rag', 0, f)
    for j in range(h):
        for i in range(w):
            if j >= h - 1 - (i * 2 % 3) or (j == 1 and i == 2): t.px[x + i, y + j] = (0, 0, 0, 0)

# ---- head: bald pate with a few long hairs, cataract eye + red pinhole eye, frog mouth ----
def face(i, j, w, h):
    if j == 1 and i == 4: return (150, 20, 20)          # small red eye (big milky eye is its own cube)
    if j in (0, 2) and i == 4: return shade(SKIN, .55)
    if j == 3 and i in (2, 3): return shade(SKIN, .45)   # slit nostrils
    if j == 4: return DARK if i % 2 else TOOTH          # upper needle teeth
paint('head', 0, 'front', face)
paint('head', 0, 'front', lambda i, j, w, h: (255, 60, 40, 255) if j == 1 and i == 4 else None, e)
paint('head', 1, 'front', lambda i, j, w, h: (205, 220, 215) if (i, j) != (1, 1) else (150, 175, 175))
paint('head', 1, 'front', lambda i, j, w, h: (190, 255, 245, 255), e)
for f in ('left', 'right', 'top'): paint('head', 1, f, lambda i, j, w, h: (120, 70, 80))
for f in ('top', 'back', 'left', 'right'):
    paint('head', 0, f, lambda i, j, w, h: (35, 30, 26) if (i * 3 + j) % 7 == 0 else None)
paint('head', 0, 'left', lambda i, j, w, h: DARK if j == 4 and i < 3 else None)
paint('head', 0, 'right', lambda i, j, w, h: DARK if j == 4 and i >= 2 else None)
paint('jaw', 0, 'front', lambda i, j, w, h: TOOTH if i % 2 == 0 else (120, 30, 40))
paint('jaw', 0, 'top', lambda i, j, w, h: TOOTH if j == 0 and i % 2 == 0 else n((115, 35, 45)))
t.blot('jaw', BLOOD, 6, (1, 2), .8, faces=['front', 'bottom'])
for p in ('right_ear', 'left_ear'):
    paint(p, 0, 'front', lambda i, j, w, h: (190, 110, 110) if j == 1 and i < 2 else None)
    paint(p, 0, 'back', lambda i, j, w, h: (190, 110, 110) if j == 1 and i > 0 else None)
    x, y, w, h = t.face(p, 0, 'front'); t.px[x + w - 1, y] = (0, 0, 0, 0)
    x, y, w, h = t.face(p, 0, 'back'); t.px[x, y] = (0, 0, 0, 0)

# ---- limbs: knuckle callouses, black nails, blood-caked hands, knee wraps ----
for p in ('right_fore', 'left_fore'):
    for f in SIDES: paint(p, 0, f, lambda i, j, w, h: shade(SKIN, .8) if j >= h - 1 else None)
    t.blot(p, BLOOD, 5, (1, 3), .7)
for p in ('right_hand', 'left_hand'): t.blot(p, BLOOD, 8, (1, 2), .7)
for p in ('right_shin', 'left_shin'):
    for f in SIDES: paint(p, 0, f, lambda i, j, w, h: n((150, 140, 120)) if j == 0 or (j == 1 and (i + j) % 2) else None)
for p in ('right_foot', 'left_foot'):
    paint(p, 0, 'front', lambda i, j, w, h: (30, 22, 20))
    paint(p, 0, 'top', lambda i, j, w, h: (30, 22, 20) if j == 0 else None)

t.save('jockey'); e.save('jockey_eyes')
if __name__ == '__main__': preview(m, 'jockey')
