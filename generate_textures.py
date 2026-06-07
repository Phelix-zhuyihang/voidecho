"""Generate placeholder textures for Void Echo Phase 3 features."""

from PIL import Image, ImageDraw
import os

TEXTURE_DIR = "E:/phelix/void_echo_mod/src/main/resources/assets/void_echo/textures"

def create_block_texture(name, base_color, glow_color, pattern="solid"):
    """Create a 16x16 block texture."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    if pattern == "solid":
        draw.rectangle([0, 0, 15, 15], fill=base_color)
        # Add some noise/variation
        for i in range(20):
            x, y = i % 16, (i * 3) % 16
            r = min(255, base_color[0] + (i * 10) % 40 - 20)
            g = min(255, base_color[1] + (i * 7) % 30 - 15)
            b = min(255, base_color[2] + (i * 13) % 50 - 25)
            draw.point((x, y), fill=(r, g, b, 255))
    elif pattern == "cross":
        # Transparent background with a cross/crystal shape
        # Center glow
        draw.ellipse([4, 4, 11, 11], fill=glow_color)
        # Petals
        draw.ellipse([2, 6, 6, 10], fill=base_color)
        draw.ellipse([9, 6, 13, 10], fill=base_color)
        draw.ellipse([6, 2, 10, 6], fill=base_color)
        draw.ellipse([6, 9, 10, 13], fill=base_color)
        # Center
        draw.ellipse([5, 5, 10, 10], fill=glow_color)
    elif pattern == "forge":
        # Dark obsidian-like base
        draw.rectangle([0, 0, 15, 15], fill=base_color)
        # Purple glowing cracks
        for i in range(8):
            x1 = i * 2
            y1 = (i * 3) % 16
            x2 = ((i * 3) + 2) % 16
            y2 = ((i * 7) + 2) % 16
            draw.line([x1, y1, x2, y2], fill=glow_color, width=1)
        # Center glow
        draw.ellipse([3, 3, 12, 12], fill=glow_color)
        draw.ellipse([5, 5, 10, 10], fill=(255, 255, 255, 200))

    # Ensure directory exists
    os.makedirs(f"{TEXTURE_DIR}/block", exist_ok=True)
    img.save(f"{TEXTURE_DIR}/block/{name}.png")
    print(f"Created block texture: {name}.png")

def create_item_texture(name, base_color, glow_color):
    """Create a 16x16 item texture."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Crystal shard-like shape
    draw.polygon([(8, 0), (12, 6), (10, 14), (6, 14), (4, 6)], fill=base_color)
    # Inner glow
    draw.polygon([(8, 3), (10, 7), (9, 12), (7, 12), (6, 7)], fill=glow_color)
    # Top highlight
    draw.polygon([(8, 1), (10, 5), (9, 4), (7, 4), (6, 5)], fill=(255, 255, 255, 180))

    os.makedirs(f"{TEXTURE_DIR}/item", exist_ok=True)
    img.save(f"{TEXTURE_DIR}/item/{name}.png")
    print(f"Created item texture: {name}.png")

def create_entity_texture(name, base_color, glow_color):
    """Create a 32x32 entity texture."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Simple body shape
    draw.ellipse([8, 4, 24, 20], fill=base_color)
    # Wings
    draw.polygon([(8, 8), (0, 4), (4, 12), (8, 16)], fill=glow_color)
    draw.polygon([(24, 8), (32, 4), (28, 12), (24, 16)], fill=glow_color)
    # Eyes
    draw.ellipse([12, 8, 15, 11], fill=(255, 255, 255, 220))
    draw.ellipse([17, 8, 20, 11], fill=(255, 255, 255, 220))
    # Inner glow
    draw.ellipse([13, 9, 14, 10], fill=(200, 255, 255, 255))
    draw.ellipse([18, 9, 19, 10], fill=(200, 255, 255, 255))

    os.makedirs(f"{TEXTURE_DIR}/entity", exist_ok=True)
    img.save(f"{TEXTURE_DIR}/entity/{name}.png")
    print(f"Created entity texture: {name}.png")


if __name__ == "__main__":
    # Block textures
    create_block_texture("crystal_bloom",
                         base_color=(100, 200, 255, 220),
                         glow_color=(200, 230, 255, 255),
                         pattern="cross")
    create_block_texture("void_forge",
                         base_color=(30, 10, 40, 255),
                         glow_color=(150, 50, 200, 180),
                         pattern="forge")
    # Item textures
    create_item_texture("rift_fragment",
                        base_color=(120, 40, 180, 255),
                        glow_color=(200, 100, 255, 220))
    # Entity textures
    create_entity_texture("crystal_sprite",
                          base_color=(100, 200, 255, 180),
                          glow_color=(200, 230, 255, 120))
    print("All textures generated!")
