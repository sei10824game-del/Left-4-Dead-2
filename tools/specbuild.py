"""Build a JSON model spec in python and auto-pack every cube's box-UV island (no overlaps, always in bounds).
    P = Spec(128, 128)
    P.part('body', None, [0, 8, 2], [0, 0, 0], [C([-8, -6, -5], [16, 6, 10])])
    P.write('tank')
"""
import json, os
from texlib import MODELS

def C(frm, size, inflate=0, mirror=False):
    c = {'from': frm, 'size': size}
    if inflate: c['inflate'] = inflate
    if mirror: c['mirror'] = True
    return c

class Spec:
    def __init__(self, w, h): self.w, self.h, self.parts = w, h, []
    def part(self, name, parent, pivot, rot=(0, 0, 0), cubes=()):
        assert name not in {p['name'] for p in self.parts}, name
        assert parent is None or parent in {p['name'] for p in self.parts}, parent
        p = {'name': name, 'pivot': list(pivot), 'rot': list(rot), 'cubes': list(cubes)}
        if parent: p['parent'] = parent
        self.parts.append(p)
    def pack(self):
        """shelf packing, tallest islands first. island = (2d+2w) x (d+h)."""
        cubes = [c for p in self.parts for c in p['cubes']]
        for c in cubes: assert all(float(s).is_integer() for s in c['size']), c
        isl = sorted(cubes, key=lambda c: -(c['size'][2] + c['size'][1]))
        x = y = row = 0
        for c in isl:
            w, h, d = (int(s) for s in c['size'])
            iw, ih = 2 * (w + d), d + h
            if x + iw > self.w: x, y, row = 0, y + row, 0
            assert y + ih <= self.h, 'texture too small'
            c['uv'] = [x, y]; x += iw; row = max(row, ih)
    def write(self, name):
        self.pack()
        json.dump({'tex': [self.w, self.h], 'parts': self.parts}, open(os.path.join(MODELS, name + '.json'), 'w'), indent=1)
