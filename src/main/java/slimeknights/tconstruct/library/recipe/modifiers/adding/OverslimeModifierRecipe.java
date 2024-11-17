package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.slotless.OverslimeModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe.withModifiers;

/**
 * Recipe to add overslime to a tool
 */
public class OverslimeModifierRecipe implements ITinkerStationRecipe, IDisplayModifierRecipe {
    private static final RecipeResult<ItemStack> AT_CAPACITY = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "overslime.at_capacity"));
    public static final RecordLoadable<OverslimeModifierRecipe> LOADER = RecordLoadable.create(
            ContextKey.ID.requiredField(),
            IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
            IntLoadable.FROM_ONE.requiredField("restore_amount", r -> r.restoreAmount),
            OverslimeModifierRecipe::new);

    @Getter
    private final Identifier id;
    private final Ingredient ingredient;
    private final int restoreAmount;

    public OverslimeModifierRecipe(Identifier id, Ingredient ingredient, int restoreAmount) {
        this.id = id;
        this.ingredient = ingredient;
        this.restoreAmount = restoreAmount;
        ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.overslime);
    }

    @Override
    public boolean matches(ITinkerStationContainer inv, World world) {
        if (!inv.getTinkerableStack().isIn(TinkerTags.Items.DURABILITY)) {
            return false;
        }
        // must find at least one slime, but multiple is fine, as is empty slots
        return IncrementalModifierRecipe.containsOnlyIngredient(inv, ingredient);
    }

    @Override
    public RecipeResult<ItemStack> getValidatedResult(ITinkerStationContainer inv, DynamicRegistryManager manager) {
        ToolStack tool = inv.getTinkerable();
        OverslimeModifier overslime = TinkerModifiers.overslime.get();
        ModifierId overslimeId = TinkerModifiers.overslime.getId();
        ModifierEntry entry = tool.getModifier(overslimeId);
        // if the tool lacks true overslime, add overslime
        if (tool.getUpgrades().getLevel(overslimeId) == 0) {
            // however, if we have overslime though a trait and reached our cap, also do nothing
            if (entry.getLevel() > 0 && overslime.getShield(tool) >= overslime.getShieldCapacity(tool, entry)) {
                return AT_CAPACITY;
            }
            // truely add overslime, this will cost a slime crystal if full durability
            tool = tool.copy();
            tool.addModifier(TinkerModifiers.overslime.getId(), 1);
        } else {
            // ensure we are not at the cap already
            if (overslime.getShield(tool) >= overslime.getShieldCapacity(tool, entry)) {
                return AT_CAPACITY;
            }
            // copy the tool as we will change it later
            tool = tool.copy();
        }

        // see how much value is available, update overslime to the max possible
        int available = IncrementalModifierRecipe.getAvailableAmount(inv, ingredient, restoreAmount);
        overslime.addOverslime(tool, entry, available);
        return RecipeResult.success(tool.createStack(Math.min(inv.getTinkerableSize(), shrinkToolSlotBy())));
    }

    /**
     * Updates the input stacks upon crafting this recipe
     *
     * @param result Result from {@link #assemble(ITinkerStationContainer)}. Generally should not be modified
     * @param inv    Inventory instance to modify inputs
     */
    @Override
    public void updateInputs(ItemStack result, IMutableTinkerStationContainer inv, boolean isServer) {
        ToolStack tool = inv.getTinkerable();
        // if the original tool did not have overslime, its treated as having no slime
        int current = 0;
        OverslimeModifier overslime = TinkerModifiers.overslime.get();
        if (tool.getModifierLevel(overslime) != 0) {
            current = overslime.getShield(tool);
        }

        // how much did we actually consume?
        int maxNeeded = overslime.getShield(ToolStack.from(result)) - current;
        IncrementalModifierRecipe.updateInputs(inv, this.ingredient, maxNeeded, this.restoreAmount, ItemStack.EMPTY);
    }

    /**
     * @deprecated use {@link #assemble(ITinkerStationContainer)}
     */
    @Deprecated
    @Override
    public ItemStack getOutput(DynamicRegistryManager manager) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TinkerModifiers.overslimeSerializer.get();
    }

    /* JEI display */
    /**
     * Cache of modifier result, same for all overslime
     */
    private static final ModifierEntry RESULT = new ModifierEntry(TinkerModifiers.overslime, 1);
    /**
     * Cache of input and output tools for display
     */
    private List<ItemStack> toolWithoutModifier, toolWithModifier = null;

    @Override
    public int getInputCount() {
        return 1;
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
        if (slot == 0) {
            return Arrays.asList(this.ingredient.getMatchingStacks());
        }
        return Collections.emptyList();
    }

    @Override
    public List<ItemStack> getToolWithoutModifier() {
        if (this.toolWithoutModifier == null) {
            this.toolWithoutModifier = RegistryHelper.getTagValueStream(Registries.ITEM, TinkerTags.Items.DURABILITY).map(MAP_TOOL_FOR_RENDERING).toList();
        }
        return this.toolWithoutModifier;
    }

    @Override
    public List<ItemStack> getToolWithModifier() {
        if (this.toolWithModifier == null) {
            OverslimeModifier overslime = TinkerModifiers.overslime.get();
            List<ModifierEntry> result = List.of(RESULT);
            this.toolWithModifier = RegistryHelper.getTagValueStream(Registries.ITEM, TinkerTags.Items.DURABILITY)
                    .map(MAP_TOOL_FOR_RENDERING)
                    .map(stack -> withModifiers(stack, result, data -> overslime.setShield(data, this.restoreAmount)))
                    .toList();
        }
        return this.toolWithModifier;
    }

    @Override
    public ModifierEntry getDisplayResult() {
        return RESULT;
    }
}