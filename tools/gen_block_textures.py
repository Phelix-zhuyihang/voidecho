from PIL import Image
import os, random, math

BASE = r'E:\phelix\void_echo_mod\src\main\resources\assets\void_echo\textures\block'
random.seed(42)

P = {
    'darkest':  (14,  2,  20, 255), 'shadow':   (26,  6,  40, 255),
    'mid_dark': (45,  15, 69, 255), 'main':     (69,  32,  104, 255),
    'light':    (92,  45,  133, 255), 'highlight':(123, 64,  168, 255),
    'crystal':  (155, 95, 192, 255), 'core':     (184, 120, 216, 255),
    'energy':   (212, 144, 240, 255), 'glow':     (232, 192, 255, 255),
    'white':    (240, 224, 255, 255), 'outline':  (10,  1,   18,  255),
    'dirt_dark':(30,  8,  22, 255),  'dirt_mid': (50,  15, 35, 255),
    'grass_top':(55,  30, 80, 255),  'grass_side':(40, 15, 55, 255),
    'ore_dark': (35,  10, 50, 255),  'ore_mid':  (60,  20, 80, 255),
}

def noise(x, y, scale=0.3):
    return math.sin(x*scale + y*scale*1.3) * math.cos(y*scale*0.7 - x*scale*0.5)

def make_void_stone():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            n = noise(x, y, 0.8)
            if n > 0.2: px[x,y] = P['main']
            elif n > -0.2: px[x,y] = P['mid_dark']
            else: px[x,y] = P['darkest']
    for _ in range(8):
        x, y = random.randint(2,13), random.randint(2,13)
        px[x,y] = P['light']
    return img

def make_bricks():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['main']
    for y in range(16):
        for x in range(16):
            if y in (0,5,10,15) or x in (0,7,8,15):
                px[x,y] = P['shadow']
    for y in range(5, 10):
        px[7,y] = P['shadow']; px[8,y] = P['shadow']
    for _ in range(6):
        x, y = random.randint(2,6), random.randint(1,4)
        px[x,y] = P['mid_dark']
    return img

def make_cracked_bricks():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['main']
    for y in range(16):
        for x in range(16):
            if y in (0,5,10,15) or x in (0,7,8,15):
                px[x,y] = P['shadow']
    cracks = [(2,2),(3,3),(4,3),(5,2), (10,5),(11,6),(12,6), (3,7),(4,8),(5,7),(6,6),
              (9,10),(10,11),(11,11),(12,10), (2,11),(3,12),(4,12),(5,13)]
    for x,y in cracks:
        if 0<=x<16 and 0<=y<16:
            px[x,y] = P['darkest']
    for y in range(1, 5): px[8,y] = P['darkest']
    return img

def make_dirt():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            n = noise(x, y, 0.6)
            if n > 0.3: px[x,y] = P['dirt_mid']
            elif n > 0: px[x,y] = P['dirt_dark']
            else: px[x,y] = P['darkest']
    for _ in range(12):
        px[random.randint(2,13), random.randint(2,13)] = P['crystal'] if random.random() < 0.2 else P['dirt_mid']
    return img

def make_grass_top():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            if y < 4:
                px[x,y] = P['grass_top'] if noise(x,y,0.9) > -0.1 else P['main']
            else:
                px[x,y] = P['dirt_dark'] if noise(x,y,0.6) > 0 else P['darkest']
    for x in range(16):
        if noise(x, 0, 0.7) > 0: px[x,0] = P['crystal']
        if noise(x, 1, 0.7) > 0.2: px[x,1] = P['light']
    for _ in range(8):
        px[random.randint(1,14), random.randint(3,15)] = P['dirt_mid']
    return img

def make_grass_side():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            if y < 3:
                px[x,y] = P['grass_top'] if noise(x,y,0.8) > -0.2 else P['grass_side']
            elif y < 5:
                px[x,y] = P['grass_side']
            else:
                px[x,y] = P['dirt_dark'] if noise(x,y,0.5) > 0 else P['darkest']
    for _ in range(6):
        px[random.randint(1,14), random.randint(5,15)] = P['dirt_mid']
    return img

def make_crystal_ore():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['ore_dark']
            if noise(x,y,0.7) > 0.1: px[x,y] = P['mid_dark']
    veins = [(3,4),(4,3),(5,3),(6,4),(4,4),(5,4), (10,8),(11,7),(12,7),(13,8),(11,8),(12,8),
             (5,11),(6,10),(7,10),(8,11),(6,11),(7,11), (2,7),(3,7),(3,8)]
    for x,y in veins:
        px[x,y] = P['crystal']
    bright = [(4,3),(5,4),(11,7),(12,8),(6,11),(3,7)]
    for x,y in bright:
        px[x,y] = P['core']
    return img

def make_crystal_block():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['main']
    for y in range(16):
        for x in range(16):
            if (x + y) % 3 == 0:
                px[x,y] = P['crystal']
            elif (x + y) % 3 == 1:
                px[x,y] = P['core']
    for y in range(6, 10):
        for x in range(6, 10):
            d = abs(x-7.5) + abs(y-7.5)
            if d < 2: px[x,y] = P['energy']
            elif d < 3: px[x,y] = P['glow']
    for x in range(16):
        px[x,0] = P['light']; px[x,15] = P['shadow']
        px[0,x] = P['shadow']; px[15,x] = P['light']
    return img

def make_portal_top():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['darkest']
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-7.5)**2 + (y-7.5)**2)
            angle = math.atan2(y-7.5, x-7.5)
            swirl = math.sin(d*1.5 + angle*2) * math.cos(d*0.5)
            if swirl > 0.3: px[x,y] = P['main']
            elif swirl > 0: px[x,y] = P['mid_dark']
    for y in range(6, 10):
        for x in range(6, 10):
            if abs(x-7.5) + abs(y-7.5) < 1.5:
                px[x,y] = P['energy']
    for i in range(16):
        px[i,0] = P['outline']; px[i,15] = P['outline']
        px[0,i] = P['outline']; px[15,i] = P['outline']
    return img

def make_portal_side():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['darkest']
    for y in range(16):
        for x in range(16):
            n = noise(x, y, 1.5)
            if n > 0.3: px[x,y] = P['shadow']
            elif n > -0.3: px[x,y] = P['mid_dark']
    for _ in range(5):
        vx, vy = random.randint(2,13), int(noise(_, 0)*8 + 8)
        for i in range(3):
            if 0<=vy+i<16: px[vx, vy+i] = P['crystal'] if i%2==0 else P['energy']
    return img

def make_altar_top():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['shadow']
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-7.5)**2 + (y-7.5)**2)
            if d < 5.5: px[x,y] = P['main']
            if d < 3: px[x,y] = P['mid_dark']
    for y in range(5, 11):
        for x in range(5, 11):
            d = math.sqrt((x-7.5)**2 + (y-7.5)**2)
            if d < 2.5:
                px[x,y] = P['crystal'] if (x+y)%2==0 else P['core']
    px[7,7] = P['energy']; px[8,7] = P['glow']; px[7,8] = P['glow']; px[8,8] = P['energy']
    for ox,oy in [(2,2),(13,2),(2,13),(13,13)]:
        for dy in range(2):
            for dx in range(2):
                px[ox+dx,oy+dy] = P['crystal']
    return img

def make_altar_side():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['shadow']
    for y in range(16):
        for x in range(16):
            if y in (0, 7, 8, 15):
                px[x,y] = P['crystal']
            elif y < 8:
                px[x,y] = P['main'] if noise(x,y,1.0)>0 else P['mid_dark']
            else:
                px[x,y] = P['mid_dark'] if noise(x,y,0.8)>0 else P['shadow']
    return img

def make_crystal_bloom():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    cx, cy = 7.5, 6.5
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-cx)**2 + (y-cy)**2)
            if d < 3:
                px[x,y] = P['crystal'] if d < 2 else P['energy']
    for angle_deg in [0, 72, 144, 216, 288]:
        a = math.radians(angle_deg)
        for r in range(2, 5):
            px_i = int(cx + math.cos(a)*r)
            py_i = int(cy + math.sin(a)*r)
            if 0 <= px_i < 16 and 0 <= py_i < 16:
                px[px_i, py_i] = P['core'] if r < 4 else P['glow']
    for y in range(10, 16):
        px[7,y] = P['mid_dark']; px[8,y] = P['mid_dark']
    return img

def make_forge():
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    for y in range(16):
        for x in range(16):
            px[x,y] = P['darkest']
    for y in range(3, 14):
        for x in range(3, 13):
            px[x,y] = P['shadow']
    for y in range(5, 10):
        for x in range(5, 11):
            px[x,y] = P['energy'] if (x+y)%2==0 else P['core']
    for y in range(6, 9):
        for x in range(6, 10):
            px[x,y] = P['white']
    for y in range(2, 4):
        for x in range(3, 13):
            px[x,y] = P['main']
    for ox,oy in [(2,2),(13,2),(2,13),(13,13)]:
        px[ox,oy] = P['crystal']
    return img

def make_shard(n):
    """Generate echo_shard_N texture with number-based variation"""
    img = Image.new('RGBA', (16, 16), (0,0,0,0)); px = img.load()
    colors = [P['main'], P['light'], P['crystal'], P['core'], P['energy'], P['highlight']]
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = math.sqrt((x-cx)**2 + (y-cy)**2)
            angle = math.atan2(y-cy, x-cx) + n * 0.5
            if d < 6 + n * 0.3:
                ci = int((math.sin(angle*3 + n) + 1) * 2.5) % len(colors)
                alpha = max(0, min(255, int(255 * (1 - d/7))))
                r,g,b,a = colors[ci]
                px[x,y] = (r, g, b, min(alpha, a))
    # Bright center
    for y in range(6, 10):
        for x in range(6, 10):
            if abs(x-cx) + abs(y-cy) < 1.5:
                px[x,y] = P['glow'] if n%2==0 else P['energy']
    return img

# Save all blocks
make_fns = {
    'void_stone': make_void_stone,
    'void_stone_bricks': make_bricks,
    'cracked_void_stone_bricks': make_cracked_bricks,
    'void_dirt': make_dirt,
    'void_grass_block_top': make_grass_top,
    'void_grass_block_side': make_grass_side,
    'crystal_ore': make_crystal_ore,
    'crystal_block': make_crystal_block,
    'void_portal_frame_top': make_portal_top,
    'void_portal_frame_side': make_portal_side,
    'echo_altar_top': make_altar_top,
    'echo_altar_side': make_altar_side,
    'crystal_bloom': make_crystal_bloom,
    'void_forge': make_forge,
}
for name, fn in make_fns.items():
    img = fn()
    path = os.path.join(BASE, f'{name}.png')
    img.save(path)
    colors = set()
    for y in range(16):
        for x in range(16):
            colors.add(img.getpixel((x, y)))
    print(f'{name}.png: {len(colors)-1} colors, {os.path.getsize(path)} bytes')

# Echo shards 1-5
for n in range(1, 6):
    img = make_shard(n)
    path = os.path.join(BASE, f'echo_shard_{n}.png')
    img.save(path)
    colors = set()
    for y in range(16):
        for x in range(16):
            colors.add(img.getpixel((x, y)))
    print(f'echo_shard_{n}.png: {len(colors)-1} colors, {os.path.getsize(path)} bytes')

print(f'Done - {len(make_fns)} blocks + 5 shards')
