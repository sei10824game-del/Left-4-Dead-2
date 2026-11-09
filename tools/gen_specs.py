"""Writes the default humanoid spec for every infected (specials get replaced by hand-authored specs)."""
import json, os, sys
D = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/assets/l4dinfected/models/entity')
def cube(uv, frm, size, **k): return dict(uv=uv, **{'from': frm}, size=size, **k)
def humanoid():
    return {'tex': [64, 64], 'parts': [
        {'name': 'body', 'pivot': [0, 0, 0], 'cubes': [cube([16, 16], [-4, 0, -2], [8, 12, 4])]},
        {'name': 'head', 'parent': 'body', 'pivot': [0, 0, 0], 'cubes': [cube([0, 0], [-4, -8, -4], [8, 8, 8])]},
        {'name': 'right_arm', 'parent': 'body', 'pivot': [-5, 2, 0], 'cubes': [cube([40, 16], [-3, -2, -2], [4, 12, 4])]},
        {'name': 'left_arm', 'parent': 'body', 'pivot': [5, 2, 0], 'cubes': [cube([32, 48], [-1, -2, -2], [4, 12, 4])]},
        {'name': 'right_leg', 'pivot': [-1.9, 12, 0], 'cubes': [cube([0, 16], [-2, 0, -2], [4, 12, 4])]},
        {'name': 'left_leg', 'pivot': [1.9, 12, 0], 'cubes': [cube([16, 48], [-2, 0, -2], [4, 12, 4])]}]}
if __name__ == '__main__':
    force = '--force' in sys.argv
    for n in sys.argv[1:]:
        if n.startswith('--'): continue
        p = os.path.join(D, n + '.json')
        if force or not os.path.exists(p): json.dump(humanoid(), open(p, 'w'), indent=1)
