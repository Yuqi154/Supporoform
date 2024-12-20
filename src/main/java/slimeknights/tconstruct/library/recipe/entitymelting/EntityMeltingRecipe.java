package slimeknights.tconstruct.library.recipe.entitymelting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.data.loadable.common.FluidStackLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ICustomOutputRecipe;
import slimeknights.mantle.recipe.container.IEmptyContainer;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.Collection;

/**
 * Recipe to melt an entity into a fluid
 */
@RequiredArgsConstructor
public class EntityMeltingRecipe implements ICustomOutputRecipe<IEmptyContainer> {
    public static final RecordLoadable<EntityMeltingRecipe> LOADER = RecordLoadable.create(
            ContextKey.ID.requiredField(),
            EntityIngredient.LOADABLE.requiredField("entity", r -> r.ingredient),
            FluidStackLoadable.REQUIRED_STACK_NBT.requiredField("result", r -> r.output),
            IntLoadable.FROM_ONE.defaultField("damage", 2, true, r -> r.damage),
            EntityMeltingRecipe::new);

    @Getter
    private final Identifier id;
    @Getter
    private final EntityIngredient ingredient;
    @Getter
    private final FluidStack output;
    @Getter
    private final int damage;

    /**
     * Checks if the recipe matches the given type
     *
     * @param type Type
     * @return True if it matches
     */
    public boolean matches(EntityType<?> type) {
        return this.ingredient.test(type);
    }

    /**
     * Gets the output for this recipe
     *
     * @param entity Entity being melted
     * @return Fluid output
     */
    public FluidStack getOutput(LivingEntity entity) {
        return this.output.copy();
    }

    /**
     * Gets a collection of inputs for filtering in JEI
     *
     * @return Collection of types
     */
    public Collection<EntityType<?>> getInputs() {
        return this.ingredient.getTypes();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TinkerSmeltery.entityMeltingSerializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TinkerRecipeTypes.ENTITY_MELTING.get();
    }

    /**
     * @deprecated use {@link #matches(EntityType)}
     */
    @Deprecated
    @Override
    public boolean matches(IEmptyContainer inv, World worldIn) {
        return false;
    }
}
