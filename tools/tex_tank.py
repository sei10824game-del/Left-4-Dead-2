"""Tank: hunched colossus. Builds models/entity/tank.json (auto-packed UVs) + paints tank.png / tank_eyes.png.
Look: ashen-violet hide stretched over tumour-like muscle, calcified bone plates and spurs bursting from the
right shoulder, raw red fissures, stitched surgical scar, shredded charcoal trousers held by a rope."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *
from specbuild import Spec, C

def build():
    S = Spec(256, 128)
    S.part('body', None, [0, 8, 1], [18, 0, 0], [
        C([-7, -5, -5], [14, 6, 10]),            # 0 pelvis
        C([-8, -12, -6], [16, 8, 11]),           # 1 gut
        C([-11, -22, -8], [22, 11, 13]),         # 2 chest
        C([-8, -26, -4], [16, 5, 11]),           # 3 back hump
        C([3, -20, -10], [7, 6, 2]),             # 4 pec tumour
        C([-13, -18, -5], [2, 7, 8])])           # 5 split flank (exposed ribs)
    S.part('spine1', 'body', [0, -25, 5], [-35, 0, 0], [C([-1, -5, -1], [2, 5, 2])])
    S.part('spine2', 'body', [-3, -20, 7], [-55, 0, -12], [C([-1, -4, -1], [2, 4, 2])])
    S.part('spine3', 'body', [3, -15, 6], [-60, 0, 14], [C([-1, -3, -1], [2, 3, 2])])
    S.part('head', 'body', [0, -19, -8], [-18, 0, 0], [
        C([-3, -6, -5], [6, 6, 6]),              # 0 skull
        C([-3, -6, -6], [6, 2, 1])])             # 1 brow ridge
    S.part('jaw', 'head', [0, 0, -1], cubes=[C([-3, -1, -4], [6, 2, 5])])
    S.part('right_arm', 'body', [-11, -19, 0], [-18, 0, 0], [
        C([-11, -7, -7], [12, 12, 13]),          # 0 shoulder mass
        C([-8, 3, -5], [9, 12, 9])])             # 1 upper arm
    S.part('spur_r1', 'right_arm', [-6, -6, -2], [10, 0, -22], [C([-1, -6, -1], [2, 6, 2])])
    S.part('spur_r2', 'right_arm', [-10, -4, 2], [-15, 0, -55], [C([-1, -5, -1], [2, 5, 2])])
    S.part('spur_r3', 'right_arm', [-3, -6, 4], [-45, 0, -8], [C([-1, -4, -1], [2, 4, 2])])
    S.part('right_forearm', 'right_arm', [-3.5, 15, 0], cubes=[
        C([-6, 0, -6], [12, 13, 12]),            # 0 forearm
        C([-6.5, 12, -7], [13, 8, 13]),          # 1 fist
        C([-5, 13, -9], [3, 3, 2]),              # 2..4 knuckle growths
        C([-1, 14, -9], [3, 3, 2]),
        C([3, 13, -9], [2, 3, 2])])
    S.part('spur_elbow', 'right_forearm', [-3, 2, 5], [-65, 0, -10], [C([-1, -5, -1], [2, 5, 2])])
    S.part('left_arm', 'body', [10, -18, 0], [-18, 0, 0], [
        C([-1, -6, -6], [10, 10, 11]),           # 0 shoulder
        C([0, 3, -4], [7, 11, 7]),               # 1 upper arm
        C([4, -8, -2], [5, 3, 5]),               # 2 tumour
        C([7, -5, 2], [3, 4, 3])])               # 3 tumour
    S.part('left_forearm', 'left_arm', [3.5, 14, 0], cubes=[
        C([-4.5, 0, -4.5], [9, 11, 9]),
        C([-5, 10, -5.5], [10, 7, 10])])
    for side, x in (('right', -5), ('left', 5)):
        S.part(side + '_leg', None, [x, 8, 1], cubes=[
            C([-4, -1, -4], [8, 9, 8]),          # thigh (trousers)
            C([-3.5, 8, -3.5], [7, 6, 7]),       # shin
            C([-4, 14, -6], [8, 2, 9])])         # foot
    S.part('rock', 'body', [0, -2, -16], cubes=[C([-5, -5, -5], [10, 9, 10]), C([-3, -8, -3], [7, 3, 6])])
    S.write('tank')

SKIN = (124, 114, 112); BONE = (222, 210, 178); MEAT = (150, 36, 40); PANTS = (52, 48, 46); VEIN = (70, 42, 82)

def skin_noise(t, part, base, cubes=None):
    t.fill(part, base, 7, 0.18, cubes=cubes)
    t.blot(part, (168, 150, 108), 1.2, (3, 7), 0.3)   # jaundiced patches
    t.blot(part, (98, 74, 92), 1.5, (2, 5), 0.35)       # bruising

def veins(t, part, n=2, faces=('front', 'right', 'left', 'back'), cubes=None):
    def f(fc, x, y, w, h):
        if fc not in faces: return
        for _ in range(max(1, n * w * h // 120)):
            cx, cy = x + t.rng.randrange(w), y + t.rng.randrange(h)
            for _ in range(t.rng.randint(4, 9)):
                if x <= cx < x + w and y <= cy < y + h: t.put(cx, cy, mix(t.px[cx, cy], VEIN, 0.55))
                cx += t.rng.choice((-1, 0, 1)); cy += t.rng.choice((0, 1, 1))
    t.overlay(part, f, cubes=cubes)

def fissure(t, x, y, length, vertical=True):
    """raw split in the hide: dark core, red meat, pale torn edges."""
    for k in range(length):
        jx = x + (t.rng.choice((-1, 0, 0, 1)) if vertical else k); jy = y + (k if vertical else t.rng.choice((-1, 0, 0, 1)))
        x, y = (jx, y) if vertical else (x, jy)
        t.put(jx, jy, (60, 8, 12))
        for dx, dy in ((1, 0), (-1, 0)) if vertical else ((0, 1), (0, -1)):
            t.put(jx + dx, jy + dy, shade(MEAT, 0.9 + t.rng.random() * 0.3))

def bone(t, part, cubes=None):
    t.fill(part, BONE, 8, 0.25, cubes=cubes)
    t.blot(part, (150, 130, 95), 4, (1, 3), 0.6, faces=None)
    def tip(f, x, y, w, h):     # blood-stained base of each spur
        if f in ('top', 'bottom'): return
        for i in range(w): t.put(x + i, y + h - 1, shade(MEAT, 0.8))
    t.overlay(part, tip, cubes=cubes)

def paint():
    m = Model('tank'); t = Tex(m, seed=7)
    for p in ('body', 'right_arm', 'left_arm', 'right_forearm', 'left_forearm', 'head', 'jaw'): skin_noise(t, p, SKIN)
    for p in ('body', 'right_arm', 'left_arm', 'right_forearm', 'left_forearm'): veins(t, p, 3)
    # muscle striation on arms
    for p in ('right_arm', 'left_arm', 'right_forearm', 'left_forearm'):
        def stri(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for i in range(0, w, 3):
                for j in range(h):
                    if t.rng.random() < .55: t.put(x + i, y + j, shade(t.px[x + i, y + j], .86))
        t.overlay(p, stri, cubes=[0, 1])
    # calcified plates crusting the right shoulder top/outer face
    def plates(f, x, y, w, h):
        if f not in ('top', 'right', 'back', 'front'): return
        for _ in range(w * h // 9):
            cx, cy = x + t.rng.randrange(w), y + t.rng.randrange(h if f == 'top' else max(1, h // 2))
            s = t.rng.randint(1, 3)
            for a in range(s):
                for b in range(s): t.put(min(cx + a, x + w - 1), min(cy + b, y + h - 1), shade(BONE, .8 + t.rng.random() * .25))
    t.overlay('right_arm', plates, cubes=[0])
    for p in ('spine1', 'spine2', 'spine3', 'spur_r1', 'spur_r2', 'spur_r3', 'spur_elbow'): bone(t, p)
    # chest: diagonal stitched scar + fissures
    x, y, w, h = t.face('body', 2, 'front')
    for k in range(w - 4):
        sx, sy = x + 2 + k, y + 1 + k * (h - 2) // (w - 4)
        t.put(sx, sy, (88, 30, 40)); t.put(sx, sy + 1, (120, 60, 66))
        if k % 2 == 0: t.put(sx, sy - 1, (30, 26, 24)); t.put(sx, sy + 2, (30, 26, 24))
    fissure(t, x + 4, y + 3, h - 4)
    x, y, w, h = t.face('body', 1, 'front'); fissure(t, x + w - 4, y + 1, h - 2)
    for i in range(1, w - 1, 3): t.put(x + i, y + h // 2, shade(SKIN, .8))   # stretch marks
    # flank: exposed ribs
    for fc in ('right', 'front', 'back', 'left', 'top', 'bottom'):
        x, y, w, h = t.face('body', 5, fc)
        for j in range(h):
            for i in range(w): t.put(x + i, y + j, BONE if j % 2 == 0 else shade(MEAT, .7 + t.rng.random() * .3))
    # hump vertebra knobs
    x, y, w, h = t.face('body', 3, 'back')
    for j in range(0, h, 2): t.put(x + w // 2, y + j, BONE); t.put(x + w // 2 - 1, y + j, shade(BONE, .8))
    # pec tumour: glossy swollen, pink-purple
    t.fill('body', (150, 96, 120), 10, .3, cubes=[4]); veins(t, 'body', 6, cubes=[4])
    # trousers + rope belt
    t.fill('body', PANTS, 9, .2, cubes=[0])
    x, y, w, h = t.face('body', 0, 'front')
    for fc in ('front', 'right', 'back', 'left'):
        rx, ry, rw, rh = t.face('body', 0, fc)
        for i in range(rw): t.put(rx + i, ry, (150, 120, 70) if i % 2 else (110, 85, 50))
    for leg in ('right_leg', 'left_leg'):
        t.fill(leg, PANTS, 9, .2, cubes=[0]); skin_noise(t, leg, SKIN, cubes=[1, 2]); veins(t, leg, 2, cubes=[1])
        def shred(f, x, y, w, h):   # ragged cuff
            if f in ('top', 'bottom'): return
            for i in range(w):
                for j in range(h - 1 - t.rng.randint(0, 3), h): t.put(x + i, y + j, shade(SKIN, .8 + t.rng.random() * .2))
        t.overlay(leg, shred, cubes=[0])
        x, y, w, h = t.face(leg, 2, 'front')
        for i in range(0, w, 2): t.put(x + i, y + h - 1, (40, 34, 30))      # black toenails
        t.blot(leg, (70, 55, 40), 6, (2, 4), .6)
    # fists: callused dark knuckles, blood
    for fa, ci in (('right_forearm', 1), ('left_forearm', 1)):
        for fc in ('front', 'bottom'):
            x, y, w, h = t.face(fa, ci, fc)
            for j in range(h):
                for i in range(w):
                    if fc == 'bottom' or j < 3: t.put(x + i, y + j, shade((92, 72, 70), .8 + t.rng.random() * .3))
        t.blot(fa, (100, 12, 14), 2, (2, 5), .85)
    t.fill('right_forearm', BONE, 10, .3, cubes=[2, 3, 4])
    for p in ('body', 'right_arm', 'left_arm', 'head'): t.blot(p, (100, 12, 14), .6, (2, 4), .8)
    t.fill('left_arm', (150, 96, 120), 10, .3, cubes=[2, 3]); veins(t, 'left_arm', 6, cubes=[2, 3])
    # head: bald, cracked, sunken sockets, tiny glowing pupils; jaw full of teeth
    x, y, w, h = t.face('head', 0, 'front')
    t.rect(x, y + 2, w, 2, shade(SKIN, .55))
    t.rect(x + 1, y + 2, 1, 1, (20, 10, 10)); t.rect(x + 4, y + 2, 1, 1, (20, 10, 10))
    t.rect(x + 2, y + 4, 2, 1, (60, 30, 36))       # nose slit
    t.rect(x, y + 5, w, 1, (45, 10, 12))
    t.fill('head', shade(SKIN, .75), 6, 0, cubes=[1])
    x, y, w, h = t.face('jaw', 0, 'front')
    for i in range(w): t.put(x + i, y, (215, 200, 150) if i % 2 == 0 else (60, 12, 14))
    x, y, w, h = t.face('jaw', 0, 'top')
    t.rect(x, y, w, h, (80, 14, 18))
    for i in range(0, w, 2): t.put(x + i, y + h - 1, (215, 200, 150))
    # rock: concrete/stone chunk
    t.fill('rock', (118, 116, 112), 14, .3)
    t.blot('rock', (85, 82, 78), 6, (2, 5), .7); t.blot('rock', (95, 78, 55), 4, (2, 5), .6)
    t.save('tank')
    # glow layer: pupils only
    eyes = Image.new('RGBA', tuple(m.tex), (0, 0, 0, 0))
    x, y, w, h = t.face('head', 0, 'front')
    eyes.putpixel((x + 1, y + 2), (255, 190, 60, 255)); eyes.putpixel((x + 4, y + 2), (255, 190, 60, 255))
    eyes.save(os.path.join(TEXDIR, 'tank_eyes.png'))
    preview(m, 'tank', views=(0, 90, 180, 225), scale=5)
    preview(m, 'tank', 'tank_throw', views=(0, 90), scale=5,
            pose={'right_arm': (-2.9, 0, 0.3), 'left_arm': (-2.9, 0, -0.3)})

if __name__ == '__main__':
    build(); paint()
