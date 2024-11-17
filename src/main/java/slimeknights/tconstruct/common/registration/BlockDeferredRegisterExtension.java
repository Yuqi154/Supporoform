package slimeknights.tconstruct.common.registration;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.EnumObject.Builder;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Additional methods in deferred register extension
 */
@SuppressWarnings("UnusedReturnValue")
public class BlockDeferredRegisterExtension extends BlockDeferredRegister {
    private static final AbstractBlock.Settings POTTED_PROPS = AbstractBlock.Settings.create().breakInstantly().nonOpaque();

    public BlockDeferredRegisterExtension(String modID) {
        super(modID);
    }

    /**
     * Registers a geode block
     *
     * @param name       Geode name
     * @param color      Color of the geode
     * @param blockSound Sound of the block and budding block
     * @param props      Item props
     * @return The geode block
     */
    public GeodeItemObject registerGeode(String name, MaterialColor color, BlockSoundGroup blockSound, SoundEvent chimeSound, Map<BudSize, BlockSoundGroup> clusterSounds, int baseLight, Item.Settings props) {
        RegistryObject<Item> shard = itemRegister.register(name, () -> new Item(props));
        return new GeodeItemObject(shard, this, color, blockSound, chimeSound, clusterSounds, baseLight, props);
    }

    /**
     * Registers a potted form of the given block using the vanilla pot
     *
     * @param name  Name of the flower
     * @param block Block to put in the block
     * @return Potted block instance
     */
    public RegistryObject<FlowerPotBlock> registerPotted(String name, Supplier<? extends Block> block) {
        RegistryObject<FlowerPotBlock> potted = registerNoItem("potted_" + name, () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, block, POTTED_PROPS));
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(resource(name), potted);
        return potted;
    }

    /**
     * Registers a potted form of the given block using the vanilla pot
     */
    public RegistryObject<FlowerPotBlock> registerPotted(RegistryObject<? extends Block> block) {
        return registerPotted(block.getId().getPath(), block);
    }

    /**
     * Registers a potted form of the given block using the vanilla pot
     */
    public RegistryObject<FlowerPotBlock> registerPotted(ItemObject<? extends Block> block) {
        return registerPotted(block.getId().getPath(), block);
    }

    /**
     * Registers a potted form of the given block using the vanilla pot
     */
    public <T extends Enum<T> & StringIdentifiable, B extends Block> EnumObject<T, FlowerPotBlock> registerPottedEnum(T[] values, String name, EnumObject<T, B> block) {
        Builder<T, FlowerPotBlock> builder = new Builder<>(values[0].getDeclaringClass());
        for (T value : values) {
            Supplier<? extends B> supplier = block.getSupplier(value);
            if (supplier != null) {
                builder.put(value, registerPotted(value.asString() + "_" + name, supplier));
            }
        }
        return builder.build();
    }
}