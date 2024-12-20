package slimeknights.tconstruct.tables.recipe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IncrementalModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.TinkerTables;

@RequiredArgsConstructor
public class TinkerStationDamagingRecipe implements ITinkerStationRecipe {
    public static final RecordLoadable<TinkerStationDamagingRecipe> LOADER = RecordLoadable.create(
            ContextKey.ID.requiredField(),
            IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
            IntLoadable.FROM_ONE.requiredField("damage_amount", r -> r.damageAmount),
            TinkerStationDamagingRecipe::new);
    private static final RecipeResult<ItemStack> BROKEN = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "damaging.broken"));

    @Getter
    private final Identifier id;
    private final Ingredient ingredient;
    private final int damageAmount;

    @Override
    public boolean matches(ITinkerStationContainer inv, World world) {
        if (!inv.getTinkerableStack().isIn(TinkerTags.Items.DURABILITY)) {
            return false;
        }
        // must find at least one input, but multiple is fine, as is empty slots
        return IncrementalModifierRecipe.containsOnlyIngredient(inv, this.ingredient);
    }

    @Override
    public RecipeResult<ItemStack> getValidatedResult(ITinkerStationContainer inv, DynamicRegistryManager manager) {
        ToolStack tool = inv.getTinkerable();
        if (tool.isBroken()) {
            return BROKEN;
        }
        // simply damage the tool directly
        tool = tool.copy();
        int maxDamage = IncrementalModifierRecipe.getAvailableAmount(inv, this.ingredient, this.damageAmount);
        ToolDamageUtil.directDamage(tool, maxDamage, null, inv.getTinkerableStack());
        return RecipeResult.success(tool.createStack());
    }

    @Override
    public int shrinkToolSlotBy() {
        return 1;
    }

    @Override
    public void updateInputs(ItemStack result, IMutableTinkerStationContainer inv, boolean isServer) {
        // how much did we actually consume?
        int damageTaken = ToolStack.from(result).getDamage() - inv.getTinkerable().getDamage();
        IncrementalModifierRecipe.updateInputs(inv, this.ingredient, damageTaken, this.damageAmount, ItemStack.EMPTY);
    }

    /**
     * @deprecated Use {@link ITinkerStationRecipe#getValidatedResult(ITinkerStationContainer, DynamicRegistryManager)}
     */
    @Deprecated
    @Override
    public ItemStack getOutput(DynamicRegistryManager manager) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TinkerTables.tinkerStationDamagingSerializer.get();
    }
}
