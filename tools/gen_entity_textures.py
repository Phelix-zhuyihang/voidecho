from PIL import Image
import os, math

BASE = r'E:\phelix\void_echo_mod\src\main\resources\assets\void_echo\textures\entity'
P = {
    'darkest':  (14,  2,  20, 255), 'shadow':   (26,  6,  40, 255),
    'mid_dark': (45,  15, 69, 255), 'main':     (69,  32,  104, 255),
    'light':    (92,  45,  133, 255), 'highlight':(123, 64,  168, 255),
    'crystal':  (155, 95, 192, 255), 'core':     (184, 120, 216, 255),
    'energy':   (212, 144, 240, 255), 'glow':     (232, 192, 255, 255),
    'white':    (240, 224, 255, 255), 'outline':  (10,  1,   18,  255),
    'flesh':    (60,  20,  80, 255),  'bone':     (100, 50,  130, 255),
}

def void_stalker():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(0, 4):
        for x in range(5, 11):
            px[x,y] = P['shadow'] if x in (5,10) else P['mid_dark']
    px[6,1] = P['energy']; px[9,1] = P['energy']
    px[7,2] = P['core']; px[8,2] = P['core']
    for y in range(4, 10):
        for x in range(4, 12):
            if x in (4,11): px[x,y] = P['shadow']
            else: px[x,y] = P['main']
    for y in range(5, 7):
        for x in range(6, 10):
            d = abs(x-7.5) + abs(y-5.5)
            if d < 1: px[x,y] = P['white']
            elif d < 2: px[x,y] = P['core']
    for y in range(4, 9):
        for x in range(3, 5): px[x,y] = P['main']
        for x in range(11, 13): px[x,y] = P['main']
    px[11,3] = P['core']; px[12,3] = P['crystal']
    px[12,4] = P['core']; px[12,5] = P['crystal']
    for y in range(10, 16):
        for x in range(5, 8): px[x,y] = P['main']
        for x in range(8, 11): px[x,y] = P['main']
        if y < 14:
            px[4,y] = P['shadow']; px[11,y] = P['shadow']
    px[5,10] = P['crystal']; px[10,10] = P['crystal']
    for y in range(4, 13):
        for x in range(12, 14):
            px[x,y] = P['darkest']
    px[5,0] = P['glow']; px[10,0] = P['glow']
    return img

def echo_warden():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(0, 2):
        for x in range(4, 12):
            if x == 7 or x == 8: px[x,y] = P['core']
            elif abs(x-7.5) < 3: px[x,y] = P['crystal']
    px[7,0] = P['white']; px[8,0] = P['glow']
    for y in range(1, 5):
        for x in range(4, 12):
            px[x,y] = P['mid_dark']
    for y in range(2, 4):
        for x in range(6, 10):
            px[x,y] = P['core']
    px[6,2] = P['white']; px[9,2] = P['white']
    px[7,3] = P['energy']
    for y in range(5, 11):
        for x in range(3, 13):
            if x in (3,12): px[x,y] = P['shadow']
            else: px[x,y] = P['main']
    for y in range(6, 9):
        for x in range(6, 10):
            d = abs(x-7.5) + abs(y-7)
            if d < 1: px[x,y] = P['white']
            elif d < 2: px[x,y] = P['core']
            else: px[x,y] = P['crystal']
    for y in range(5, 7):
        for x in range(1, 4): px[x,y] = P['crystal']
        for x in range(12, 15): px[x,y] = P['crystal']
    px[2,4] = P['energy']; px[13,4] = P['energy']
    for y in range(7, 11):
        for x in range(2, 4): px[x,y] = P['mid_dark']
        for x in range(12, 14): px[x,y] = P['mid_dark']
    for y in range(11, 16):
        for x in range(4, 7): px[x,y] = P['main']
        for x in range(9, 12): px[x,y] = P['main']
    for x in range(3, 5): px[x,11] = P['crystal']
    for x in range(11, 13): px[x,11] = P['crystal']
    return img

def shard_guard():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(1, 5):
        for x in range(4, 12):
            px[x,y] = P['main'] if x in (4,11) else P['shadow']
    for x in range(6, 10):
        for y in range(2, 4):
            px[x,y] = P['crystal']
    px[7,2] = P['white']; px[8,2] = P['white']
    for y in range(5, 11):
        for x in range(4, 12):
            px[x,y] = P['main'] if x in (4,11) or y in (5,10) else P['mid_dark']
    for pos in [(4,6),(11,6),(5,8),(10,8),(6,5),(9,5)]:
        px[pos[0],pos[1]] = P['crystal']
    for y in range(5, 9):
        for x in range(2, 4): px[x,y] = P['main']
        for x in range(12, 14): px[x,y] = P['main']
    for y in range(11, 15):
        for x in range(5, 8): px[x,y] = P['mid_dark']
        for x in range(8, 11): px[x,y] = P['mid_dark']
    px[3,5]=P['core']; px[12,5]=P['core']
    return img

def void_worm():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(4, 12):
        for x in range(0, 4):
            px[x,y] = P['flesh']
        if y in (5,6): px[2,y] = P['crystal']
    px[0,5] = P['bone']; px[0,10] = P['bone']
    for seg in range(3):
        sx = 4 + seg * 4
        for y in range(4, 12):
            for x in range(sx, sx+4):
                if seg % 2 == 0:
                    px[x,y] = P['flesh'] if y not in (4,11) else P['shadow']
                else:
                    px[x,y] = P['shadow'] if y not in (4,11) else P['main']
        px[sx+1, 3] = P['crystal']
        px[sx+1, 12] = P['crystal']
    for x in range(15, 16):
        for y in range(6, 10):
            px[x,y] = P['bone']
    return img

def crystal_wraith():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(2, 10):
        w = 2 + (4 - abs(y-6)) // 2
        for x in range(8-w, 8+w):
            px[x,y] = P['mid_dark'] if y > 3 else P['shadow']
    for y in range(0, 3):
        for x in range(6, 10):
            px[x,y] = P['shadow']
    px[6,1] = P['energy']; px[9,1] = P['energy']
    px[7,1] = P['white']
    for y in range(3, 9):
        for x in range(0, 5):
            if abs(y-6) < 4 - abs(x-1):
                px[x,y] = P['shadow']
        for x in range(11, 16):
            if abs(y-6) < 4 - abs(x-14):
                px[x,y] = P['shadow']
    for y in range(3, 9):
        for x in [0, 15]:
            if px[x,y] != (0,0,0,0):
                px[x,y] = P['crystal']
    for y in range(10, 14):
        px[7,y] = P['glow']; px[8,y] = P['glow']
    for y in range(14, 16):
        px[7,y] = P['energy']; px[8,y] = P['energy']
    return img

for name, make_fn in [('void_stalker', void_stalker), ('echo_warden', echo_warden),
                       ('shard_guard', shard_guard), ('void_worm', void_worm),
                       ('crystal_wraith', crystal_wraith)]:
    img = make_fn()
    path = os.path.join(BASE, f'{name}.png')
    img.save(path)
    colors = set()
    for y in range(img.size[1]):
        for x in range(img.size[0]):
            colors.add(img.getpixel((x, y)))
    print(f'{name}.png: {img.size[0]}x{img.size[1]}, {len(colors)-1} colors (+alpha), {os.path.getsize(path)} bytes')
print('Done')
