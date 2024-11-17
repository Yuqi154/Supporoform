package slimeknights.tconstruct.plugin.jei;

import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.Collections;
import java.util.List;

/**
 * Recipe category for molding casts
 */
public class MoldingRecipeCategory implements IRecipeCategory<MoldingRecipe> {
    private static final Identifier BACKGROUND_LOC = TConstruct.getResource("textures/gui/jei/casting.png");
    private static final Text TITLE = TConstruct.makeTranslation("jei", "molding.title");
    private static final Text TOOLTIP_PATTERN_CONSUMED = Text.translatable(TConstruct.makeTranslationKey("jei", "molding.pattern_consumed"));

    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;
    private final IDrawable table, basin, downArrow, upArrow;

    public MoldingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(BACKGROUND_LOC, 0, 55, 70, 57);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TinkerSmeltery.blankSandCast.get()));
        this.table = helper.createDrawable(BACKGROUND_LOC, 117, 0, 16, 16);
        this.basin = helper.createDrawable(BACKGROUND_LOC, 117, 16, 16, 16);
        this.downArrow = helper.createDrawable(BACKGROUND_LOC, 70, 55, 6, 6);
        this.upArrow = helper.createDrawable(BACKGROUND_LOC, 76, 55, 6, 6);
    }

    @Override
    public RecipeType<MoldingRecipe> getRecipeType() {
        return TConstructJEIConstants.MOLDING;
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

    @Override
    public void draw(MoldingRecipe recipe, IRecipeSlotsView slots, MatrixStack matrixStack, double mouseX, double mouseY) {
        // draw the main block
        IDrawable block = recipe.getType() == TinkerRecipeTypes.MOLDING_BASIN.get() ? this.basin : this.table;
        block.draw(matrixStack, 3, 40);

        // if no mold, we "pickup" the item, so draw no table
        if (!recipe.getPattern().isEmpty()) {
            block.draw(matrixStack, 51, 40);
            this.downArrow.draw(matrixStack, 8, 17);
        } else {
            this.upArrow.draw(matrixStack, 8, 17);
        }
    }

    @Override
    public List<Text> getTooltipStrings(MoldingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (recipe.isPatternConsumed() && !recipe.getPattern().isEmpty() && GuiUtil.isHovered((int) mouseX, (int) mouseY, 50, 7, 18, 18)) {
            return Collections.singletonList(TOOLTIP_PATTERN_CONSUMED);
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoldingRecipe recipe, IFocusGroup focuses) {
        // basic input output
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 24).addIngredients(recipe.getMaterial());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 51, 24).addItemStack(recipe.getResultItem());

        // if we have a mold, we are pressing into the table, so draw pressed item on input and output
        Ingredient pattern = recipe.getPattern();
        if (!pattern.isEmpty()) {
            IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 3, 1).addIngredients(pattern);
            if (!recipe.isPatternConsumed()) {
                IRecipeSlotBuilder preservedSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 51, 8).addIngredients(pattern);
                builder.createFocusLink(inputSlot, preservedSlot);
            }
        }
    }
}