package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.item.ItemStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.recipe.ICommonRecipe;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

/**
 * Base interface for all casting recipes
 */
public interface ICastingRecipe extends ICommonRecipe<ICastingContainer> {
    @Override
    default ItemStack createIcon() {
        return new ItemStack(getType() == TinkerRecipeTypes.CASTING_TABLE.get() ? TinkerSmeltery.searedTable : TinkerSmeltery.searedBasin);
    }

    /**
     * Gets the amount of fluid required for this recipe
     *
     * @param inv Inventory instance
     * @return Fluid amount when using the fluid in the inventory
     */
    long getFluidAmount(ICastingContainer inv);

    /**
     * @return true if the cast item is consumed on crafting
     */
    boolean isConsumed();

    /**
     * @return true if the recipe output is placed into the casting input slot
     */
    boolean switchSlots();

    /**
     * @param inv ICastingInventory for casting recipe
     * @return cooling time for the output.
     */
    int getCoolingTime(ICastingContainer inv);

    /**
     * Calculates the cooling time for a recipe based on the amount and temperature
     *
     * @param temperature Temperature baseline in celsius
     * @param amount      Output amount
     * @return Cooling time based on the given inputs
     */
    static int calcCoolingTime(int temperature, long amount) {
        // the time in melting reipes assumes updating 5 times a second
        // we update 20 times a second, so get roughly a quart of those values
        return IMeltingRecipe.calcTimeForAmount(temperature, amount);
    }

    /**
     * Calculates the cooling time for a recipe based on the fluid input
     *
     * @param fluid Fluid input
     * @return Time for the recipe
     */
    static int calcCoolingTime(FluidStack fluid) {
        return calcCoolingTime(fluid.getFluid().getFluidType().getTemperature(fluid) - 300, fluid.getAmount());
    }
}
