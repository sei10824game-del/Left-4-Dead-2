"""Shared texture painter + software previewer for the JSON models.
Usage in a texture script:
    from texlib import *
    m = Model('hunter'); t = Tex(m)
    t.fill('head', skin)              # paint every face of a part (with noise/shading)
    t.face('head', 0, 'front', lambda x,y,w,h: ...)  # access a face rect to draw detail
    t.save('hunter')                  # writes assets/.../textures/entity/hunter.png
    preview(m, 'hunter')              # writes tools/preview/hunter.png (front/side/back/3-4 views)
Cube faces: top,bottom,right,front,left,back (Minecraft box UV layout).
"""
import json, math, os, random
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.abspath(__file__))
MODELS = os.path.join(ROOT, '..', 'src/main/resources/assets/l4dinfected/models/entity')
TEXDIR = os.path.join(ROOT, '..', 'src/main/resources/assets/l4dinfected/textures/entity')
os.makedirs(TEXDIR, exist_ok=True)
os.makedirs(os.path.join(ROOT, 'preview'), exist_ok=True)

def clamp(v): return max(0, min(255, int(v)))
def shade(c, f): return tuple(clamp(x * f) for x in c[:3]) + ((c[3],) if len(c) > 3 else ())
def mix(a, b, t): return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))

class Model:
    def __init__(self, name):
        self.name = name
        self.spec = json.load(open(os.path.join(MODELS, name + '.json')))
        self.tex = self.spec.get('tex', [64, 64])
        self.parts = {p['name']: p for p in self.spec['parts']}

def face_rects(cube):
    """(u,v,w,h,d) -> dict face -> (x,y,w,h) in texture pixels (ints)."""
    u, v = cube['uv']; w, h, d = [int(round(s)) for s in cube['size']]
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h),
            'front': (u + d, v + d, w, h), 'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h)}

class Tex:
    def __init__(self, model, seed=1):
        self.m = model; self.rng = random.Random(seed)
        W, H = model.tex
        self.img = Image.new('RGBA', (W, H), (0, 0, 0, 0)); self.px = self.img.load()
    def rects(self, part):
        return [face_rects(c) for c in self.m.parts[part].get('cubes', [])]
    def face(self, part, cube, face, fn=None):
        """returns rect; if fn given call fn(x,y,w,h)."""
        r = face_rects(self.m.parts[part]['cubes'][cube])[face]
        if fn: fn(*r)
        return r
    def put(self, x, y, c):
        if 0 <= x < self.img.width and 0 <= y < self.img.height: self.px[x, y] = c if len(c) == 4 else c + (255,)
    def rect(self, x, y, w, h, c):
        for j in range(h):
            for i in range(w): self.put(x + i, y + j, c)
    def fill(self, part, base, noise=10, grad=0.22, faces=None, cubes=None):
        """base colour + per-pixel noise + vertical gradient (top lighter) + face-dependent shading."""
        sh = {'top': 1.12, 'bottom': .62, 'front': 1.0, 'back': .84, 'right': .9, 'left': .9}
        for ci, rs in enumerate(self.rects(part)):
            if cubes is not None and ci not in cubes: continue
            for f, (x, y, w, h) in rs.items():
                if faces and f not in faces: continue
                for j in range(h):
                    for i in range(w):
                        g = 1 + grad * (0.5 - j / max(1, h - 1)) if f not in ('top', 'bottom') else 1
                        n = self.rng.uniform(-noise, noise) / 100
                        self.put(x + i, y + j, shade(base, sh[f] * g * (1 + n)))
    def overlay(self, part, fn, faces=None, cubes=None):
        """call fn(face_name,x,y,w,h) for each face -> lets you draw details with self.put/rect."""
        for ci, rs in enumerate(self.rects(part)):
            if cubes is not None and ci not in cubes: continue
            for f, (x, y, w, h) in rs.items():
                if faces and f not in faces: continue
                fn(f, x, y, w, h)
    def blot(self, part, color, n, size=(1, 3), alpha=1.0, faces=None):
        """random stains/blood/dirt spots confined to faces of a part."""
        for rs in self.rects(part):
            for f, (x, y, w, h) in rs.items():
                if faces and f not in faces: continue
                for _ in range(max(0, int(n * w * h / 64))):
                    cx, cy = x + self.rng.randrange(w), y + self.rng.randrange(h)
                    for _ in range(self.rng.randint(*size)):
                        px_, py_ = cx + self.rng.randint(-1, 1), cy + self.rng.randint(-1, 1)
                        if x <= px_ < x + w and y <= py_ < y + h:
                            old = self.px[px_, py_]; self.put(px_, py_, mix(old, color, alpha))
    def save(self, name):
        self.img.save(os.path.join(TEXDIR, name + '.png'))

# ---------------- preview (orthographic painter's algorithm) ----------------
def _rot(v, r):
    x, y, z = v; rx, ry, rz = r
    # ModelPart applies Z then Y then X (rotationZYX): v' = Rz*Ry*Rx*v
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x, y, z)

def preview(model, texname=None, name=None, scale=6, views=(0, 90, 180, 45), pose=None):
    """Render textured boxes. pose: {part: (rx,ry,rz) radians extra rotation} for checking poses."""
    tex = Image.open(os.path.join(TEXDIR, (texname or model.name) + '.png')).convert('RGBA')
    tw, th = model.tex
    W = 70 * scale; H = 56 * scale
    out = Image.new('RGBA', (W * len(views), H), (40, 44, 52, 255))
    xf = {}
    def world(pn):
        if pn in xf: return xf[pn]
        p = model.parts[pn]
        r = [math.radians(a) for a in p.get('rot', [0, 0, 0])]
        if pose and pn in pose: r = [r[i] + pose[pn][i] for i in range(3)]
        par = p.get('parent')
        piv = p.get('pivot', [0, 0, 0])
        pw = world(par) if par else (lambda v: v)
        def f(v, piv=piv, r=r, pw=pw):
            q = _rot(v, r); q = (q[0] + piv[0], q[1] + piv[1], q[2] + piv[2]); return pw(q)
        xf[pn] = f; return f
    quads = []
    for pn, p in model.parts.items():
        f = world(pn)
        for c in p.get('cubes', []):
            x0, y0, z0 = c['from']; sx, sy, sz = c['size']; inf = c.get('inflate', 0)
            x0 -= inf; y0 -= inf; z0 -= inf; sx += 2 * inf; sy += 2 * inf; sz += 2 * inf
            x1, y1, z1 = x0 + sx, y0 + sy, z0 + sz
            rects = face_rects(c); mir = c.get('mirror', False)
            # face -> (corner origin, u-axis vector, v-axis vector) in model space (v goes down the texture)
            F = {'front': ((x0, y0, z0), (sx, 0, 0), (0, sy, 0)), 'back': ((x1, y0, z1), (-sx, 0, 0), (0, sy, 0)),
                 'right': ((x0, y0, z1), (0, 0, -sz), (0, sy, 0)), 'left': ((x1, y0, z0), (0, 0, sz), (0, sy, 0)),
                 'top': ((x0, y0, z1), (sx, 0, 0), (0, 0, -sz)), 'bottom': ((x0, y1, z0), (sx, 0, 0), (0, 0, sz))}
            for fn, (o, ua, va) in F.items():
                rx, ry, rw, rh = rects[fn]
                if rw <= 0 or rh <= 0: continue
                for j in range(rh):
                    for i in range(rw):
                        ii = (rw - 1 - i) if mir and fn not in ('top', 'bottom') else i
                        col = tex.getpixel((rx + ii, ry + j)) if 0 <= rx + ii < tw and 0 <= ry + j < th else (0, 0, 0, 0)
                        if col[3] < 10: continue
                        pts = []
                        for (a, b) in ((i, j), (i + 1, j), (i + 1, j + 1), (i, j + 1)):
                            v = (o[0] + ua[0] * a / rw + va[0] * b / rh, o[1] + ua[1] * a / rw + va[1] * b / rh,
                                 o[2] + ua[2] * a / rw + va[2] * b / rh)
                            pts.append(f(v))
                        quads.append((pts, col))
    for vi, ang in enumerate(views):
        a = math.radians(ang); layer = Image.new('RGBA', (W, H), (0, 0, 0, 0)); d = ImageDraw.Draw(layer)
        items = []
        for pts, col in quads:
            pr = [(x * math.cos(a) + z * math.sin(a), y, -x * math.sin(a) + z * math.cos(a)) for x, y, z in pts]
            # viewer sits at -z looking +z; nearer = smaller z. face normal check for backface culling
            ux, uy, uz = [pr[1][k] - pr[0][k] for k in range(3)]; vx, vy, vz = [pr[3][k] - pr[0][k] for k in range(3)]
            nz = ux * vy - uy * vx
            items.append((sum(p[2] for p in pr) / 4, pr, col, nz))
        items.sort(key=lambda t: -t[0])
        for z, pr, col, nz in items:
            xy = [(W / 2 + p[0] * scale, H - 6 * scale - (24 - p[1]) * scale) for p in pr]
            d.polygon(xy, fill=col[:3] + (255,))
        out.paste(layer, (vi * W, 0), layer)
    out.save(os.path.join(ROOT, 'preview', (name or model.name) + '.png'))
