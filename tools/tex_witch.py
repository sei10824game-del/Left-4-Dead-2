"""Witch: emaciated, hunched, impossibly long fingers. Builds models/entity/witch.json + witch_0/witch_1/witch_eyes.png.
v0 'Patient': ash-blue skin, matted blue-black hair, blood-soaked mint hospital gown, bandaged wrists.
v1 'Bride': same body, yellowed ivory gown with lace + rotten hem, auburn hair under a torn lace veil."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *
from specbuild import Spec, C

def build():
    S = Spec(128, 64)
    S.part('body', None, [0, 10, 0], cubes=[
        C([-3, -5, -1.5], [6, 5, 3]),            # 0 waist
        C([-3.5, -11, -2], [7, 6, 4]),           # 1 ribcage
        C([-1, -11, 2], [2, 9, 1])])             # 2 spine ridge
    S.part('skirt', 'body', [0, 0, 0], cubes=[
        C([-4, -1, -3], [8, 9, 6]),
        C([-3, 8, -3], [2, 4, 1]), C([1, 8, -3], [2, 2, 1]), C([-2, 8, 2], [3, 5, 1]), C([3, 8, -1], [1, 3, 2])])
    S.part('head', 'body', [0, -11, -1], cubes=[C([-3.5, -7, -3.5], [7, 7, 7])])
    S.part('hair', 'head', [0, -7, 0], cubes=[C([-4, -1, -4], [8, 1, 8])])
    S.part('hair_back', 'head', [0, -6, 3.5], [8, 0, 0], [C([-4, 0, 0], [8, 16, 1])])
    S.part('hair_l', 'head', [3.5, -6, 0], [0, 0, -6], [C([0, 0, -4], [1, 12, 4]), C([0, 0, 0], [1, 15, 4])])
    S.part('hair_r', 'head', [-3.5, -6, 0], [0, 0, 6], [C([-1, 0, -4], [1, 13, 4]), C([-1, 0, 0], [1, 11, 4])])
    S.part('hair_front', 'head', [0, -6, -4], [-6, 0, 0], [C([-3, 0, -1], [1, 10, 1]), C([-1, 0, -1], [1, 4, 1]), C([2, 0, -1], [1, 12, 1])])
    S.part('veil', 'head', [0, -7, 0], cubes=[C([-4.5, -1.5, -4.5], [9, 1, 9])])
    S.part('veil_back', 'veil', [0, -1, 4.5], [10, 0, 0], [C([-5, 0, 0], [10, 20, 1])])
    for side, sx in (('right', -1), ('left', 1)):
        S.part(side + '_arm', 'body', [4.5 * sx, -10, 0], cubes=[C([-1.5, -1, -1.5], [3, 9, 3])])
        S.part(side + '_forearm', side + '_arm', [0, 8, 0], cubes=[
            C([-1.5, 0, -1.5], [3, 7, 3]),       # 0 forearm
            C([-2, 7, -2], [4, 2, 4]),           # 1 hand
            C([-2, 9, -2], [1, 6, 1]), C([-0.5, 9, -2], [1, 7, 1]), C([1, 9, -2], [1, 6, 1]),  # 2-4 claws
            C([1, 8, 1], [1, 4, 1])])            # 5 thumb claw
        S.part(side + '_leg', None, [2 * sx, 10, 0], cubes=[C([-1.5, 0, -1.5], [3, 7, 3])])
        S.part(side + '_shin', side + '_leg', [0, 7, 0], cubes=[C([-1.5, 0, -1.5], [3, 6, 3]), C([-1.5, 6, -3.5], [3, 1, 4])])
    S.write('witch')

SKIN = [(160, 168, 180), (172, 170, 166)]
HAIR = [(38, 34, 50), (82, 40, 28)]
GOWN = [(140, 172, 160), (226, 216, 190)]
CLAW = (226, 218, 196)

def make(v):
    m = Model('witch'); t = Tex(m, seed=40 + v)
    skin, hair, gown = SKIN[v], HAIR[v], GOWN[v]
    for p in ('body', 'head', 'right_arm', 'left_arm', 'right_forearm', 'left_forearm', 'right_leg', 'left_leg', 'right_shin', 'left_shin'):
        t.fill(p, skin, 7, .2)
        t.blot(p, (110, 110, 150), 1.4, (2, 5), .45)          # veins / bruises
    t.fill('body', gown, 8, .2, cubes=[0, 1])
    t.fill('skirt', gown, 9, .25)
    # neckline: collarbones show
    x, y, w, h = t.face('body', 1, 'front'); t.rect(x + 1, y, w - 2, 2, skin); t.put(x + 2, y + 1, shade(skin, .8)); t.put(x + 4, y + 1, shade(skin, .8))
    t.fill('body', shade(skin, .9), 8, 0, cubes=[2])                  # bare spine through torn back
    x, y, w, h = t.face('body', 2, 'back')
    for j in range(0, h, 2): t.put(x, y + j, (210, 205, 190)); t.put(x + 1, y + j, (200, 196, 182))
    if v == 0:   # hospital gown: tie strings, faded print, blood soaking from chest, bandaged wrists, ID band
        for c in (0, 1):
            x, y, w, h = t.face('body', c, 'front')
            for j in range(0, h, 3):
                for i in range((j // 3) % 2, w, 3): t.put(x + i, y + j, shade(gown, .82))
        for c in range(5):
            x, y, w, h = t.face('skirt', c, 'front')
            for j in range(0, h, 3):
                for i in range((j // 3) % 2, w, 3): t.put(x + i, y + j, shade(gown, .82))
        t.blot('body', (110, 14, 18), 3, (3, 6), .8); t.blot('skirt', (110, 14, 18), 2.5, (3, 6), .8)
        for fa in ('right_forearm', 'left_forearm'):
            def band(f, x, y, w, h):
                if f in ('top', 'bottom'): return
                for j in range(1, 5):
                    for i in range(w): t.put(x + i, y + j, shade((205, 198, 180), .85 + .15 * ((i + j) % 2)))
                t.put(x, y + 3, (150, 30, 30))
            t.overlay(fa, band, cubes=[0])
        x, y, w, h = t.face('left_forearm', 0, 'front'); t.rect(x, y + 6, w, 1, (230, 230, 235))
    else:        # bride: lace scallops, corset seams, dead flower, rotten muddy hem
        for c in (0, 1):
            x, y, w, h = t.face('body', c, 'front')
            for i in range(w): t.put(x + i, y + h - 1 if c == 1 else y, shade(gown, .78))
            t.rect(x + w // 2, y, 1, h, shade(gown, .85))
        def lace(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for j in range(h):
                for i in range(w):
                    if (i + 2 * j) % 4 == 0 and t.rng.random() < .7: t.put(x + i, y + j, shade(gown, 1.08))
            for j in range(max(0, h - 3), h):
                for i in range(w): t.put(x + i, y + j, mix(t.px[x + i, y + j], (70, 58, 40), .45 + .1 * (j - h + 3)))
        t.overlay('skirt', lace)
        x, y, w, h = t.face('skirt', 0, 'front'); t.rect(x + 1, y + 1, 2, 2, (90, 40, 60)); t.put(x + 2, y + 3, (50, 70, 40))
        t.blot('body', (100, 16, 20), 1.5, (2, 5), .75); t.blot('skirt', (100, 16, 20), 1.2, (3, 6), .7)
        t.blot('skirt', (90, 80, 60), 2, (2, 5), .5)
        # veil: torn lace with holes (cutout)
        for p in ('veil', 'veil_back'):
            t.fill(p, (232, 228, 218), 6, .1)
            def holes(f, x, y, w, h):
                for j in range(h):
                    for i in range(w):
                        r = t.rng.random()
                        if (i + j) % 3 == 0 and r < .55 or j > h * .6 and r < (j / h - .6) * 2.2: t.put(x + i, y + j, (0, 0, 0, 0))
            t.overlay(p, holes, faces=('front', 'back', 'left', 'right'))
        x, y, w, h = t.face('veil', 0, 'top')
        for i in range(1, w, 2): t.put(x + i, y + 1, (200, 190, 120))       # tarnished tiara
    # hair: matted strands, darker roots, clumped with blood
    for p in ('hair', 'hair_back', 'hair_l', 'hair_r', 'hair_front'):
        t.fill(p, hair, 12, .3)
        def strands(f, x, y, w, h):
            for i in range(w):
                s = .7 + t.rng.random() * .5
                for j in range(h): t.put(x + i, y + j, shade(t.px[x + i, y + j], s))
                if f not in ('top', 'bottom') and h > 4:
                    for j in range(h - t.rng.randint(0, 3), h): t.put(x + i, y + j, (0, 0, 0, 0))   # ragged ends
        t.overlay(p, strands)
        t.blot(p, (90, 14, 18), .8, (1, 3), .6)
    # face: sunken black sockets, glowing eyes go on the eyes layer, gaping mouth, tear tracks of blood
    x, y, w, h = t.face('head', 0, 'front')
    t.rect(x + 1, y + 2, 2, 2, (25, 18, 26)); t.rect(x + 4, y + 2, 2, 2, (25, 18, 26))
    for ex in (x + 1, x + 5):
        for j in range(4, 7): t.put(ex, y + j, (120, 18, 22))
    t.rect(x + 2, y + 5, 3, 2, (40, 6, 10)); t.put(x + 3, y + 5, (200, 190, 160))
    t.rect(x, y, w, 1, hair); t.put(x, y + 1, hair); t.put(x + w - 1, y + 1, hair)
    # claws: bone white, darkening & bloody tips; hands raw
    for fa in ('right_forearm', 'left_forearm'):
        t.fill(fa, shade(skin, .85), 8, .2, cubes=[1])
        t.fill(fa, CLAW, 6, .4, cubes=[2, 3, 4, 5])
        def tips(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for j in range(h // 2, h): t.put(x, y + j, mix(t.px[x, y + j], (120, 20, 20), (j - h / 2) / h * 1.6))
        t.overlay(fa, tips, cubes=[2, 3, 4, 5])
    for leg in ('right_shin', 'left_shin'):
        t.blot(leg, (70, 60, 50), 3, (2, 4), .6)
        x, y, w, h = t.face(leg, 1, 'front'); t.rect(x, y, w, h, (120, 110, 100))
    t.save(f'witch_{v}')
    if v == 0:
        eyes = Image.new('RGBA', tuple(m.tex), (0, 0, 0, 0))
        x, y, w, h = t.face('head', 0, 'front')
        for ex in (x + 1, x + 5): eyes.putpixel((ex, y + 3), (255, 40, 30, 255))
        eyes.save(os.path.join(TEXDIR, 'witch_eyes.png'))
    preview(m, f'witch_{v}', f'witch_{v}', views=(0, 90, 180, 45), scale=6)

if __name__ == '__main__':
    build(); make(0); make(1)
