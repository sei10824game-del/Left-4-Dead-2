"""Writes hunter.json + jockey.json (part tree + auto-packed, non-overlapping cube UVs). Run before tex_hunter/tex_jockey."""
import json, os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/assets/l4dinfected/models/entity')

def C(fr, sz, inflate=0): return (fr, sz, inflate)

def pack(parts, W, H):
    """shelf packer: every cube gets its own (2(d+w)) x (d+h) island."""
    cubes = [c for p in parts for c in p['cubes']]
    order = sorted(cubes, key=lambda c: -(c['size'][2] + c['size'][1]))
    x = y = row = 0
    for c in order:
        w, h, d = c['size']; cw, ch = 2 * (d + w), d + h
        if x + cw > W: x, y, row = 0, y + row, 0
        c['uv'] = [x, y]; x += cw; row = max(row, ch)
        assert y + ch <= H, 'texture too small'

def build(name, W, H, spec):
    parts = []
    for n, par, piv, rot, cubes in spec:
        p = {'name': n, 'pivot': piv, 'rot': rot, 'cubes': []}
        if par: p['parent'] = par
        for fr, sz, inf in cubes:
            c = {'from': fr, 'size': sz}
            if inf: c['inflate'] = inf
            p['cubes'].append(c)
        parts.append(p)
    names = [p['name'] for p in parts]
    assert len(set(names)) == len(names)
    for p in parts: assert 'parent' not in p or names.index(p['parent']) < names.index(p['name'])
    pack(parts, W, H)
    json.dump({'tex': [W, H], 'parts': parts}, open(os.path.join(OUT, name + '.json'), 'w'), indent=1)

def arm(side, sx, shoulder, upper, fore, hand_cubes):
    s = 'right' if sx < 0 else 'left'
    return [(f'{s}_arm', 'body', [sx * shoulder[0], shoulder[1], shoulder[2]], [0, 0, 0], upper),
            (f'{s}_fore', f'{s}_arm', [0, upper[0][0][1] + upper[0][1][1], 0], [0, 0, 0], fore),
            (f'{s}_hand', f'{s}_fore', [0, fore[0][1][1], 0], [0, 0, 0], hand_cubes)]

# ---------------- hunter: gaunt, long-limbed stalker with a ragged tarp mantle ----------------
H = [('body', None, [0, 12, 0], [0, 0, 0], [
        C([-3, -6, -2], [6, 6, 4]),            # 0 waist
        C([-4, -11, -2.5], [8, 5, 5]),         # 1 ribcage
        C([-4.5, -11.5, -3], [9, 3, 6], .2),   # 2 tarp mantle over the shoulders
        C([-0.5, -9, 2.5], [1, 2, 2]),         # 3-5 spinal spurs
        C([-0.5, -6, 1.5], [1, 2, 2]),
        C([-0.5, -3, 1.5], [1, 1, 2])]),
     ('rag', 'body', [0, -1, 2], [12, 0, 0], [C([-3.5, 0, 0], [7, 9, 0])]),
     ('rag_front', 'body', [0, -1, -2.05], [-6, 0, 0], [C([-2.5, 0, 0], [5, 6, 0])]),
     ('head', 'body', [0, -11, -1], [0, 0, 0], [
        C([-3, -6, -4], [6, 5, 6]),            # 0 skull
        C([-2.5, -3, -6], [5, 2, 2]),          # 1 muzzle / upper teeth
        C([-0.5, -7, -1], [1, 1, 4])]),        # 2 bony crest
     ('jaw', 'head', [0, -1, 1], [0, 0, 0], [C([-2.5, 0, -7], [5, 2, 7])]),
     *arm('r', -1, [4.5, -10, 0], [C([-2, -1, -1.5], [3, 7, 3])], [C([-1, 0, -1], [2, 7, 2])],
          [C([-1.5, 0, -1.5], [3, 1, 3]), C([-1.5, 1, -1.5], [1, 4, 1]), C([0.5, 1, -1.5], [1, 4, 1]), C([-0.5, 1, 0.5], [1, 3, 1])]),
     *arm('l', 1, [4.5, -10, 0], [C([-1, -1, -1.5], [3, 7, 3])], [C([-1, 0, -1], [2, 7, 2])],
          [C([-1.5, 0, -1.5], [3, 1, 3]), C([-1.5, 1, -1.5], [1, 4, 1]), C([0.5, 1, -1.5], [1, 4, 1]), C([-0.5, 1, 0.5], [1, 3, 1])]),
     ('right_leg', None, [-2, 12, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 6, 3])]),
     ('right_shin', 'right_leg', [0, 6, 0], [0, 0, 0], [C([-1, 0, -1], [2, 5, 2])]),
     ('right_foot', 'right_shin', [0, 5, 0], [0, 0, 0], [C([-1.5, 0, -3.5], [3, 1, 5])]),
     ('left_leg', None, [2, 12, 0], [0, 0, 0], [C([-1.5, 0, -1.5], [3, 6, 3])]),
     ('left_shin', 'left_leg', [0, 6, 0], [0, 0, 0], [C([-1, 0, -1], [2, 5, 2])]),
     ('left_foot', 'left_shin', [0, 5, 0], [0, 0, 0], [C([-1.5, 0, -3.5], [3, 1, 5])])]

# ---------------- jockey: tumour-hunched, frog-mouthed gremlin with long knuckle-walking arms ----------------
J = [('body', None, [0, 17, 0], [0, 0, 0], [
        C([-3, -6, -2.5], [6, 6, 5]),          # 0 torso
        C([-3, -7.5, 0], [6, 5, 4], .3),       # 1 hump
        C([0.5, -9, 1], [2, 2, 2]),            # 2-4 tumours
        C([-2.8, -5.5, 3.3], [2, 2, 2]),
        C([-1, -4, 3.5], [2, 1, 1]),
        C([-2.5, -3.5, -3.2], [5, 3, 1])]),    # 5 sagging belly
     ('rag', 'body', [0, -0.5, 2.5], [10, 0, 0], [C([-2.5, 0, 0], [5, 4, 0])]),
     ('head', 'body', [0, -6, -2], [0, 0, 0], [
        C([-3, -5, -5], [6, 5, 5]),            # 0 skull
        C([-2.5, -4.5, -5.6], [2, 2, 1])]),    # 1 bulging cataract eye
     ('jaw', 'head', [0, 0, -0.5], [0, 0, 0], [C([-3, 0, -4.5], [6, 1, 4])]),
     ('right_ear', 'head', [-3, -3, -2], [0, -20, -25], [C([-3, -1, 0], [3, 2, 0])]),
     ('left_ear', 'head', [3, -3, -2], [0, 20, 25], [C([0, -1, 0], [3, 2, 0])]),
     *arm('r', -1, [3.5, -5.5, -0.5], [C([-1, -0.5, -1], [2, 5, 2])], [C([-1, 0, -1], [2, 5, 2])],
          [C([-1.5, 0, -1.5], [3, 1, 3]), C([-1.5, 1, -1.5], [1, 3, 1]), C([0.5, 1, -1.5], [1, 3, 1]), C([-0.5, 1, 0.5], [1, 2, 1])]),
     *arm('l', 1, [3.5, -5.5, -0.5], [C([-1, -0.5, -1], [2, 5, 2])], [C([-1, 0, -1], [2, 5, 2])],
          [C([-1.5, 0, -1.5], [3, 1, 3]), C([-1.5, 1, -1.5], [1, 3, 1]), C([0.5, 1, -1.5], [1, 3, 1]), C([-0.5, 1, 0.5], [1, 2, 1])]),
     ('right_leg', None, [-1.8, 17, 0], [0, 0, 0], [C([-1, 0, -1], [2, 3, 2])]),
     ('right_shin', 'right_leg', [0, 3, 0], [0, 0, 0], [C([-1, 0, -1], [2, 3, 2])]),
     ('right_foot', 'right_shin', [0, 3, 0], [0, 0, 0], [C([-1, 0, -2.5], [2, 1, 3])]),
     ('left_leg', None, [1.8, 17, 0], [0, 0, 0], [C([-1, 0, -1], [2, 3, 2])]),
     ('left_shin', 'left_leg', [0, 3, 0], [0, 0, 0], [C([-1, 0, -1], [2, 3, 2])]),
     ('left_foot', 'left_shin', [0, 3, 0], [0, 0, 0], [C([-1, 0, -2.5], [2, 1, 3])])]

if __name__ == '__main__':
    build('hunter', 64, 64, H)
    build('jockey', 64, 64, J)
