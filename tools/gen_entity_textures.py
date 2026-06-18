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

def crystal_guardian():
    """64x64 floating crystal guardian — diamond body, energy core, crystal wings"""
    img = Image.new('RGBA', (64, 64), (0,0,0,0))
    px = img.load()
    cx, cy = 31.5, 29.5  # center (upper half to leave room for body)
    # Diamond body
    for y in range(64):
        for x in range(64):
            d = abs(x - cx) + abs(y - cy)
            if d < 20:
                if d < 6: px[x,y] = P['white']
                elif d < 10: px[x,y] = P['glow'] if (x+y)%3==0 else P['energy']
                elif d < 14: px[x,y] = P['crystal'] if (x+y)%2==0 else P['core']
                elif d < 18: px[x,y] = P['main']
                else: px[x,y] = P['mid_dark']
    # Eyes
    for eye_y in range(25, 29):
        for eye_x in range(27, 30): px[eye_x, eye_y] = P['white']
        for eye_x in range(34, 37): px[eye_x, eye_y] = P['white']
    # Crystal wings (side projections)
    for y in range(20, 44):
        wy = 6 - abs(y-32)*0.2
        for x in range(int(cx-20-wy), int(cx-18)):
            if 0<=x<64:
                px[x,y] = P['crystal'] if (x+y)%3==0 else P['core']
        for x in range(int(cx+18), int(cx+20+wy)):
            if 0<=x<64:
                px[x,y] = P['crystal'] if (x+y)%3==0 else P['core']
    # Top crystal spike
    for y in range(8, 18):
        w = 3 - (y-8)*0.3
        for x in range(int(cx-w), int(cx+w+1)):
            if 0<=x<64:
                px[x,y] = P['energy'] if y<13 else P['crystal']
    px[int(cx),7] = P['white']
    # Bottom body
    for y in range(40, 55):
        wy = 4 - (y-40)*0.25
        for x in range(int(cx-wy), int(cx+wy+1)):
            if 0<=x<64:
                px[x,y] = P['main'] if x%2==0 else P['mid_dark']
    # Floating crystal bits
    for ox,oy in [(12,18),(50,18),(10,35),(52,35),(24,10),(38,10)]:
        for dy in range(2):
            for dx in range(2):
                px[ox+dx,oy+dy] = P['energy']
    return img

def crystal_sprite():
    """32x32 crystal sprite — small fairy with translucent wings and glow"""
    img = Image.new('RGBA', (32, 32), (0,0,0,0))
    px = img.load()
    cx, cy = 15.5, 14.5
    # Body (small humanoid)
    for y in range(11, 18):
        wy = 3 - abs(y-14)*0.5
        for x in range(int(cx-wy), int(cx+wy+1)):
            if 0<=x<32:
                px[x,y] = P['crystal'] if (x+y)%2==0 else P['core']
    # Head
    for y in range(8, 12):
        for x in range(int(cx-2.5), int(cx+3.5)):
            if 0<=x<32:
                px[x,y] = P['glow'] if (x+y)%3==0 else P['energy']
    # Eyes
    px[int(cx-1),9] = P['white']
    px[int(cx+2),9] = P['white']
    # Wings (translucent — use alpha variation)
    wing_colors = [P['crystal'], P['core'], P['energy'], P['light']]
    for y in range(6, 20):
        for x in range(3, int(cx-2)):
            d = abs(x-5) + abs(y-13)
            if d < 8 and 0<=x<32:
                ci = (x+y)%len(wing_colors)
                px[x,y] = wing_colors[ci]
        for x in range(int(cx+3), 29):
            d = abs(x-27) + abs(y-13)
            if d < 8 and 0<=x<32:
                ci = (x+y)%len(wing_colors)
                px[x,y] = wing_colors[ci]
    # Glow trail below
    for y in range(19, 28):
        alpha = max(0, (28-y)*28)
        for x in range(int(cx-1.5), int(cx+2.5)):
            if 0<=x<32:
                r,g,b,_ = P['energy']
                px[x,y] = (r,g,b,alpha)
    # Antennae
    for x in [14, 17]:
        for dy in range(3):
            px[x, 6-dy] = P['core']
    px[14,5] = P['glow']; px[17,5] = P['glow']
    return img

for name, make_fn in [('void_stalker', void_stalker), ('echo_warden', echo_warden),
                       ('shard_guard', shard_guard), ('void_worm', void_worm),
                       ('crystal_wraith', crystal_wraith),
                       ('crystal_guardian', crystal_guardian),
                       ('crystal_sprite', crystal_sprite)]:
    img = make_fn()
    path = os.path.join(BASE, f'{name}.png')
    img.save(path)
    colors = set()
    for y in range(img.size[1]):
        for x in range(img.size[0]):
            colors.add(img.getpixel((x, y)))
    print(f'{name}.png: {img.size[0]}x{img.size[1]}, {len(colors)-1} colors (+alpha), {os.path.getsize(path)} bytes')
print('Done')
