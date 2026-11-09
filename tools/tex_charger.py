"""Charger: hunched brute with one club-like right arm sheathed in calcified bone plates (skin split over raw muscle),
a withered left arm tucked to the chest, bone spurs along the spine. Frayed work trousers, one rope suspender.
Writes charger.json + textures."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from packuv import P, C, build

parts = [
    P('pelvis', None, [0, 11, 0], [0, 0, 0], [C([-4, -2, -2.5], [8, 4, 5])]),
    P('body', None, [0, 10, 0], [0, 0, 0], [C([-4.5, -6, -3], [9, 6, 6])]),
    P('chest', 'body', [0, -5, 0], [28, 0, 0], [C([-6, -9, -4], [12, 9, 8])]),
    P('hump', 'chest', [-2, -9, 0], [0, 0, 0], [C([-6, -3, -3.5], [8, 4, 7])]),
    P('spurs', 'chest', [0, -8, 4], [0, 0, 0], [C([-3, -1, -1], [1, 3, 2]), C([0, 1, -1], [1, 3, 2]), C([2.5, 3, -1], [1, 2, 2])]),
    P('head', 'chest', [1.5, -7, -4], [-28, 0, 0], [C([-2.5, -3, -5], [5, 5, 5]), C([-3, -3.5, -5.5], [6, 2, 2])]),
    P('jaw', 'head', [0, 1.5, -1], [22, 0, 0], [C([-2, 0, -4], [4, 2, 4])]),
    P('right_arm', 'chest', [-7, -7, -1], [-40, 0, 5], [C([-6, -3, -4], [7, 5, 8]), C([-5, 1, -3], [6, 8, 6])]),
    P('right_forearm', 'right_arm', [-2, 9, 0], [-14, 0, 0], [C([-4.5, 0, -4], [8, 9, 8]), C([-5.5, 1, -2], [1, 6, 4]),
                                                              C([-3, -2, 2.5], [2, 3, 2])]),
    P('right_fist', 'right_forearm', [-0.5, 9, 0], [0, 0, 0], [C([-4.5, 0, -4.5], [9, 5, 9]), C([-4, 4, -5], [8, 1, 2])]),
    P('left_arm', 'chest', [5.5, -6.5, -1.5], [-55, 0, -12], [C([-1, -1, -1], [2, 5, 2])]),
    P('left_forearm', 'left_arm', [0, 4.5, 0], [-70, 0, 0], [C([-1, 0, -1], [2, 4, 2])]),
    P('left_hand', 'left_forearm', [0, 4, 0], [0, 0, 0], [C([-1, 0, -1], [2, 2, 1])]),
    P('right_leg', None, [-2.6, 12, 0.5], [0, 0, 0], [C([-2, -1, -2], [4, 7, 4])]),
    P('right_shin', 'right_leg', [0, 6, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 6, 3]), C([-2, 5, -3.5], [4, 1, 5])]),
    P('left_leg', None, [2.6, 12, 0.5], [0, 0, 0], [C([-2, -1, -2], [4, 7, 4])]),
    P('left_shin', 'left_leg', [0, 6, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 6, 3]), C([-2, 5, -3.5], [4, 1, 5])]),
]
H = build('charger', parts, 128)
print('tex 128x%d' % H)

from texlib import *
m = Model('charger')
t = Tex(m, seed=21)
SKIN, RAW, BONE, PANTS = (168, 150, 140), (124, 44, 50), (196, 182, 146), (78, 70, 52)
glow = []

def sides(f): return f not in ('top', 'bottom')
def each(part, fn, cubes=None): t.overlay(part, fn, cubes=cubes)
def vein(part, col, n):
    def f(fc, x, y, w, h):
        if not sides(fc): return
        for _ in range(n):
            cx = x + t.rng.randrange(w)
            for j in range(t.rng.randint(min(2, h), h)):
                cx = min(x + w - 1, max(x, cx + t.rng.choice((-1, 0, 0, 1))))
                t.put(cx, y + j, mix(t.px[cx, y + j], col, .55))
    each(part, f)

for p in ('body', 'chest', 'head', 'jaw', 'left_arm', 'left_forearm', 'right_shin', 'left_shin'):
    t.fill(p, SKIN, 9)
for p in ('body', 'chest', 'head'):
    t.blot(p, (120, 80, 110), 6, (2, 5), .45)      # purple bruising
    t.blot(p, (110, 24, 30), 2, (2, 4), .75)
    vein(p, (96, 70, 110), 1)

# chest: overstretched skin tears + one rope suspender over the left shoulder
def chest(f, x, y, w, h):
    if f in ('front', 'back'):
        sx = x + w - 4 if f == 'front' else x + 3
        for j in range(h): t.put(sx + (j // 4) * (-1 if f == 'front' else 1), y + j, (104, 84, 54))
    if f == 'front':
        for j in range(2, 7):  # stretch-mark tears
            t.put(x + 2 + j // 2, y + j, RAW); t.put(x + 3 + j // 2, y + j, shade(RAW, .7))
each('chest', chest)
def belly(f, x, y, w, h):
    if f == 'front':
        t.put(x + w // 2, y + 3, (60, 30, 30))
        for i in range(w): t.put(x + i, y + h - 1, (60, 50, 34))
each('body', belly)

# hump + big arm: split skin over raw striated muscle, calcified plates
for p in ('hump', 'right_arm', 'right_forearm'): t.fill(p, RAW, 10)
def fibres(f, x, y, w, h):
    for i in range(w):
        for j in range(h):
            if (i + (j // 3)) % 3 == 0: t.put(x + i, y + j, shade(RAW, .7))
            elif (i * 7 + j * 3) % 11 == 0: t.put(x + i, y + j, shade(RAW, 1.25))
for p in ('hump', 'right_arm', 'right_forearm'): each(p, fibres)
def skin_flaps(f, x, y, w, h):   # patches of stretched skin still clinging
    for _ in range(max(1, w * h // 7)):
        cx, cy = x + t.rng.randrange(w), y + t.rng.randrange(h)
        for dx in range(t.rng.randint(1, 3)):
            for dy in range(t.rng.randint(1, 3)):
                if x <= cx + dx < x + w and y <= cy + dy < y + h: t.put(cx + dx, cy + dy, shade(SKIN, .9 + t.rng.random() * .15))
for p in ('hump', 'right_arm'): each(p, skin_flaps)
t.fill('right_fist', BONE, 9)
t.fill('right_forearm', BONE, 8, cubes=[1, 2])
t.fill('spurs', BONE, 8)
def plates(f, x, y, w, h):       # cracked plate seams + dried blood at the knuckles
    for i in range(w):
        for j in range(h):
            if (i % 3 == 0 and j % 2 == 0) or j == 0: t.put(x + i, y + j, shade(BONE, .7))
    if f in ('front', 'bottom'): t.blot('right_fist', (100, 16, 20), 3, (2, 4), .8, faces=(f,))
each('right_fist', plates)
def knuckle_gaps(f, x, y, w, h):
    if f == 'front':
        for i in range(1, w, 2): t.put(x + i, y, (40, 30, 30))
each('right_fist', knuckle_gaps, cubes=[1])

# withered little arm: dark, shrunken, veiny
for p in ('left_arm', 'left_forearm', 'left_hand'): t.fill(p, (120, 104, 108), 12)
vein('left_arm', (60, 40, 70), 2); vein('left_forearm', (60, 40, 70), 2)
each('left_hand', lambda f, x, y, w, h: [t.put(x + i, y + h - 1, (30, 24, 24)) for i in range(w)] if sides(f) else None)

# head: tiny deep-set glowing eyes under a heavy brow, locked-open maw
def face(f, x, y, w, h):
    if f == 'front':
        t.rect(x, y, w, 1, shade(SKIN, .6))
        for ex in (x + 1, x + 3):
            t.put(ex, y + 1, (30, 10, 10)); t.put(ex, y + 2, (255, 200, 60)); glow.append((ex, y + 2, (255, 200, 60)))
        t.put(x + 2, y + 3, (80, 40, 40))
        for i in range(w): t.put(x + i, y + h - 1, (220, 206, 150) if i % 2 == 0 else (50, 10, 14))
    if f == 'top':
        for _ in range(6): t.put(x + t.rng.randrange(w), y + t.rng.randrange(h), (50, 40, 36))
each('head', face, cubes=[0])
t.fill('head', shade(SKIN, .85), 10, cubes=[1])
each('jaw', lambda f, x, y, w, h: ([t.rect(x, y, w, h, (80, 14, 20))] + [t.put(x + i, y + h - 1 - (i % 2), (225, 210, 150)) for i in range(w)])
     if f == 'top' else ([t.put(x + i, y, (225, 210, 150)) for i in range(0, w, 2)] if f == 'front' else None))

# trousers, frayed hems, patched knee, bare feet
for p in ('pelvis', 'right_leg', 'left_leg'): t.fill(p, PANTS, 9); t.blot(p, (50, 40, 30), 6, (2, 4), .6); t.blot(p, (100, 20, 20), 2, (2, 4), .6)
each('pelvis', lambda f, x, y, w, h: [t.put(x + i, y, (40, 34, 28)) for i in range(w)] if sides(f) else None)
def hem(f, x, y, w, h):
    if sides(f):
        for i in range(w):
            if t.rng.random() < .55: t.px[x + i, y + h - 1] = (0, 0, 0, 0)
    if f == 'front': t.rect(x + 1, y + 2, 2, 2, (110, 96, 70)); t.put(x + 1, y + 2, (60, 50, 40))
each('right_leg', hem); each('left_leg', hem)
for p in ('right_shin', 'left_shin'):
    t.fill(p, shade(SKIN, .85), 9)
    each(p, lambda f, x, y, w, h: [t.put(x + i, y + h - 1, (40, 30, 30)) for i in range(w)] if f == 'front' else None, cubes=[1])

t.save('charger')
from PIL import Image
e = Image.new('RGBA', t.img.size, (0, 0, 0, 0))
for x, y, c in glow: e.putpixel((x, y), c + (255,))
e.save(os.path.join(TEXDIR, 'charger_eyes.png'))
preview(m)
