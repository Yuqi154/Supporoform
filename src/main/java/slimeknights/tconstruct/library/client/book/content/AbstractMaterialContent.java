package slimeknights.tconstruct.library.client.book.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.ForgeI18n;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeI18n;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.TextComponentData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextComponentElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.book.elements.TinkerItemElement;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerToolParts;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for material content pages
 */
public abstract class AbstractMaterialContent extends PageContent {
    private static final Text PART_BUILDER = TConstruct.makeTranslation("book", "material.part_builder");
    private static final String CAST_FROM = TConstruct.makeTranslationKey("book", "material.cast_from");
    private static final String COMPOSITE_FROM = TConstruct.makeTranslationKey("book", "material.composite_from");

    static final int COLUMN_MARGIN = 22;
    static final int STAT_WIDTH = BookScreen.PAGE_WIDTH / 2 - 10;

    // cached data
    private transient MaterialVariantId materialVariant;
    private transient List<ItemStack> repairStacks;
    private transient IMaterial material;

    @SerializedName("material")
    public String materialName;
    public boolean detailed;

    public AbstractMaterialContent(MaterialVariantId materialVariant, boolean detailed) {
        this.materialName = materialVariant.toString();
        this.materialVariant = materialVariant;
        this.detailed = detailed;
    }

    /**
     * Gets the page type ID
     */
    public abstract Identifier getId();

    /**
     * Given an index 0-3, return the stat type to show at that index
     */
    @Nullable
    protected abstract MaterialStatsId getStatType(int index);

    /**
     * Gets the text to display, empty if no text
     */
    protected abstract String getTextKey(MaterialId material);

    /**
     * Returns true if this stat type is supported, anything unsupported is hidden from the tools list
     */
    protected abstract boolean supportsStatType(MaterialStatsId statsId);

    /**
     * Gets the material variant for this page
     */
    protected MaterialVariantId getMaterialVariant() {
        if (this.materialVariant == null) {
            this.materialVariant = MaterialVariantId.parse(this.materialName);
        }
        return this.materialVariant;
    }

    /**
     * Gets the material for this page
     */
    protected IMaterial getMaterial() {
        if (this.material == null) {
            this.material = MaterialRegistry.getMaterial(this.getMaterialVariant().getId());
        }
        return this.material;
    }

    /**
     * Gets a list of all repair items for the given material
     */
    protected List<ItemStack> getRepairStacks() {
        if (this.repairStacks == null) {
            World world = MinecraftClient.getInstance().world;
            if (world == null) {
                return Collections.emptyList();
            }
            // simply combine all items from all recipes
            MaterialVariantId material = this.getMaterialVariant();
            this.repairStacks = RecipeHelper.getUIRecipes(world.getRecipeManager(), TinkerRecipeTypes.MATERIAL.get(), MaterialRecipe.class, recipe -> material.matchesVariant(recipe.getMaterial()))
                    .stream()
                    .flatMap(recipe -> Arrays.stream(recipe.getIngredient().getItems()))
                    .collect(Collectors.toList());
            // no repair items? use the repair kit
            if (this.repairStacks.isEmpty()) {
                TConstruct.LOG.debug("Material with id " + material + " has no representation items associated with it, using repair kit");
                // bypass the valid check, because we need to show something
                this.repairStacks = Collections.singletonList(TinkerToolParts.repairKit.get().withMaterialForDisplay(material));
            }
        }
        return this.repairStacks;
    }

    /**
     * Gets the display stacks for this page
     */
    public List<ItemStack> getDisplayStacks() {
        return this.getRepairStacks();
    }

    @Override
    public String getTitle() {
        return this.getTitleComponent().getString();
    }

    /**
     * Gets the title of this page to display in the index
     */
    public Text getTitleComponent() {
        return MaterialTooltipCache.getDisplayName(this.getMaterialVariant());
    }

    @Override
    public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
        MaterialVariantId materialVariant = this.getMaterialVariant();
        this.addTitle(list, this.getTitle(), true, MaterialTooltipCache.getColor(materialVariant).getRgb());

        // the cool tools to the left/right
        this.addDisplayItems(list, rightSide ? BookScreen.PAGE_WIDTH - 18 : 0, materialVariant);

        int y = this.getTitleHeight();
        int x = (rightSide ? 0 : COLUMN_MARGIN) + 2;

        // material stats
        y = this.addAllMaterialStats(x, y, list, 2, true);
        // material description
        this.addDescription(x, y, list);
    }

    /**
     * Adds the given number of rows of material info
     */
    protected int addAllMaterialStats(int x, int y, List<BookElement> list, int rows, boolean includeStats) {
        for (int i = 0; i < rows; i++) {
            y = Math.max(
                    this.addMaterialStat(x - 3, y, STAT_WIDTH, list, this.getStatType(i * 2), includeStats),
                    this.addMaterialStat(x + STAT_WIDTH, y, STAT_WIDTH, list, this.getStatType(i * 2 + 1), includeStats));
        }
        return y;
    }

    /**
     * Adds the stats for a stat type
     */
    protected int addMaterialStat(int x, int y, int w, List<BookElement> list, @Nullable MaterialStatsId statsId, boolean includeStats) {
        if (statsId == null) {
            return y;
        }
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        MaterialVariantId material = this.getMaterialVariant();
        Optional<IMaterialStats> stats = registry.getMaterialStats(material.getId(), statsId);
        if (stats.isEmpty()) {
            return y;
        }

        // create a list of all valid toolparts with the stats
        List<ItemStack> parts = getPartsWithMaterial(material, statsId);
        // said parts next to the name
        int textOffset = 0;
        if (!parts.isEmpty()) {
            ItemElement display = new TinkerItemElement(x, y + 1, 0.5f, parts);
            list.add(display);
            textOffset = 10;
        }

        // and the name itself
        list.add(new TextComponentElement(x + textOffset, y, w - textOffset, 10, stats.get().getLocalizedName().formatted(Formatting.BOLD, Formatting.UNDERLINE)));
        y += 12;

        List<TextComponentData> lineData = Lists.newArrayList();
        // add lines of tool information
        if (includeStats) {
            addStatLines(lineData, stats.get());
        }
        addTraitLines(lineData, registry.getTraits(material.getId(), statsId));

        list.add(new TextComponentElement(x, y, w, BookScreen.PAGE_HEIGHT, lineData));

        return y + (lineData.size() * 5) + 3;
    }

    /**
     * Gets all stat text data for the given stat instance
     */
    private static void addStatLines(List<TextComponentData> lineData, IMaterialStats stats) {
        List<Text> statInfo = stats.getLocalizedInfo();
        List<Text> tooltips = stats.getLocalizedDescriptions();
        int size = Math.min(statInfo.size(), tooltips.size());
        for (int i = 0; i < size; i++) {
            // skip empty tooltips, means empty stats
            Text tooltip = tooltips.get(i);
            TextComponentData text = new TextComponentData(statInfo.get(i));
            if (tooltip.getString().isEmpty()) {
                text.tooltips = null;
            } else {
                text.tooltips = new Text[]{tooltip};
            }

            lineData.add(text);
            lineData.add(new TextComponentData("\n"));
        }
    }

    /**
     * Gets all trait text data for the given stat instance
     */
    protected static void addTraitLines(List<TextComponentData> lineData, List<ModifierEntry> traits) {
        for (ModifierEntry trait : traits) {
            Modifier mod = trait.getModifier();
            TextComponentData textComponentData = new TextComponentData(mod.getDisplayName());

            List<Text> textComponents = mod.getDescriptionList(trait.getLevel());
            textComponentData.tooltips = textComponents.toArray(new Text[0]);
            textComponentData.text = textComponentData.text.copy().formatted(Formatting.DARK_GRAY, Formatting.UNDERLINE);

            lineData.add(textComponentData);
            lineData.add(new TextComponentData("\n"));
        }
    }

    /**
     * Adds items to the display tools list for all relevant recipes
     */
    protected void addPrimaryDisplayItems(List<ItemElement> displayTools, MaterialVariantId materialId) {
        // part builder
        if (this.getMaterial().isCraftable()) {
            ItemStack partBuilder = new ItemStack(TinkerTables.partBuilder.asItem());
            ItemElement elementItem = new TinkerItemElement(partBuilder);
            elementItem.tooltip = ImmutableList.of(PART_BUILDER);
            displayTools.add(elementItem);
        }

        // regular casting recipes
        List<MaterialFluidRecipe> fluids = MaterialCastingLookup.getCastingFluids(materialId);
        if (!fluids.isEmpty()) {
            ItemElement elementItem = new TinkerItemElement(0, 0, 1, fluids.stream().flatMap(recipe -> recipe.getFluids().stream())
                    .map(fluid -> new ItemStack(fluid.getFluid().getBucketItem()))
                    .collect(Collectors.toList()));
            FluidStack firstFluid = fluids.stream()
                    .flatMap(recipe -> recipe.getFluids().stream())
                    .findFirst().orElse(FluidStack.EMPTY);
            elementItem.tooltip = ImmutableList.of(Text.translatable(CAST_FROM, firstFluid.getDisplayName()));
            displayTools.add(elementItem);
        }

        // composite casting
        List<MaterialFluidRecipe> composites = MaterialCastingLookup.getCompositeFluids(materialId);
        for (MaterialFluidRecipe composite : composites) {
            MaterialVariant input = composite.getInput();
            if (input != null && !this.materialVariant.matchesVariant(input.getVariant())) {
                MaterialVariantId inputId = input.getVariant();
                ItemElement elementItem = new TinkerItemElement(0, 0, 1, MaterialCastingLookup.getAllItemCosts().stream()
                        .map(Entry::getKey)
                        .filter(part -> part.canUseMaterial(inputId.getId()) && part.canUseMaterial(this.material))
                        .map(part -> part.withMaterial(inputId))
                        .collect(Collectors.toList()));
                FluidStack firstFluid = composite.getFluids().stream().findFirst().orElse(FluidStack.EMPTY);
                elementItem.tooltip = ImmutableList.of(Text.translatable(COMPOSITE_FROM, firstFluid.getDisplayName(), MaterialTooltipCache.getDisplayName(inputId)));
                displayTools.add(elementItem);
            }
        }
    }

    /**
     * Adds display items to the tool sidebars
     */
    protected void addDisplayItems(ArrayList<BookElement> list, int x, MaterialVariantId materialVariant) {
        List<ItemElement> displayTools = Lists.newArrayList();

        // add display items
        displayTools.add(new TinkerItemElement(0, 0, 1f, this.getRepairStacks()));
        this.addPrimaryDisplayItems(displayTools, materialVariant);

        // fill in leftover space
        if (displayTools.size() < 9) {
            MaterialId materialId = materialVariant.getId();
            toolLoop:
            for (RegistryEntry<Item> item : Registries.ITEM.getOrCreateEntryList(TinkerTags.Items.MULTIPART_TOOL)) {
                if (item.value() instanceof IModifiable tool) {
                    List<MaterialStatsId> requirements = ToolMaterialHook.stats(tool.getToolDefinition());
                    // start building the tool with the given material
                    MaterialNBT.Builder materials = MaterialNBT.builder();
                    boolean usedMaterial = false;
                    for (MaterialStatsId part : requirements) {
                        // if any stat type of the tool is not supported by this page, skip the whole tool
                        if (!this.supportsStatType(part)) {
                            continue toolLoop;
                        }
                        // if the stat type is not supported by the material, substitute
                        if (part.canUseMaterial(materialId)) {
                            materials.add(materialVariant);
                            usedMaterial = true;
                        } else {
                            materials.add(MaterialRegistry.firstWithStatType(part));
                        }
                    }

                    // only add a stack if our material showed up
                    if (usedMaterial) {
                        ItemStack display = ToolBuildHandler.buildItemFromMaterials(tool, materials.build());
                        displayTools.add(new TinkerItemElement(display));
                        if (displayTools.size() == 9) {
                            break;
                        }
                    }
                }
            }
        }

        // built tools
        if (!displayTools.isEmpty()) {
            int y = this.getTitleHeight() - 5;
            for (ItemElement element : displayTools) {
                element.x = x;
                element.y = y;
                element.scale = 1f;
                y += ItemElement.ITEM_SIZE_HARDCODED;

                list.add(element);
            }
        }
    }

    /**
     * Adds the display text at the end of the material description
     */
    protected void addDescription(int x, int y, List<BookElement> list) {
        // inspirational quote, or boring description text
        String textKey = this.getTextKey(this.materialVariant.getId());
        if (I18n.hasTranslation(textKey)) {
            // using forge instead of I18n.format as that prevents % from being interpreted as a format key
            String translated = ForgeI18n.getPattern(textKey);
            if (!this.detailed) {
                translated = '"' + translated + '"';
            }
            TextData flavourData = new TextData(translated);
            flavourData.italic = !this.detailed;
            list.add(new TextElement(x - 3, y + 5, BookScreen.PAGE_WIDTH - COLUMN_MARGIN - 5, 60, flavourData));
        }
    }


    /**
     * Gets a list of all tool parts
     */
    private static List<IToolPart> ALL_PARTS = null;

    /**
     * Gets a list of all tool parts
     */
    private static List<IToolPart> getToolParts() {
        if (ALL_PARTS == null) {
            ALL_PARTS = RegistryHelper.getTagValueStream(Registries.ITEM, TinkerTags.Items.TOOL_PARTS)
                    .filter(item -> item instanceof IToolPart)
                    .map(item -> (IToolPart) item)
                    .toList();
        }
        return ALL_PARTS;
    }

    /**
     * Gets a list of all parts with the given material
     */
    private static List<ItemStack> getPartsWithMaterial(MaterialVariantId material, MaterialStatsId statType) {
        return getToolParts().stream()
                .filter(part -> part.getStatType().equals(statType))
                .map(part -> part.withMaterialForDisplay(material))
                .toList();
    }
}