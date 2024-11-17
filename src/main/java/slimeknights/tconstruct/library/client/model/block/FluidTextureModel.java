package slimeknights.tconstruct.library.client.model.block;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import lombok.AllArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.RetexturedModel.RetexturedContext;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.ColoredBlockModel.ColorData;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.LogicHelper;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.IDisplayFluidListener;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Model that replaces fluid textures with the fluid from model data
 */
@AllArgsConstructor
public class FluidTextureModel implements IUnbakedGeometry<FluidTextureModel> {
    /**
     * Loader instance
     */
    public static final IGeometryLoader<FluidTextureModel> LOADER = FluidTextureModel::deserialize;

    private final ColoredBlockModel model;
    private final Set<String> fluids;
    private final Set<String> retextured;

    @Override
    public Collection<SpriteIdentifier> getMaterials(JsonUnbakedModel owner, Function<Identifier, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return model.getMaterials(owner, modelGetter, missingTextureErrors);
    }

    /**
     * Trims the # character off the beginning of a texture name (if present)
     */
    private static String trimTextureName(String name) {
        if (name.charAt(0) == '#') {
            return name.substring(1);
        }
        return name;
    }

    @Override
    public BakedModel bake(JsonUnbakedModel owner, Baker bakery, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings transform, ModelOverrideList overrides, Identifier modelLocation,boolean b) {
        // start by baking the model, handing UV lock
        BakedModel baked = model.bake(owner, bakery, spriteGetter, transform, overrides, modelLocation);

        // determine which block parts are fluids
        Set<String> fluidTextures = this.fluids.isEmpty() ? Collections.emptySet() : RetexturedModel.getAllRetextured(owner, model, this.fluids);
        List<ModelElement> elements = model.getElements();
        int size = elements.size();
        BitSet fluidParts = new BitSet(size);
        if (!fluidTextures.isEmpty()) {
            for (int i = 0; i < size; i++) {
                ModelElement part = elements.get(i);
                long fluidFaces = part.faces.values().stream()
                        .filter(face -> fluidTextures.contains(trimTextureName(face.textureId)))
                        .count();
                // for simplicity, each part is either a fluid or not. If for some reason it contains both we mark it as a fluid, meaning it may get colored
                // if this is undesired, just use separate elements
                if (fluidFaces > 0) {
                    if (fluidFaces < part.faces.size()) {
                        TConstruct.LOG.warn("Mixed fluid and non-fluid elements in model {}, may cause unexpected results", modelLocation);
                    }
                    fluidParts.set(i);
                }
            }
        }
        Set<String> retextured = this.retextured.isEmpty() ? Collections.emptySet() : RetexturedModel.getAllRetextured(owner, this.model, this.retextured);
        return new Baked(baked, elements, model.getColorData(), owner, transform, fluidTextures, fluidParts, retextured);
    }

    private record BakedCacheKey(FluidStack fluid, @Nullable Identifier texture) {
    }

    /**
     * Baked wrapper class
     */
    private static class Baked extends DynamicBakedWrapper<BakedModel> {
        private final Map<BakedCacheKey, BakedModel> cache = new ConcurrentHashMap<>();
        private final List<ModelElement> elements;
        private final List<ColorData> colorData;
        private final JsonUnbakedModel owner;
        private final ModelBakeSettings transform;
        private final Set<String> fluids;
        private final BitSet fluidParts;
        private final Set<String> retextured;

        protected Baked(BakedModel originalModel, List<ModelElement> elements, List<ColorData> colorData, JsonUnbakedModel owner, ModelBakeSettings transform, Set<String> fluids, BitSet fluidParts, Set<String> retextured) {
            super(originalModel);
            this.elements = elements;
            this.colorData = colorData;
            this.owner = owner;
            this.transform = transform;
            this.fluids = fluids;
            this.fluidParts = fluidParts;
            this.retextured = retextured;
        }

        /**
         * Retextures a model for the given fluid
         */
        private BakedModel getRetexturedModel(BakedCacheKey key) {
            // setup model baking
            Function<SpriteIdentifier, Sprite> spriteGetter = SpriteIdentifier::getSprite;

            // if textured, retexture. Its fine to nest these configurations
            JsonUnbakedModel textured = this.owner;
            if (key.texture != null) {
                textured = new RetexturedContext(textured, this.retextured, key.texture);
            }

            // setup transformers, quadTransformer will be applied to all parts while fluid also adds in colors for the fluid
            IQuadTransformer quadTransformer = SimpleBlockModel.applyTransform(transform, owner.getRootTransform());
            IQuadTransformer fluidTransformer = quadTransformer;

            // get fluid details if needed
            int luminosity = 0;
            if (!key.fluid.isEmpty()) {
                IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(key.fluid.getFluid());
                int color = attributes.getTintColor(key.fluid);
                if (color != -1) {
                    fluidTransformer = ColoredBlockModel.applyColorQuadTransformer(color).andThen(quadTransformer);
                }
                luminosity = key.fluid.getFluid().getFluidType().getLightLevel(key.fluid);
                textured = new RetexturedContext(textured, this.fluids, attributes.getStillTexture(key.fluid));
            }

            // start baking
            Sprite particle = spriteGetter.apply(textured.getMaterial("particle"));
            BasicBakedModel.Builder builder = SimpleBlockModel.bakedBuilder(owner, ModelOverrideList.EMPTY).particle(particle);

            boolean defaultUvLock = transform.isUvLocked();
            int size = elements.size();
            for (int i = 0; i < size; i++) {
                ModelElement element = elements.get(i);
                ColorData colors = LogicHelper.getOrDefault(colorData, i, ColorData.DEFAULT);
                if (fluidParts.get(i)) {
                    ColoredBlockModel.bakePart(builder, textured, element, luminosity, spriteGetter, transform.getRotation(), fluidTransformer, colors.isUvLock(defaultUvLock), TankModel.BAKE_LOCATION);
                } else {
                    int partColor = colors.color();
                    IQuadTransformer partTransformer = partColor == -1 ? quadTransformer : ColoredBlockModel.applyColorQuadTransformer(partColor).andThen(quadTransformer);
                    ColoredBlockModel.bakePart(builder, textured, element, colors.luminosity(), spriteGetter, transform.getRotation(), partTransformer, colors.isUvLock(defaultUvLock), TankModel.BAKE_LOCATION);
                }
            }
            return builder.build(SimpleBlockModel.getRenderTypeGroup(owner));
        }

        /**
         * Gets a retextured model for the given fluid, using the cached model if possible
         */
        private BakedModel getCachedModel(BakedCacheKey key) {
            return this.cache.computeIfAbsent(key, this::getRetexturedModel);
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, ModelData data, @Nullable RenderLayer renderType) {
            FluidStack fluid = fluids.isEmpty() ? FluidStack.EMPTY : data.get(IDisplayFluidListener.PROPERTY);
            if (fluid == null) {
                fluid = FluidStack.EMPTY;
            }
            Block block = retextured.isEmpty() ? null : data.get(RetexturedHelper.BLOCK_PROPERTY);
            if (!fluid.isEmpty() || block != null) {
                BakedCacheKey key = new BakedCacheKey(fluid, block != null ? ModelHelper.getParticleTexture(block) : null);
                return getCachedModel(key).getQuads(state, direction, random, data, renderType);
            }
            return originalModel.getQuads(state, direction, random, data, renderType);
        }

        @Override
        public ModelOverrideList getOverrides() {
            return RetexturedOverride.INSTANCE;
        }
    }

    /**
     * Deserializes this model from JSON
     */
    public static FluidTextureModel deserialize(JsonObject json, JsonDeserializationContext context) {
        ColoredBlockModel model = ColoredBlockModel.deserialize(json, context);
        Set<String> fluids = Collections.emptySet();
        if (json.has("fluids")) {
            fluids = ImmutableSet.copyOf(JsonHelper.parseList(json, "fluids", net.minecraft.util.JsonHelper::asString));
        }
        Set<String> retextured = Collections.emptySet();
        if (json.has("retextured")) {
            retextured = ImmutableSet.copyOf(JsonHelper.parseList(json, "retextured", net.minecraft.util.JsonHelper::asString));
        }
        return new FluidTextureModel(model, fluids, retextured);
    }

    /**
     * Override list to swap the texture in from NBT
     */
    private static class RetexturedOverride extends ModelOverrideList {
        private static final RetexturedOverride INSTANCE = new RetexturedOverride();

        @Nullable
        @Override
        public BakedModel apply(BakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int pSeed) {
            if (stack.isEmpty() || !stack.hasNbt()) {
                return originalModel;
            }

            // get the block first, ensuring its valid
            Block block = RetexturedBlockItem.getTexture(stack);
            if (block == Blocks.AIR) {
                return originalModel;
            }

            // if valid, use the block
            return ((Baked) originalModel).getCachedModel(new BakedCacheKey(FluidStack.EMPTY, ModelHelper.getParticleTexture(block)));
        }
    }
}