package slimeknights.tconstruct.library.client.armor.texture;

import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Armor texture supplier that returns the first matching option
 */
public record FirstArmorTextureSupplier(List<ArmorTextureSupplier> options) implements ArmorTextureSupplier {
    public static final RecordLoadable<FirstArmorTextureSupplier> LOADER = RecordLoadable.create(ArmorTextureSupplier.LOADER.list(2).requiredField("options", f -> f.options), FirstArmorTextureSupplier::new);

    public FirstArmorTextureSupplier(ArmorTextureSupplier... options) {
        this(List.of(options));
    }

    @Override
    public ArmorTexture getArmorTexture(ItemStack stack, TextureType textureType) {
        for (ArmorTextureSupplier supplier : this.options) {
            ArmorTexture texture = supplier.getArmorTexture(stack, textureType);
            if (texture != ArmorTexture.EMPTY) {
                return texture;
            }
        }
        return ArmorTexture.EMPTY;
    }

    @Override
    public RecordLoadable<FirstArmorTextureSupplier> getLoader() {
        return LOADER;
    }
}