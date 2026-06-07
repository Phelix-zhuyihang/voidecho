package com.example.voidecho;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class VoidAmbientParticle extends SpriteBillboardParticle {

    protected VoidAmbientParticle(ClientWorld world, double x, double y, double z,
                                  double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.scale = 0.2f + random.nextFloat() * 0.3f;
        this.maxAge = 40 + random.nextInt(20);
        this.alpha = 0.6f;
        this.setSpriteForAge(spriteProvider);
        this.red = 0.3f;
        this.green = 0.0f;
        this.blue = 0.5f;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = (float) this.age / this.maxAge * 0.8f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new VoidAmbientParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
        }
    }
}
