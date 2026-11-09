"""Smoker: gaunt, hunched, over-long limbs, a swollen throat sac under a split lower jaw, smouldering spore
tumours on the back. Rags of a long charcoal raincoat with a torn hood collar. Writes smoker.json + textures."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from packuv import P, C, build

parts = [
    P('pelvis', None, [0, 11, 0], [0, 0, 0], [C([-3.5, -1, -2], [7, 3, 4])]),
    P('body', None, [0, 11, 0], [0, 0, 0], [C([-3, -6, -1.5], [6, 6, 3])]),
    P('chest', 'body', [0, -6, 0], [26, 0, 0], [C([-4.5, -8, -2.5], [9, 8, 5]), C([-4.5, -8, -2.5], [9, 8, 5], 0.35)]),
    P('collar', 'chest', [0, -8, 0.5], [-12, 0, 0], [C([-4, -3, -1], [8, 3, 3])]),
    P('growth', 'chest', [0, -6, 2.5], [0, 0, 0], [C([0, -2, -0.5], [3, 3, 3]), C([-3.5, 1, -0.5], [2, 2, 2]),
                                               C([1.5, -4, 0.5], [1, 2, 1]), C([4, -3, -3.5], [2, 2, 3])]),
    P('neck', 'chest', [0, -8, -0.5], [-8, 0, 0], [C([-1.5, -3, -1.5], [3, 3, 3])]),
    P('head', 'neck', [0, -3, 0], [-16, 0, 0], [C([-2.5, -7, -3.5], [5, 6, 6]), C([-2, -8, -1.5], [4, 2, 4])]),
    P('jaw', 'head', [0, -1, 1.5], [14, 0, 0], [C([-2, 0, -5], [4, 2, 5])]),
    P('tongue', 'jaw', [0, 0.3, -4.5], [0, 0, 0], [C([-0.5, 0, -1], [1, 1, 2])]),
    P('tongue_tip', 'tongue', [0, 0.5, -1], [0, 0, 0], [C([-0.5, 0, -0.5], [1, 4, 1])]),
    P('sac', 'neck', [0, -1.5, -1.5], [0, 0, 0], [C([-2.5, -0.5, -3.5], [5, 4, 4]), C([-1.5, 3, -3], [3, 2, 3])]),
    P('right_arm', 'chest', [-5.5, -7, 0], [-36, 0, 6], [C([-1.5, -1, -1], [2, 9, 2]), C([-2, -1.5, -1.5], [3, 3, 3], 0.1)]),
    P('right_forearm', 'right_arm', [-0.5, 8, 0], [-20, 0, 0], [C([-1, 0, -1], [2, 8, 2])]),
    P('right_hand', 'right_forearm', [0, 8, 0], [0, 0, 0], [C([-1.5, 0, -1], [3, 2, 2]), C([-1.5, 2, -0.5], [3, 4, 1])]),
    P('left_arm', 'chest', [5.5, -7, 0], [-36, 0, -6], [C([-0.5, -1, -1], [2, 9, 2]), C([-1, -1.5, -1.5], [3, 3, 3], 0.1)]),
    P('left_forearm', 'left_arm', [0.5, 8, 0], [-20, 0, 0], [C([-1, 0, -1], [2, 8, 2])]),
    P('left_hand', 'left_forearm', [0, 8, 0], [0, 0, 0], [C([-1.5, 0, -1], [3, 2, 2]), C([-1.5, 2, -0.5], [3, 4, 1])]),
    P('coat_back', 'body', [0, 0, 1.7], [8, 0, 0], [C([-4, -1, 0], [8, 10, 1])]),
    P('coat_right', 'body', [-3, 0, -1.6], [-4, 0, 6], [C([-1.5, -1, -0.5], [3, 9, 1])]),
    P('coat_left', 'body', [3, 0, -1.6], [-4, 0, -6], [C([-1.5, -1, -0.5], [3, 9, 1])]),
    P('right_leg', None, [-2, 11, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 7, 3])]),
    P('right_shin', 'right_leg', [0, 7, 0], [0, 0, 0], [C([-1, 0, -1], [2, 6, 2]), C([-1.5, 5, -3], [3, 1, 4])]),
    P('left_leg', None, [2, 11, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 7, 3])]),
    P('left_shin', 'left_leg', [0, 7, 0], [0, 0, 0], [C([-1, 0, -1], [2, 6, 2]), C([-1.5, 5, -3], [3, 1, 4])]),
]
H = build('smoker', parts, 128)
print('tex 128x%d' % H)

from texlib import *
m = Model('smoker')
t = Tex(m, seed=7)
SKIN, COAT, PANTS = (150, 144, 162), (56, 62, 56), (44, 40, 48)
SAC, MEAT, RUST, EMBER = (176, 172, 112), (168, 48, 62), (118, 70, 46), (255, 150, 50)
CLEAR = (0, 0, 0, 0)
glow = []  # (x, y, colour) pixels that also go to the emissive layer

def sides(f): return f not in ('top', 'bottom')
def each(part, fn, faces=None, cubes=None):
    t.overlay(part, lambda f, x, y, w, h: fn(f, x, y, w, h) if (faces is None or f in faces) else None, cubes=cubes)
def clear(x, y): t.px[x, y] = CLEAR
def vein(part, col, n, faces=None):
    """wandering 1px veins running downward."""
    def f(fc, x, y, w, h):
        if not sides(fc): return
        for _ in range(n):
            cx = x + t.rng.randrange(w)
            for j in range(t.rng.randint(min(2, h), h)):
                cx = min(x + w - 1, max(x, cx + t.rng.choice((-1, 0, 0, 1))))
                t.put(cx, y + j, mix(t.px[cx, y + j], col, .55))
    each(part, f, faces)

# ---- skin everywhere first ----
for p in ('body', 'neck', 'head', 'jaw', 'right_arm', 'left_arm', 'right_forearm', 'left_forearm', 'right_shin', 'left_shin'):
    t.fill(p, SKIN, 9)
for p in ('right_hand', 'left_hand'): t.fill(p, shade(SKIN, .75), 10)
t.fill('chest', SKIN, 9, cubes=[0])
for p in ('body', 'chest', 'neck', 'head', 'right_forearm', 'left_forearm', 'right_shin', 'left_shin'):
    t.blot(p, (100, 82, 122), 5, (2, 5), .45)     # violet bruising
    t.blot(p, (120, 34, 44), 2, (1, 3), .7)        # weeping lesions
    vein(p, (84, 70, 120), 1)

# gaunt ribs + sternum on the bare torso
def ribs(f, x, y, w, h):
    if f not in ('front', 'right', 'left'): return
    for j in range(1, h, 2):
        for i in range(w): t.put(x + i, y + j, shade(t.px[x + i, y + j], .72))
    if f == 'front':
        for j in range(h): t.put(x + w // 2, y + j, shade(SKIN, .8))
each('chest', ribs, cubes=[0])
def belly(f, x, y, w, h):
    if f == 'front':
        for j in range(0, 3, 2):
            for i in range(w): t.put(x + i, y + j, shade(t.px[x + i, y + j], .75))
        for j in range(3, h): t.put(x + w // 2, y + j, (70, 40, 50))     # ragged surgical seam
        for j in range(3, h, 2): t.put(x + w // 2 - 1, y + j, (40, 30, 30)); t.put(x + w // 2 + 1, y + j, (40, 30, 30))
each('body', belly)

# ---- raincoat rags ----
def coat(part, cubes=None, bottom_rag=True, hole=0):
    t.fill(part, COAT, 8, cubes=cubes)
    t.blot(part, (34, 38, 32), 6, (2, 5), .6)
    t.blot(part, (90, 18, 20), 2, (2, 4), .7)
    def rag(f, x, y, w, h):
        if f == 'bottom': [clear(x + i, y + j) for i in range(w) for j in range(h)]
        if not sides(f) or not bottom_rag: return
        for i in range(w):
            for j in range(t.rng.choice((0, 1, 1, 2, 3))): clear(x + i, y + h - 1 - j)
        for k in range(hole):
            hx, hy = x + t.rng.randrange(w), y + t.rng.randrange(h - 2)
            clear(hx, hy); clear(min(x + w - 1, hx + 1), hy)
        for i in range(w): t.put(x + i, y, shade(t.px[x + i, y], 1.2)) if t.px[x + i, y][3] else None
    each(part, rag, cubes=cubes)
coat('chest', [1], bottom_rag=True, hole=1)
def coat_open(f, x, y, w, h):   # open front shows the ribs; lapels
    if f != 'front': return
    for j in range(h):
        span = 2 + (j * 2) // h
        for i in range(w // 2 - span, w // 2 + span + 1): clear(x + i, y + j)
        t.put(x + w // 2 - span - 1, y + j, shade(COAT, 1.3)); t.put(x + w // 2 + span + 1, y + j, shade(COAT, 1.3))
each('chest', coat_open, cubes=[1])
coat('collar', bottom_rag=False)
for p in ('coat_back', 'coat_right', 'coat_left'): coat(p, hole=2)
for p in ('right_arm', 'left_arm'): coat(p, [1], bottom_rag=True)
def sleeve(f, x, y, w, h):     # torn sleeve to the elbow
    if not sides(f): return
    for i in range(w):
        for j in range(h - 3 - t.rng.randint(0, 2)): t.put(x + i, y + j, shade(COAT, .95 + t.rng.random() * .15))
for p in ('right_arm', 'left_arm'): each(p, sleeve, cubes=[0])

# ---- trousers / feet ----
for p in ('pelvis', 'right_leg', 'left_leg'): t.fill(p, PANTS, 8); t.blot(p, (70, 55, 40), 5, (2, 4), .5)
each('pelvis', lambda f, x, y, w, h: [t.put(x + i, y, (96, 80, 52)) for i in range(w)] if sides(f) else None)  # rope belt
def frayed(f, x, y, w, h):
    if sides(f):
        for i in range(w):
            if t.rng.random() < .5: t.put(x + i, y + h - 1, SKIN)
each('right_leg', frayed); each('left_leg', frayed)
def foot(f, x, y, w, h):
    for i in range(w):
        for j in range(h): t.put(x + i, y + j, shade(SKIN, .62 + t.rng.random() * .1))
    if f == 'front':
        for i in range(0, w, 1): t.put(x + i, y + h - 1, (30, 26, 28))   # black toenails
for p in ('right_shin', 'left_shin'): each(p, foot, cubes=[1])

# ---- hands: long black talons ----
def talons(f, x, y, w, h):
    if not sides(f): return
    for i in range(w):
        for j in range(h - 2, h): t.put(x + i, y + j, (26, 22, 26) if (i % 2 == 0 or j == h - 2) else CLEAR)
    if f == 'front':
        for j in range(h - 2): t.put(x + 1, y + j, shade(SKIN, .55))   # finger gaps
for p in ('right_hand', 'left_hand'): each(p, talons, cubes=[1])

# ---- head ----
def face(f, x, y, w, h):
    if f == 'front':        # 5x6: sunken glowing eye (left), swollen-shut eye (right), slit nose, upper teeth
        t.rect(x, y + 1, 2, 2, (40, 30, 42)); t.put(x + 1, y + 2, EMBER); glow.append((x + 1, y + 2, EMBER))
        t.put(x, y + 2, (200, 110, 40))
        t.rect(x + 3, y + 1, 2, 2, (184, 128, 140)); t.rect(x + 3, y + 2, 2, 1, (80, 40, 50))
        t.put(x + 2, y + 3, (50, 34, 40)); t.put(x + 2, y + 4, (70, 40, 50))
        for i in range(w): t.put(x + i, y + h - 1, (214, 200, 140) if i % 2 == 0 else (60, 20, 26))
    if f == 'top':          # stitched scar over the crown
        for j in range(h): t.put(x + 2, y + j, (90, 40, 50))
        for j in range(0, h, 2): t.put(x + 1, y + j, (40, 30, 30)); t.put(x + 3, y + j, (40, 30, 30))
each('head', face, cubes=[0])
def wisps(f, x, y, w, h):
    for _ in range(w * h // 4): t.put(x + t.rng.randrange(w), y + t.rng.randrange(h), (40, 38, 44))
each('head', wisps, faces=('top', 'back', 'right', 'left'), cubes=[1])
def jaw(f, x, y, w, h):
    if f == 'top': t.rect(x, y, w, h, (70, 16, 24)); [t.put(x + i, y + h - 1, (210, 196, 130)) for i in range(0, w, 2)]
    if f == 'front': [t.put(x + i, y, (210, 196, 130)) for i in range(0, w, 2)]
each('jaw', jaw)
for p in ('tongue', 'tongue_tip'):
    t.fill(p, MEAT, 8)
    each(p, lambda f, x, y, w, h: [t.put(x + i, y + j, shade(MEAT, .7)) for i in range(w) for j in range(0, h, 2)] if sides(f) else None)

# ---- throat sac: taut, glossy, purple veined ----
t.fill('sac', SAC, 7, grad=.35)
vein('sac', (110, 60, 120), 3)
def gloss(f, x, y, w, h):
    if f == 'front': t.put(x + 1, y + 1, (226, 224, 180)); t.put(x + 2, y + 1, (210, 210, 160))
each('sac', gloss)

# ---- spore tumours that smoulder ----
t.fill('growth', RUST, 14)
def pores(f, x, y, w, h):
    for _ in range(max(1, w * h // 4)):
        px_, py_ = x + t.rng.randrange(w), y + t.rng.randrange(h)
        c = EMBER if t.rng.random() < .45 else (60, 30, 22)
        t.put(px_, py_, c)
        if c == EMBER: glow.append((px_, py_, (255, 120, 40)))
each('growth', pores)

t.save('smoker')
# emissive layer
from PIL import Image
e = Image.new('RGBA', t.img.size, CLEAR)
for x, y, c in glow: e.putpixel((x, y), c + (255,))
e.save(os.path.join(TEXDIR, 'smoker_eyes.png'))
# tongue strand texture (u: around, v: along) - ridged, wet
tg = Image.new('RGBA', (8, 16))
for y in range(16):
    for x in range(8):
        f = (0.7 if y % 4 == 0 else 1.0) * (1.15 if x in (3, 4) else 1.0) * (0.92 + t.rng.random() * .12)
        tg.putpixel((x, y), shade(MEAT, f) + (255,))
tg.save(os.path.join(TEXDIR, 'smoker_tongue.png'))
preview(m)
preview(m, name='smoker_pose', pose={'jaw': (0.7, 0, 0), 'right_arm': (-1.4, -0.4, 0), 'left_arm': (-1.4, 0.4, 0)})
