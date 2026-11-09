"""Builds a model JSON from a compact part list, packing every cube's box-UV island automatically (no overlaps).
P(name, parent, pivot, rot_deg, [C(from, size, inflate=0), ...])"""
import json, os
D = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/assets/l4dinfected/models/entity')

def P(name, parent, pivot, rot, cubes): return dict(name=name, parent=parent, pivot=pivot, rot=rot, cubes=cubes)
def C(frm, size, inflate=0):
    c = {'from': frm, 'size': size}
    if inflate: c['inflate'] = inflate
    return c

def build(name, parts, W):
    """shelf-pack islands (2*(d+w) x d+h) into a W-wide texture; height rounds up to a power of two."""
    cubes = [c for p in parts for c in p['cubes']]
    def fp(c): w, h, d = [int(round(s)) for s in c['size']]; return 2 * (d + w), d + h
    x = y = row = 0
    for c in sorted(cubes, key=lambda c: (-fp(c)[1], -fp(c)[0])):
        w, h = fp(c)
        if x + w > W: x, y, row = 0, y + row, 0
        c['uv'] = [x, y]; x += w; row = max(row, h)
    H = 32
    while H < y + row: H *= 2
    names = set()
    for p in parts:
        assert p['name'] not in names and (p['parent'] is None or p['parent'] in names), p['name']
        names.add(p['name'])
        if p['parent'] is None: del p['parent']
    json.dump({'tex': [W, H], 'parts': parts}, open(os.path.join(D, name + '.json'), 'w'), indent=1)
    return H
