package slimeknights.tconstruct.library.recipe.melting;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for a recipe that melts an ingredient into a fuel
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MeltingRecipeBuilder extends AbstractRecipeBuilder<MeltingRecipeBuilder> {
    private final Ingredient input;
    private final FluidStack output;
    private final int temperature;
    private final int time;
    @Nullable
    private OreRateType oreRate = null;
    private List<OreRateType> byproductRates = List.of();
    @Nullable
    private int[] unitSizes;
    private final List<FluidStack> byproducts = new ArrayList<>();

    /**
     * Creates a new builder instance using a specific temperature
     *
     * @param input       Recipe input
     * @param output      Recipe output
     * @param temperature Temperature required
     * @param time        Time this recipe takes
     * @return Builder instance
     */
    public static MeltingRecipeBuilder melting(Ingredient input, FluidStack output, int temperature, int time) {
        if (temperature < 0)
            throw new IllegalArgumentException("Invalid temperature " + temperature + ", must be greater than zero");
        if (time <= 0) throw new IllegalArgumentException("Invalid time " + time + ", must be greater than zero");
        return new MeltingRecipeBuilder(input, output, temperature, time);
    }

    /**
     * Creates a new builder instance using a specific temperature
     *
     * @param input      Recipe input
     * @param output     Recipe output
     * @param timeFactor Factor this recipe takes compared to the standard of ingots
     * @return Builder instance
     */
    public static MeltingRecipeBuilder melting(Ingredient input, FluidStack output, float timeFactor) {
        int temperature = output.getFluid().getFluidType().getTemperature(output) - 300;
        return melting(input, output, temperature, IMeltingRecipe.calcTime(temperature, timeFactor));
    }

    /**
     * Creates a new builder instance using a specific temperature
     *
     * @param input      Recipe input
     * @param fluid      Fluid result
     * @param amount     Fluid returned from recipe
     * @param timeFactor Factor this recipe takes compared to the standard of ingots
     * @return Builder instance
     */
    public static MeltingRecipeBuilder melting(Ingredient input, Fluid fluid, int amount, float timeFactor) {
        return melting(input, new FluidStack(fluid, amount), timeFactor);
    }

    /**
     * Creates a new builder instance using a specific temperature
     *
     * @param input  Recipe input
     * @param fluid  Fluid result
     * @param amount Fluid returned from recipe
     * @return Builder instance
     */
    public static MeltingRecipeBuilder melting(Ingredient input, Fluid fluid, int amount) {
        return melting(input, new FluidStack(fluid, amount), IMeltingRecipe.calcTimeFactor(amount));
    }

    /**
     * Sets this recipe as an ore recipe, output multiplied based on the melter
     *
     * @return Builder instance
     */
    public MeltingRecipeBuilder setOre(OreRateType rate, OreRateType... byproductRates) {
        this.oreRate = rate;
        this.byproductRates = List.of(byproductRates);
        return this;
    }

    /**
     * Marks this item as damagable, the output should scale based on the input damage
     *
     * @return Builder instance
     */
    public MeltingRecipeBuilder setDamagable(int... unitSizes) {
        this.unitSizes = unitSizes;
        return this;
    }

    /**
     * Adds a byproduct to this recipe
     *
     * @param fluidStack Byproduct to add
     * @return Builder instance
     */
    public MeltingRecipeBuilder addByproduct(FluidStack fluidStack) {
        this.byproducts.add(fluidStack);
        return this;
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer) {
        this.save(consumer, Registries.FLUID.getId(this.output.getFluid()));
    }

    @Override
    public void save(Consumer<RecipeJsonProvider> consumer, Identifier id) {
        if (this.oreRate != null && this.unitSizes != null) {
            throw new IllegalStateException("Builder cannot be both ore and damagable");
        }
        // only build JSON if needed
        Identifier advancementId = this.buildOptionalAdvancement(id, "melting");
        // based on properties, choose which recipe to build
        if (this.oreRate != null) {
            consumer.accept(new LoadableFinishedRecipe<>(
                    new OreMeltingRecipe(id, this.group, this.input, this.output, this.temperature, this.time, this.byproducts, this.oreRate, this.byproductRates),
                    OreMeltingRecipe.LOADER, advancementId));
        } else if (this.unitSizes != null) {
            consumer.accept(new LoadableFinishedRecipe<>(
                    new DamageableMeltingRecipe(id, this.group, this.input, this.output, this.temperature, this.time, this.byproducts, this.unitSizes[0], List.of(Arrays.stream(this.unitSizes, 1, this.unitSizes.length).boxed().toArray(Integer[]::new))),
                    DamageableMeltingRecipe.LOADER, advancementId));
        } else {
            consumer.accept(new LoadableFinishedRecipe<>(
                    new MeltingRecipe(id, this.group, this.input, this.output, this.temperature, this.time, this.byproducts),
                    MeltingRecipe.LOADER, advancementId));
        }
    }
}