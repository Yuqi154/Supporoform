package slimeknights.tconstruct.library.client.data.material;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataWriter;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider.MaterialSpriteInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoJson;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoJson.MaterialGeneratorJson;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Base data generator for use in addons
 */
@SuppressWarnings("unused")  // API
public abstract class AbstractMaterialRenderInfoProvider extends GenericDataProvider {
    /**
     * Map of material ID to builder, there is at most one builder for each ID
     */
    private final Map<MaterialVariantId, RenderInfoBuilder> allRenderInfo = new HashMap<>();
    @Nullable
    private final AbstractMaterialSpriteProvider materialSprites;
    @Nullable
    private final ExistingFileHelper existingFileHelper;

    public AbstractMaterialRenderInfoProvider(DataGenerator gen, @Nullable AbstractMaterialSpriteProvider materialSprites, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, ResourceType.CLIENT_RESOURCES, MaterialRenderInfoLoader.FOLDER, MaterialRenderInfoLoader.GSON);
        this.materialSprites = materialSprites;
        this.existingFileHelper = existingFileHelper;
    }

    public AbstractMaterialRenderInfoProvider(DataGenerator gen) {
        this(gen, null, null);
    }

    /**
     * Adds all relevant material stats
     */
    protected abstract void addMaterialRenderInfo();

    @Override
    public void run(DataWriter cache) {
        if (this.existingFileHelper != null) {
            MaterialPartTextureGenerator.runCallbacks(this.existingFileHelper, null);
        }
        this.addMaterialRenderInfo();
        // generate
        this.allRenderInfo.forEach((materialId, info) -> this.saveJson(cache, materialId.getLocation('/'), info.build()));
        if (this.existingFileHelper != null) {
            MaterialPartTextureGenerator.runCallbacks(null, null);
        }
    }


    /* Helpers */

    /**
     * Initializes a builder for the given material
     */
    private RenderInfoBuilder getBuilder(Identifier texture) {
        RenderInfoBuilder builder = new RenderInfoBuilder();
        if (this.materialSprites != null) {
            MaterialSpriteInfo spriteInfo = this.materialSprites.getMaterialInfo(texture);
            if (spriteInfo != null) {
                String[] fallbacks = spriteInfo.getFallbacks();
                if (fallbacks.length > 0) {
                    builder.fallbacks(fallbacks);
                }
                // colors are in AABBGGRR format, we want AARRGGBB, so swap red and blue
                int color = spriteInfo.getTransformer().getFallbackColor();
                if (color != 0xFFFFFFFF) {
                    builder.color((color & 0x00FF00) | ((color >> 16) & 0x0000FF) | ((color << 16) & 0xFF0000));
                }
                builder.generator(spriteInfo);
            }
        }
        return builder;
    }

    /**
     * Starts a builder for a general render info
     */
    protected RenderInfoBuilder buildRenderInfo(MaterialVariantId materialId) {
        return this.allRenderInfo.computeIfAbsent(materialId, id -> this.getBuilder(materialId.getLocation('_')));
    }

    /**
     * Starts a builder for a general render info with an overridden texture.
     * Use {@link #buildRenderInfo(MaterialVariantId)} if you plan to override the texture without copying the datagen settings
     */
    protected RenderInfoBuilder buildRenderInfo(MaterialVariantId materialId, Identifier texture) {
        return this.allRenderInfo.computeIfAbsent(materialId, id -> this.getBuilder(texture).texture(texture));
    }

    @Accessors(fluent = true, chain = true)
    protected static class RenderInfoBuilder {
        @Setter
        private Identifier texture = null;
        private String[] fallbacks;
        private int color = -1;
        @Setter
        private boolean skipUniqueTexture;
        @Setter
        private int luminosity = 0;
        @Setter
        private MaterialGeneratorJson generator = null;

        /**
         * Sets the color
         */
        public RenderInfoBuilder color(int color) {
            if ((color & 0xFF000000) == 0) {
                color |= 0xFF000000;
            }
            this.color = color;
            return this;
        }

        /**
         * Sets the fallback names
         */
        public RenderInfoBuilder fallbacks(@Nullable String... fallbacks) {
            this.fallbacks = fallbacks;
            return this;
        }

        /**
         * Sets the texture from another material variant
         */
        public RenderInfoBuilder materialTexture(MaterialVariantId variantId) {
            return this.texture(variantId.getLocation('_'));
        }

        /**
         * Builds the material
         */
        public MaterialRenderInfoJson build() {
            return new MaterialRenderInfoJson(this.texture, this.fallbacks, ColorLoadable.ALPHA.getString(this.color), this.skipUniqueTexture ? Boolean.TRUE : null, this.luminosity, this.generator);
        }
    }
}
