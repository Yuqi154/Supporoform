package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.function.Consumer;

@SuppressWarnings({"unused", "WeakerAccess"})
@RequiredArgsConstructor(staticName = "castingRecipe")
public class MaterialCastingRecipeBuilder extends AbstractRecipeBuilder<MaterialCastingRecipeBuilder> {
    private final IMaterialItem result;
    private final IModifiable resultTool;
    private final TypeAwareRecipeSerializer<? extends AbstractMaterialCastingRecipe> recipeSerializer;
    private Ingredient cast = Ingredient.EMPTY;
    @Setter
    @Accessors(chain = true)
    private int itemCost = 0;
    private boolean consumed = false;
    private boolean switchSlots = false;

    /**
     * Creates a new material casting recipe for an basin recipe
     *
     * @param result Material item result
     * @return Builder instance
     */
    public static MaterialCastingRecipeBuilder basinRecipe(IMaterialItem result) {
        return castingRecipe(result, null, TinkerSmeltery.basinMaterialSerializer.get());
    }

    /**
     * Creates a new material casting recipe for an table recipe
     *
     * @param result Material item result
     * @return Builder instance
     */
    public static MaterialCastingRecipeBuilder tableRecipe(IMaterialItem result) {
        return castingRecipe(result, null, TinkerSmeltery.tableMaterialSerializer.get());
    }

    /**
     * Creates a new material casting recipe for an basin recipe
     *
     * @param result Material item result
     * @return Builder instance
     */
    public static MaterialCastingRecipeBuilder basinRecipe(IModifiable result) {
        return castingRecipe(null, result, TinkerSmeltery.basinToolSerializer.get());
    }

    /**
     * Creates a new material casting recipe for an table recipe
     *
     * @param result Material item result
     * @return Builder instance
     */
    public static MaterialCastingRecipeBuilder tableRecipe(IModifiable result) {
        return castingRecipe(null, result, TinkerSmeltery.tableToolSerializer.get());
    }

    /**
     * Sets the cast to the given tag
     *
     * @param tag      Cast tag
     * @param consumed If true, cast is consumed
     * @return Builder instance
     */
    public MaterialCastingRecipeBuilder setCast(TagKey<Item> tag, boolean consumed) {
        return this.setCast(Ingredient.fromTag(tag), consumed);
    }

    /**
     * Sets the cast to the given item
     *
     * @param item     Cast item
     * @param consumed If true, cast is consumed
     * @return Builder instance
     */
    public MaterialCastingRecipeBuilder setCast(ItemConvertible item, boolean consumed) {
        return this.setCast(Ingredient.ofItems(item), consumed);
    }

    /**
     * Set the cast to the given ingredient
     *
     * @param cast     Ingredient
     * @param consumed If true, cast is consumed
     * @return Builder instance
     */
    public MaterialCastingRecipeBuilder setCast(Ingredient cast, boolean consumed) {
        this.cast = cast;
        this.consumed = consumed;
        return this;
    }

    /**
     * Set output of recipe to be put into the input slot.
     * Mostly used for cast creation
     */
    public MaterialCastingRecipeBuilder setSwitchSlots() {
        this.switchSlots = true;
        return this;
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer) {
        this.save(consumer, Registries.ITEM.getId(this.result.asItem()));
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer, Identifier id) {
        if (this.itemCost <= 0) {
            throw new IllegalStateException("Material casting recipes require a positive amount of fluid");
        }
        Identifier advancementId = this.buildOptionalAdvancement(id, "casting");
        if (result != null) {
            consumer.accept(new LoadableFinishedRecipe<>(new MaterialCastingRecipe(recipeSerializer, id, group, cast, itemCost, result, consumed, switchSlots), MaterialCastingRecipe.LOADER, advancementId));
        } else if (resultTool != null) {
            consumer.accept(new LoadableFinishedRecipe<>(new ToolCastingRecipe(recipeSerializer, id, group, this.cast, this.itemCost, this.resultTool), ToolCastingRecipe.LOADER, advancementId));
        } else {
            throw new IllegalArgumentException("Must have either result or result tool");
        }
    }
}