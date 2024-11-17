package slimeknights.tconstruct.library.recipe.alloying;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for alloy recipes
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "alloy")
public class AlloyRecipeBuilder extends AbstractRecipeBuilder<AlloyRecipeBuilder> {
    private final FluidStack output;
    private final int temperature;
    private final List<FluidIngredient> inputs = new ArrayList<>();

    /**
     * Creates a new recipe producing the given fluid
     *
     * @param fluid Fluid output
     * @return Builder instance
     */
    public static AlloyRecipeBuilder alloy(FluidStack fluid) {
        return alloy(fluid, fluid.getFluid().getFluidType().getTemperature(fluid) - 300);
    }

    /**
     * Creates a new recipe producing the given fluid
     *
     * @param fluid  Fluid output
     * @param amount Output amount
     * @return Builder instance
     */
    public static AlloyRecipeBuilder alloy(Fluid fluid, int amount) {
        return alloy(new FluidStack(fluid, amount));
    }


    /* Inputs */

    /**
     * Adds an input
     *
     * @param input Input ingredient
     * @return Builder instance
     */
    public AlloyRecipeBuilder addInput(FluidIngredient input) {
        inputs.add(input);
        return this;
    }

    /**
     * Adds an input
     *
     * @param input Input fluid
     * @return Builder instance
     */
    public AlloyRecipeBuilder addInput(FluidStack input) {
        return addInput(FluidIngredient.of(input));
    }

    /**
     * Adds an input
     *
     * @param fluid  Input fluid
     * @param amount Input amount
     * @return Builder instance
     */
    public AlloyRecipeBuilder addInput(Fluid fluid, int amount) {
        return addInput(FluidIngredient.of(new FluidStack(fluid, amount)));
    }

    /**
     * Adds an input
     *
     * @param tag    Input tag
     * @param amount Input amount
     * @return Builder instance
     */
    public AlloyRecipeBuilder addInput(TagKey<Fluid> tag, int amount) {
        return addInput(FluidIngredient.of(tag, amount));
    }


    /* Building */

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer) {
        save(consumer, Registries.FLUID.getId(this.output.getFluid()));
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer, Identifier id) {
        if (this.inputs.size() < 2) {
            throw new IllegalStateException("Invalid alloying recipe " + id + ", must have at least two inputs");
        }
        consumer.accept(new LoadableFinishedRecipe<>(
                new AlloyRecipe(id, this.inputs, this.output, this.temperature),
                AlloyRecipe.LOADER,
                this.buildOptionalAdvancement(id, "alloys")
        ));
    }
}