"""Common infected: 6 variants (office shirt, hoodie, work coveralls, nurse scrubs, tank-top, suit). Reference for other scripts."""
import sys; sys.path.insert(0, __file__.rsplit('/', 1)[0])
from texlib import *

SKINS = [(150, 150, 118), (160, 140, 112), (130, 138, 112), (170, 150, 125), (140, 125, 105), (150, 160, 130)]
# (torso, arms, legs, accent)
OUTFITS = [((200, 200, 205), (200, 200, 205), (60, 62, 75), (120, 30, 30)),     # white shirt + tie
           ((70, 78, 95), (70, 78, 95), (50, 55, 70), (20, 20, 25)),             # hoodie jeans
           ((200, 120, 30), (200, 120, 30), (200, 120, 30), (40, 40, 40)),       # coveralls
           ((90, 160, 160), (90, 160, 160), (90, 160, 160), (200, 200, 200)),    # scrubs
           ((150, 40, 40), None, (110, 100, 70), (20, 20, 20)),                  # tank top, bare arms
           ((35, 35, 42), (35, 35, 42), (35, 35, 42), (200, 200, 200))]          # suit

def blood(t, part, n=8): t.blot(part, (95, 10, 10), n, (2, 5), 0.85)

def make(v):
    m = Model('common'); t = Tex(m, seed=10 + v)
    skin = SKINS[v]; torso, arms, legs, acc = OUTFITS[v]
    t.fill('head', skin, 9)
    t.fill('right_arm', arms or skin, 10); t.fill('left_arm', arms or skin, 10)
    t.fill('body', torso, 8)
    t.fill('right_leg', legs, 8); t.fill('left_leg', legs, 8)
    # skin where clothes end: forearms / hands & lower legs
    for a in ('right_arm', 'left_arm'):
        def hands(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for j in range(h - 3, h):
                for i in range(w): t.put(x + i, y + j, shade(skin, .85 + t.rng.random() * .1))
        if arms: t.overlay(a, hands)
        # torn sleeve
        def tear(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for i in range(w):
                d = t.rng.randint(0, 3)
                for j in range(h - 8 - d, h - 3):
                    t.put(x + i, y + j, shade(skin, .9 + t.rng.random() * .1))
        if arms and v in (0, 1, 3): t.overlay(a, tear, cubes=[0])
    # veins on skin
    t.blot('head', (80, 90, 110), 5, (2, 4), .5)
    # face: front of head 8x8 at (8,8)
    fx, fy = 8, 8
    t.rect(fx + 1, fy + 3, 2, 1, (15, 5, 5)); t.rect(fx + 5, fy + 3, 2, 1, (15, 5, 5))        # eye sockets
    t.put(fx + 1, fy + 3, (225, 215, 150)); t.put(fx + 6, fy + 3, (225, 215, 150))             # yellowed eyes
    t.rect(fx + 2, fy + 6, 4, 1, (45, 8, 8)); t.rect(fx + 3, fy + 7, 2, 1, (95, 15, 15))       # open bloody mouth
    t.put(fx + 2, fy + 5, (60, 40, 35)); t.put(fx + 5, fy + 5, (60, 40, 35))
    # hair
    hair = [(40, 30, 25), (20, 18, 16), (110, 90, 50), (55, 35, 25), None, (160, 160, 160)][v]
    if hair:
        t.rect(8, 0, 8, 8, hair)                      # top
        t.rect(0, 8, 8, 3, hair); t.rect(16, 8, 8, 3, hair); t.rect(24, 8, 8, 3, hair); t.rect(8, 8, 8, 2, hair)
        for i in range(8):
            if t.rng.random() < .5: t.put(8 + i, 10, hair)
    # outfit details on torso front (16+4=20, 20) 8x12
    bx, by = 20, 20
    if v == 0:
        t.rect(bx + 3, by, 2, 6, acc); t.rect(bx + 3, by + 6, 2, 1, shade(acc, .7))             # tie
        t.rect(bx + 3, by, 2, 1, (230, 230, 230))
    if v == 1: t.rect(bx + 1, by + 7, 6, 4, shade(torso, .75)); t.rect(bx + 3, by, 2, 3, shade(torso, 1.2))  # pocket + hood
    if v == 2: t.rect(bx, by + 8, 8, 1, (210, 210, 40)); t.rect(bx + 3, by, 2, 12, shade(torso, .8))     # reflective strip
    if v == 3: t.rect(bx + 2, by + 1, 4, 3, shade(torso, 1.15)); t.rect(bx + 3, by + 2, 2, 1, (170, 30, 30))
    if v == 4: t.rect(bx + 1, by, 6, 3, skin); t.rect(bx + 2, by + 3, 4, 1, shade(skin, .8))
    if v == 5: t.rect(bx + 3, by, 2, 12, (230, 230, 235)); t.rect(bx + 3, by + 2, 2, 8, acc)
    # belt
    t.rect(16, 28 - 1 + 0, 0, 0, (0, 0, 0))
    for part in ('body', 'right_arm', 'left_arm', 'right_leg', 'left_leg', 'head'):
        blood(t, part, 9 if part == 'body' else 6)
    t.blot('body', (60, 45, 30), 8, (2, 4), .6); t.blot('right_leg', (60, 45, 30), 8, (2, 4), .6); t.blot('left_leg', (60, 45, 30), 8, (2, 4), .6)
    # shoes
    for l in ('right_leg', 'left_leg'):
        def shoe(f, x, y, w, h):
            if f in ('top', 'bottom'): return
            for j in range(h - 3, h):
                for i in range(w): t.put(x + i, y + j, shade((40, 36, 34), .85 + t.rng.random() * .3))
        t.overlay(l, shoe)
    t.save(f'common_{v}')
    if v == 0: preview(m, 'common_0', 'common_0')

for v in range(6): make(v)
