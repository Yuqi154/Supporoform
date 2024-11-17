package slimeknights.tconstruct.library.client.particle;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;

import org.jetbrains.annotations.Nullable;

// not part of the tic particle system since it uses vanilla particles
public class SlimeParticle extends CrackParticle {

    public SlimeParticle(ClientWorld worldIn, double posXIn, double posYIn, double posZIn, ItemStack stack) {
        super(worldIn, posXIn, posYIn, posZIn, stack);
    }

    public SlimeParticle(ClientWorld worldIn, double posXIn, double posYIn, double posZIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, ItemStack stack) {
        super(worldIn, posXIn, posYIn, posZIn, xSpeedIn, ySpeedIn, zSpeedIn, stack);
    }

    @RequiredArgsConstructor
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final ItemConvertible slime;

        public Factory(SlimeType type) {
            this.slime = TinkerCommons.slimeball.get(type);
        }

        @Nullable
        @Override
        public Particle createParticle(DefaultParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SlimeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(slime));
        }
    }
}