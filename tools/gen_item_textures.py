from PIL import Image
import os, math, random

BASE = r'E:\phelix\void_echo_mod\src\main\resources\assets\void_echo\textures\item'
random.seed(42)

P = {
    'darkest':  (14,  2,  20, 255), 'shadow':   (26,  6,  40, 255),
    'mid_dark': (45,  15, 69, 255), 'main':     (69,  32,  104, 255),
    'light':    (92,  45,  133, 255), 'highlight':(123, 64,  168, 255),
    'crystal':  (155, 95, 192, 255), 'core':     (184, 120, 216, 255),
    'energy':   (212, 144, 240, 255), 'glow':     (232, 192, 255, 255),
    'white':    (240, 224, 255, 255), 'outline':  (10,  1,   18,  255),
    'gold':     (200, 160, 60, 255),  'gold_light':(240, 200, 100, 255),
    'red':      (180, 40,  60, 255),  'red_glow': (220, 100, 120, 255),
    'cyan':     (60,  180, 200, 255), 'cyan_glow':(120, 220, 240, 255),
}

def diamond_shape(px, cx, cy, size, inner, outer, edge=None):
    """Draw a diamond shape centered at cx,cy"""
    for y in range(16):
        for x in range(16):
            d = abs(x-cx) + abs(y-cy)
            if d < size * 0.4:
                px[x,y] = inner
            elif d < size * 0.8:
                px[x,y] = outer
            elif edge and d < size:
                px[x,y] = edge

def make_void_heart():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Inner glow diamond (rendered first so heart overlays it)
    diamond_shape(px, 7.5, 6.5, 5, P['white'], P['core'], P['crystal'])
    # Heart shape on top
    for y in range(16):
        for x in range(16):
            hx, hy = abs(x-7.5), y-6
            if y < 9 and abs(hx-3) < 2 and hy < 3:
                if abs(hx-3) < 1: px[x,y] = P['energy']
                else: px[x,y] = P['core']
            elif 9 <= y <= 13 and abs(x-7.5) < (14-y)*0.8:
                px[x,y] = P['crystal'] if (x+y)%2==0 else P['core']
            elif y == 14 and x in (6,7,8,9):
                px[x,y] = P['main']
    return img

def make_echo_core():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Concentric crystal rings
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-7.5)**2 + (y-7.5)**2)
            ring = math.sin(d * 2.5) * 0.5 + 0.5
            if d < 6:
                if d < 1.5: px[x,y] = P['white']
                elif ring > 0.7: px[x,y] = P['crystal']
                elif ring > 0.4: px[x,y] = P['core']
                else: px[x,y] = P['main']
    diamond_shape(px, 7.5, 7.5, 5, P['white'], P['energy'], P['crystal'])
    return img

def make_void_key():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Key head (top, y=0-5)
    for y in range(0, 6):
        for x in range(4, 12):
            if y < 3:
                px[x,y] = P['gold'] if (x+y)%2==0 else P['gold_light']
            else:
                px[x,y] = P['gold'] if x in (4,11) else P['gold_light']
    # Key teeth (y=4-6)
    for x in range(4, 7): px[x,6] = P['gold']
    px[5,7] = P['gold']; px[4,8] = P['gold']
    # Key shaft (y=6-12)
    for y in range(6, 13):
        for x in range(6, 10):
            px[x,y] = P['gold'] if x in (6,9) else P['gold_light']
    # Crystal inlay on head
    diamond_shape(px, 7.5, 2, 3, P['white'], P['energy'], P['crystal'])
    # Key bit (bottom)
    for x in range(5, 8): px[x,13] = P['gold']
    for x in range(5, 8): px[x,14] = P['gold']
    return img

def make_alloy_ingot():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Ingot shape
    for y in range(3, 13):
        w = 5 - abs(y-8)*0.3
        for x in range(int(8-w), int(8+w)):
            if 0 <= x < 16:
                edge = abs(x-7.5) > w-1 or y in (3,12)
                px[x,y] = P['light'] if edge else P['main']
    # Crystal stripe
    for y in range(5, 11):
        for x in range(7, 9):
            px[x,y] = P['crystal'] if (x+y)%2==0 else P['core']
    # Highlight
    for x in range(6, 10): px[x,4] = P['highlight']
    return img

def make_crystal_shard():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Sharp crystal shard pointing up-right
    cx, cy = 6.5, 9.5
    for y in range(16):
        for x in range(16):
            dx, dy = x-cx, y-cy
            angle = math.atan2(dy, dx)
            dist = math.sqrt(dx*dx + dy*dy)
            # Diamond shape rotated
            if abs(math.cos(angle*2)) * dist < 5:
                lvl = (math.sin(dist*0.8) + 1) * 0.5
                if lvl > 0.7: px[x,y] = P['core']
                elif lvl > 0.4: px[x,y] = P['crystal']
                else: px[x,y] = P['main']
    diamond_shape(px, 6.5, 9.5, 4, P['white'], P['energy'])
    return img

def make_berry():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Small round berry with crystal stem
    for y in range(5, 12):
        for x in range(5, 11):
            d = math.sqrt((x-7.5)**2 + (y-8)**2)
            if d < 3.5: px[x,y] = P['crystal']
            if d < 2.5: px[x,y] = P['energy']
    # Highlight spot
    px[6,6] = P['white']; px[7,7] = P['glow']
    # Stem
    for y in range(2, 6): px[7,y] = P['main']
    # Small leaf (connected to stem at (8,4))
    px[8,4] = P['main']; px[9,4] = P['mid_dark']; px[10,4] = P['main']; px[11,3] = P['main']
    return img

def make_catalyst():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Orb shape with swirling energy
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-7.5)**2 + (y-7.5)**2)
            swirl = math.sin(d*2 + math.atan2(y-7.5, x-7.5)*3)
            if d < 6:
                if swirl > 0.5: px[x,y] = P['energy']
                elif swirl > 0: px[x,y] = P['crystal']
                else: px[x,y] = P['core']
    diamond_shape(px, 7.5, 7.5, 5, P['white'], P['glow'])
    # Orbital ring
    for a in range(0, 360, 30):
        ax = int(7.5 + math.cos(math.radians(a))*5)
        ay = int(7.5 + math.sin(math.radians(a))*5)
        if 0<=ax<16 and 0<=ay<16: px[ax,ay] = P['energy']
    return img

def make_amulet():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Crystal inlay (rendered first, once)
    diamond_shape(px, 7.5, 8.5, 6, P['white'], P['energy'], P['crystal'])
    # Gold frame — top band (y=3-5) and bottom tip (y=12-14)
    for y in range(3, 15):
        for x in range(3, 13):
            if y < 6:
                d = abs(x-7.5)
                if d < 3: px[x,y] = P['gold'] if (x+y)%2==0 else P['gold_light']
            elif y >= 12:
                if abs(x-7.5) < 1.5: px[x,y] = P['gold']
    # Chain
    for x in range(6, 10): px[x,0] = P['gold_light']; px[x,1] = P['gold']
    px[7,2] = P['gold']; px[8,2] = P['gold']
    return img

def make_tome():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Book shape with crystal cover
    for y in range(2, 15):
        for x in range(3, 14):
            if x in (3,13) or y in (2,14):
                px[x,y] = P['shadow']
            else:
                px[x,y] = P['mid_dark']
    # Spine
    for y in range(3, 14): px[4,y] = P['main']
    # Crystal gem on cover
    diamond_shape(px, 8.5, 8, 4, P['white'], P['energy'], P['crystal'])
    # Page edges (right)
    for y in range(3, 14):
        if y%2==0: px[12,y] = P['crystal']  # glowing pages
    return img

def make_fragment():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    # Broken crystal fragment shape
    cx, cy = 7.5, 6.5
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-cx)**2 + (y-cy)**2)
            angle = math.atan2(y-cy, x-cx)
            jagged = math.sin(angle*5) * 2 + 4
            if d < jagged:
                lvl = (math.sin(d*1.5) + 1) * 0.5
                if lvl > 0.7: px[x,y] = P['core']
                elif lvl > 0.4: px[x,y] = P['crystal']
                else: px[x,y] = P['main']
    # Bright edge
    for y in range(16):
        for x in range(16):
            if px[x,y] != (0,0,0,0):
                for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                    nx,ny = x+dx, y+dy
                    if 0<=nx<16 and 0<=ny<16 and px[nx,ny] == (0,0,0,0):
                        px[x,y] = P['energy']
    # Glow highlight
    px[7,6] = P['white']; px[8,6] = P['glow']
    return img

items = [
    ('void_heart', make_void_heart), ('echo_core', make_echo_core),
    ('void_key', make_void_key), ('void_alloy_ingot', make_alloy_ingot),
    ('crystal_shard', make_crystal_shard), ('crystal_berry', make_berry),
    ('void_catalyst', make_catalyst), ('echo_amulet', make_amulet),
    ('echo_tome', make_tome), ('rift_fragment', make_fragment),
]
for name, fn in items:
    img = fn()
    path = os.path.join(BASE, f'{name}.png')
    img.save(path)
    colors = set()
    for y in range(16):
        for x in range(16):
            colors.add(img.getpixel((x, y)))
    print(f'{name}.png: {len(colors)-1} colors (+alpha), {os.path.getsize(path)} bytes')
print(f'Done - {len(items)} items')
