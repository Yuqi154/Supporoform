package slimeknights.tconstruct.world.data;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.data.AbstractStructureRepalleter;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.block.DirtType;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.Objects;

public class StructureRepalleter extends AbstractStructureRepalleter {

    public StructureRepalleter(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, TConstruct.MOD_ID);
    }

    @Override
    public void addStructures() {
        String[] sizes = {"0x1x0", "2x2x4", "4x1x6", "8x1x11", "11x1x11"};

        // slime islands have 2 blocks to replace: minecraft:grass_block and minecraft:dirt

        // earth foliage with earth or sky dirt
        Replacement earth = this.replacement().addMapping(Blocks.CLAY, TinkerWorld.slimeDirt.get(DirtType.EARTH))
                .addMapping(Blocks.SAND, TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                .addMapping(Blocks.WATER, Objects.requireNonNull(TinkerFluids.earthSlime.getBlock()));
        this.repalette(sizes, "islands/earth/", false,
                earth.copy().addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.EARTH))
                        .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.earthSlimeGrass.get(FoliageType.EARTH)),
                earth.copy().addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.SKY))
                        .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.skySlimeGrass.get(FoliageType.EARTH)));
        // sky foliage with earth or sky dirt
        Replacement sky = this.replacement().addMapping(Blocks.CLAY, TinkerWorld.slimeDirt.get(DirtType.SKY))
                .addMapping(Blocks.SAND, TinkerWorld.congealedSlime.get(SlimeType.SKY))
                .addMapping(Blocks.WATER, Objects.requireNonNull(TinkerFluids.skySlime.getBlock()));
        this.repalette(sizes, "islands/sky/", false,
                sky.copy().addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.EARTH))
                        .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.earthSlimeGrass.get(FoliageType.SKY)),
                sky.copy().addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.SKY))
                        .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.skySlimeGrass.get(FoliageType.SKY)));
        // blood
        this.repalette(sizes, "islands/blood/", false, this.replacement()
                .addMapping(Blocks.CLAY, Blocks.MAGMA_BLOCK)
                .addMapping(Blocks.SAND, TinkerWorld.congealedSlime.get(SlimeType.ICHOR))
                .addMapping(Blocks.WATER, Objects.requireNonNull(TinkerFluids.magma.getBlock()))
                .addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.ICHOR))
                .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.ichorSlimeGrass.get(FoliageType.BLOOD)));
        // ender
        this.repalette(sizes, "islands/ender/", true, this.replacement()
                .addMapping(Blocks.CLAY, TinkerWorld.slimeDirt.get(DirtType.ENDER))
                .addMapping(Blocks.SAND, TinkerWorld.congealedSlime.get(SlimeType.ENDER))
                .addMapping(Blocks.WATER, Objects.requireNonNull(TinkerFluids.enderSlime.getBlock()))
                .addMapping(Blocks.DIRT, TinkerWorld.slimeDirt.get(DirtType.ENDER))
                .addMapping(Blocks.GRASS_BLOCK, TinkerWorld.enderSlimeGrass.get(FoliageType.ENDER)));
    }

    /**
     * Replaettes all sizes from the given list
     */
    private void repalette(String[] sizes, String target, boolean reprocess, Replacement... replacements) {
        for (String size : sizes) {
            this.repalette(TConstruct.getResource("islands/dirt/" + size), target + size, reprocess, replacements);
        }
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Structure Repaletter";
    }
}
