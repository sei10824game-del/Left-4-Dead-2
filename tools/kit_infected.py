"""Model + paint kit shared by tex_common.py and the uncommon texture scripts.
Each script builds its model spec in python (UVs auto-packed -> the json and the texture can never drift apart),
writes models/entity/<name>.json, then paints the texture with the helpers below.
Humanoid layout (hip pivot so leaning/crouching rotates around the hips): body(hip) > head, right/left_arm > *_forearm;
right/left_leg > *_shin (root level)."""
import sys, os, json, math, zlib
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from texlib import *
import numpy as np

SIDES = ('right', 'front', 'left', 'back')


def hh(*a):
    """deterministic hash (python's str hash is salted per run)."""
    return zlib.crc32(repr(a).encode())


def C(frm, size, inf=0, mir=False):
    c = {'from': list(frm), 'size': list(size)}
    if inf: c['inflate'] = inf
    if mir: c['mirror'] = True
    return c


class Spec:
    def __init__(self, name, tex=(128, 128)):
        self.name, self.tex, self.parts, self.by = name, list(tex), [], {}

    def part(self, name, parent=None, pivot=(0, 0, 0), rot=None, cubes=()):
        assert name not in self.by, name
        assert parent is None or parent in self.by, (name, parent)
        p = {'name': name}
        if parent: p['parent'] = parent
        p['pivot'] = list(pivot)
        if rot: p['rot'] = list(rot)
        p['cubes'] = list(cubes)
        self.parts.append(p); self.by[name] = p
        return self

    def humanoid(self, inf_body=0, inf_arm=0, inf_leg=0, head=True):
        self.part('body', None, (0, 12, 0), cubes=[C((-4, -12, -2), (8, 12, 4), inf_body)])
        self.part('head', 'body', (0, -12, 0), cubes=[C((-4, -8, -4), (8, 8, 8))] if head else [])
        for s, sx in (('right', -1), ('left', 1)):
            self.part(s + '_arm', 'body', (5 * sx, -10, 0), cubes=[C((-3 if sx < 0 else -1, -2, -2), (4, 6, 4), inf_arm)])
            self.part(s + '_forearm', s + '_arm', (sx, 4, 0), cubes=[C((-2, 0, -2), (4, 6, 4), inf_arm)])
        for s, sx in (('right', -1), ('left', 1)):
            self.part(s + '_leg', None, (1.9 * sx, 12, 0), cubes=[C((-2, 0, -2), (4, 6, 4), inf_leg)])
            self.part(s + '_shin', s + '_leg', (0, 6, 0), cubes=[C((-2, 0, -2), (4, 6, 4), inf_leg)])
        return self

    def pack(self):
        W, H = self.tex
        used = np.zeros((H, W), bool)
        cubes = [c for p in self.parts for c in p['cubes']]
        for c in cubes:
            assert all(float(s).is_integer() for s in c['size']), c
        def box(c):
            w, h, d = (int(s) for s in c['size'])
            return max(1, 2 * (d + w)), max(1, d + h)
        for c in sorted(cubes, key=lambda c: (-box(c)[1], -box(c)[0])):
            bw, bh = box(c)
            for y in range(H - bh + 1):
                row = used[y:y + bh]
                xs = [x for x in range(W - bw + 1) if not row[:, x:x + bw].any()]
                if xs: break
            else:
                raise SystemExit(f'{self.name}: texture {W}x{H} too small')
            used[y:y + bh, xs[0]:xs[0] + bw] = True
            c['uv'] = [xs[0], y]

    def save(self):
        self.pack()
        out = {'tex': self.tex, 'parts': [{k: (v if k != 'cubes' else [{'uv': c['uv'], **{q: c[q] for q in c if q != 'uv'}} for c in v])
                                           for k, v in p.items()} for p in self.parts]}
        with open(os.path.join(MODELS, self.name + '.json'), 'w') as f:
            json.dump(out, f, indent=1)
        return Model(self.name)


class Paint(Tex):
    """Tex + per-pixel painting: fn(face, i, j, w, h, old) -> colour | None (i,j local to the face)."""
    def paint(self, part, fn, faces=SIDES, cubes=None):
        for ci, rs in enumerate(self.rects(part)):
            if cubes is not None and ci not in cubes: continue
            for f, (x, y, w, h) in rs.items():
                if faces and f not in faces: continue
                for j in range(h):
                    for i in range(w):
                        c = fn(f, i, j, w, h, self.px[x + i, y + j])
                        if c is not None: self.put(x + i, y + j, c)

    def at(self, part, face, i, j, c, cube=0):
        """pixel on a face; negative i/j count from the right/bottom."""
        x, y, w, h = self.face(part, cube, face)
        self.put(x + (i % w), y + (j % h), c)

    def box(self, part, face, i, j, w, h, c, cube=0):
        for b in range(h):
            for a in range(w): self.at(part, face, i + a, j + b, c, cube)

    def band(self, part, j0, j1, c, faces=SIDES, cubes=None, noise=6, from_bottom=False):
        def f(fc, i, j, w, h, old):
            jj = h - 1 - j if from_bottom else j
            if j0 <= jj < j1: return shade(c, 1 + self.rng.uniform(-noise, noise) / 100)
        self.paint(part, f, faces, cubes)

    def folds(self, part, k=.82, n=3, faces=SIDES):
        """vertical cloth creases."""
        for rs in self.rects(part):
            for fc, (x, y, w, h) in rs.items():
                if fc not in faces or w < 2: continue
                for _ in range(max(1, n * w // 8)):
                    i = self.rng.randrange(w); j0 = self.rng.randrange(max(1, h // 2)); ln = self.rng.randint(2, max(2, h - j0))
                    for j in range(j0, min(h, j0 + ln)):
                        self.put(x + i, y + j, shade(self.px[x + i, y + j], k))

    def edge(self, part, k=.7, faces=SIDES, cubes=None):
        """darken the bottom/top rows of side faces for definition."""
        self.paint(part, lambda f, i, j, w, h, o: shade(o, k) if (j == h - 1 and h > 2) else None, faces, cubes)

    def grime(self, part, n=6, c=(60, 45, 30), a=.55): self.blot(part, c, n, (2, 4), a)
    def blood(self, part, n=6, a=.85): self.blot(part, (95, 12, 10), n, (2, 5), a)


def zombie_face(t, part='head', skin=(150, 150, 118), eye=(225, 215, 150), cube=0, mouth=True, top=1):
    """sunken sockets + bloodshot eyes + gore mouth on the 8-wide front face. top: socket row."""
    x, y, w, h = t.face(part, cube, 'front')
    cx = w // 2
    for dx in (-3, 1):
        t.rect(x + cx + dx, y + top + 2, 2, 1, (25, 10, 10))
        t.put(x + cx + dx + (0 if dx < 0 else 1), y + top + 2, eye)
        t.put(x + cx + dx, y + top + 1, shade(skin, .7)); t.put(x + cx + dx + 1, y + top + 1, shade(skin, .75))
        t.put(x + cx + dx, y + top + 3, shade(skin, .72))
    t.put(x + cx - 1, y + top + 4, shade(skin, .6)); t.put(x + cx, y + top + 4, shade(skin, .65))  # nose shadow
    if mouth:
        t.rect(x + cx - 2, y + h - 2, 4, 1, (45, 8, 8)); t.rect(x + cx - 1, y + h - 1, 2, 1, (95, 15, 15))


def glow(m, name, pixels):
    """emissive overlay: pixels = [(part, face, i, j, colour, cube?)]; written as textures/entity/<name>_eyes.png."""
    e = Paint(m)
    for p in pixels:
        e.at(p[0], p[1], p[2], p[3], p[4], p[5] if len(p) > 5 else 0)
    e.save(name + '_eyes')


def check(m):
    """uv sanity: inside texture, no overlap between cube islands."""
    W, H = m.tex; used = np.zeros((H, W), int)
    for p in m.parts.values():
        for c in p['cubes']:
            for f, (x, y, w, h) in face_rects(c).items():
                assert 0 <= x and 0 <= y and x + w <= W and y + h <= H, (p['name'], f)
                used[y:y + h, x:x + w] += 1
    assert used.max() <= 1, 'overlapping uv'


def look(m, tex, name, pose=None, views=(0, 90, 180, 35), scale=10):
    """preview + crop around the figure so the views are big enough to judge."""
    preview(m, tex, name, scale=scale, views=views, pose=pose)
    p = os.path.join(ROOT, 'preview', name + '.png'); img = Image.open(p)
    W = 70 * scale; out = Image.new("RGBA", (34 * scale * len(views), 44 * scale))
    for i in range(len(views)):
        out.paste(img.crop((i * W + W // 2 - 17 * scale, 12 * scale, i * W + W // 2 + 17 * scale, 56 * scale)), (i * 34 * scale, 0))
    out.save(p)
