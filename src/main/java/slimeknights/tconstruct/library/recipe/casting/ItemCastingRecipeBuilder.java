package slimeknights.tconstruct.library.recipe.casting;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.function.Consumer;

/**
 * Builder for an item casting recipe. Takes a fluid and optional cast to create an item
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
@RequiredArgsConstructor(staticName = "castingRecipe")
public class ItemCastingRecipeBuilder extends AbstractRecipeBuilder<ItemCastingRecipeBuilder> {
    private final ItemOutput result;
    private final TypeAwareRecipeSerializer<? extends ItemCastingRecipe> recipeSerializer;
    private Ingredient cast = Ingredient.EMPTY;
    private FluidIngredient fluid = FluidIngredient.EMPTY;
    @Setter
    @Accessors(chain = true)
    private int coolingTime = -1;
    private boolean consumed = false;
    private boolean switchSlots = false;

    /**
     * Creates a new casting basin recipe
     *
     * @param result Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder basinRecipe(ItemOutput result) {
        return castingRecipe(result, TinkerSmeltery.basinRecipeSerializer.get());
    }

    /**
     * Creates a new casting basin recipe
     *
     * @param result Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder retexturedBasinRecipe(ItemOutput result) {
        return castingRecipe(result, TinkerSmeltery.retexturedBasinRecipeSerializer.get());
    }

    /**
     * Creates a new casting basin recipe
     *
     * @param resultIn Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder basinRecipe(ItemConvertible resultIn) {
        return basinRecipe(ItemOutput.fromItem(resultIn));
    }

    /**
     * Creates a new casting basin recipe
     *
     * @param result Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder basinRecipe(TagKey<Item> result) {
        return basinRecipe(ItemOutput.fromTag(result));
    }

    /**
     * Creates a new casting table recipe
     *
     * @param resultIn Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder tableRecipe(ItemOutput resultIn) {
        return castingRecipe(resultIn, TinkerSmeltery.tableRecipeSerializer.get());
    }

    /**
     * Creates a new casting table recipe
     *
     * @param resultIn Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder retexturedTableRecipe(ItemOutput resultIn) {
        return castingRecipe(resultIn, TinkerSmeltery.retexturedTableRecipeSerializer.get());
    }

    /**
     * Creates a new casting table recipe
     *
     * @param resultIn Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder tableRecipe(ItemConvertible resultIn) {
        return tableRecipe(ItemOutput.fromItem(resultIn));
    }

    /**
     * Creates a new casting table recipe
     *
     * @param result Recipe result
     * @return Builder instance
     */
    public static ItemCastingRecipeBuilder tableRecipe(TagKey<Item> result) {
        return tableRecipe(ItemOutput.fromTag(result));
    }


    /* Fluids */

    /**
     * Sets the fluid for this recipe
     *
     * @param fluid  Fluid instance
     * @param amount amount of fluid
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setFluid(Fluid fluid, int amount) {
        return this.setFluid(FluidIngredient.of(fluid, amount));
    }

    /**
     * Sets the fluid for this recipe
     *
     * @param tagIn  Tag<Fluid> instance
     * @param amount amount of fluid
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setFluid(TagKey<Fluid> tagIn, int amount) {
        return this.setFluid(FluidIngredient.of(tagIn, amount));
    }

    /**
     * Sets the fluid ingredient
     *
     * @param fluid Fluid ingredient instance
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setFluid(FluidIngredient fluid) {
        this.fluid = fluid;
        return this;
    }

    /**
     * Sets the recipe cooling time
     *
     * @param temperature Recipe temperature
     * @param amount      Recipe amount
     */
    public ItemCastingRecipeBuilder setCoolingTime(int temperature, int amount) {
        return setCoolingTime(ICastingRecipe.calcCoolingTime(temperature, amount));
    }

    /**
     * Sets the fluid for this recipe, and cooling time if unset.
     *
     * @param fluidStack Fluid input
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setFluidAndTime(FluidStack fluidStack) {
        this.fluid = FluidIngredient.of(fluidStack);
        if (this.coolingTime == -1) {
            this.coolingTime = ICastingRecipe.calcCoolingTime(fluidStack);
        }
        return this;
    }

    /**
     * Sets the fluid for this recipe, and cooling time
     *
     * @param fluid    Fluid object instance
     * @param forgeTag If true, uses the forge tag
     * @param amount   amount of fluid
     */
    public ItemCastingRecipeBuilder setFluidAndTime(FluidObject<?> fluid, boolean forgeTag, int amount) {
        setFluid(fluid.ingredient(amount, forgeTag));
        setCoolingTime(fluid.getType().getTemperature() - 300, amount);
        return this;
    }

    /**
     * Sets the fluid for this recipe using the forge tag, and cooling time
     *
     * @param fluid  Fluid object instance
     * @param amount amount of fluid
     */
    public ItemCastingRecipeBuilder setFluidAndTime(FluidObject<?> fluid, int amount) {
        return setFluidAndTime(fluid, false, amount);
    }

    /* Cast */

    /**
     * Sets the cast from a tag
     *
     * @param tagIn    Cast tag
     * @param consumed If true, the cast is consumed
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setCast(TagKey<Item> tagIn, boolean consumed) {
        return this.setCast(Ingredient.fromTag(tagIn), consumed);
    }

    /**
     * Sets the cast from a tag
     *
     * @param itemIn   Cast item
     * @param consumed If true, the cast is consumed
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setCast(ItemConvertible itemIn, boolean consumed) {
        return this.setCast(Ingredient.ofItems(itemIn), consumed);
    }

    /**
     * Sets the cast from an ingredient
     *
     * @param ingredient Cast ingredient
     * @param consumed   If true, the cast is consumed
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setCast(Ingredient ingredient, boolean consumed) {
        this.cast = ingredient;
        this.consumed = consumed;
        return this;
    }

    /**
     * Set output of recipe to be put into the input slot.
     * Mostly used for cast creation
     *
     * @return Builder instance
     */
    public ItemCastingRecipeBuilder setSwitchSlots() {
        this.switchSlots = true;
        return this;
    }

    /**
     * Builds a recipe using the registry name as the recipe name
     *
     * @param consumerIn Recipe consumer
     */
    @Override
    public void save(Consumer<RecipeJsonProvider> consumerIn) {
        this.save(consumerIn, Registries.ITEM.getId(this.result.get().getItem()));
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer, Identifier id) {
        if (this.fluid == FluidIngredient.EMPTY) {
            throw new IllegalStateException("Casting recipes require a fluid input");
        }
        if (this.coolingTime < 0) {
            throw new IllegalStateException("Cooling time is too low, must be at least 0");
        }
        Identifier advancementId = this.buildOptionalAdvancement(id, "casting");
        // yeah, retextured recipes have their own constructor, does not matter as long as we pass the right serializer in
        // you can use this for your custom recipe extensions too if you don't change the JSON :)
        consumer.accept(new LoadableFinishedRecipe<>(new ItemCastingRecipe(recipeSerializer, id, group, this.cast, this.fluid, this.result, this.coolingTime, this.consumed, this.switchSlots), ItemCastingRecipe.LOADER, advancementId));
    }
}