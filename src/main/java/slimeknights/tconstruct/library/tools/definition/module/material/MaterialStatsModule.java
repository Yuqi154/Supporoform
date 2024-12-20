package slimeknights.tconstruct.library.tools.definition.module.material;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.Identifier;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.field.OptionallyNestedLoadable;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolStatsHook;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitHook;
import slimeknights.tconstruct.library.tools.helper.ModifierBuilder;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Module for building tool stats using materials
 */
public class MaterialStatsModule implements ToolStatsHook, ToolTraitHook, ToolMaterialHook, MaterialRepairToolHook, ToolModule {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialStatsModule>defaultHooks(ToolHooks.TOOL_STATS, ToolHooks.TOOL_TRAITS, ToolHooks.TOOL_MATERIALS, ToolHooks.MATERIAL_REPAIR);
    protected static final LoadableField<Integer, MaterialStatsModule> PRIMARY_PART_FIELD = IntLoadable.FROM_MINUS_ONE.defaultField("primary_part", 0, true, m -> m.primaryPart);
    public static final RecordLoadable<MaterialStatsModule> LOADER = RecordLoadable.create(
            new OptionallyNestedLoadable<>(MaterialStatsId.PARSER, "stat").list().requiredField("stat_types", m -> m.statTypes),
            new StatScaleField("stat", "stat_types"),
            PRIMARY_PART_FIELD,
            MaterialStatsModule::new);

    private final List<MaterialStatsId> statTypes;
    @Getter
    @VisibleForTesting
    final float[] scales;
    private int[] repairIndices;
    private final int primaryPart;

    /**
     * @deprecated use {@link #MaterialStatsModule(List, float[], int)} or {@link Builder}
     */
    @Deprecated
    public MaterialStatsModule(List<MaterialStatsId> statTypes, float[] scales) {
        this(statTypes, scales, 0);
    }

    protected MaterialStatsModule(List<MaterialStatsId> statTypes, float[] scales, int primaryPart) {
        this.statTypes = statTypes;
        this.scales = scales;
        this.primaryPart = primaryPart;
    }

    @Override
    public RecordLoadable<? extends MaterialStatsModule> getLoader() {
        return LOADER;
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public void addModules(ModuleHookMap.Builder builder) {
        // automatically add the primary part if not disabled
        if (this.primaryPart >= 0 && this.primaryPart < this.statTypes.size()) {
            builder.addHook(new MaterialTraitsModule(this.statTypes.get(this.primaryPart), this.primaryPart), ToolHooks.REBALANCED_TRAIT);
        }
    }

    @Override
    public List<MaterialStatsId> getStatTypes(ToolDefinition definition) {
        return this.statTypes;
    }

    /**
     * Gets the repair indices, calculating them if needed
     */
    private int[] getRepairIndices() {
        if (this.repairIndices == null) {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            this.repairIndices = IntStream.range(0, this.statTypes.size()).filter(i -> registry.canRepair(this.statTypes.get(i))).toArray();
        }
        return this.repairIndices;
    }

    @Override
    public boolean isRepairMaterial(IToolStackView tool, MaterialId material) {
        for (int part : this.getRepairIndices()) {
            if (tool.getMaterial(part).matches(material)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getRepairAmount(IToolStackView tool, MaterialId material) {
        Identifier toolId = tool.getDefinition().getId();
        for (int i : this.getRepairIndices()) {
            if (tool.getMaterial(i).matches(material)) {
                return MaterialRepairModule.getDurability(toolId, material, this.statTypes.get(i));
            }
        }
        return 0;
    }

    @Override
    public void addToolStats(IToolContext context, ModifierStatsBuilder builder) {
        MaterialNBT materials = context.getMaterials();
        if (materials.size() > 0) {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            for (int i = 0; i < this.statTypes.size(); i++) {
                MaterialStatsId statType = this.statTypes.get(i);
                // apply the stats if they exist for the material
                Optional<IMaterialStats> stats = registry.getMaterialStats(materials.get(i).getId(), statType);
                if (stats.isPresent()) {
                    stats.get().apply(builder, this.scales[i]);
                } else {
                    // fallback to the default stats if present
                    IMaterialStats defaultStats = registry.getDefaultStats(statType);
                    if (defaultStats != null) {
                        defaultStats.apply(builder, this.scales[i]);
                    }
                }
            }
        }
    }

    @Override
    public void addTraits(ToolDefinition definition, MaterialNBT materials, ModifierBuilder builder) {
        int max = Math.min(materials.size(), this.statTypes.size());
        if (max > 0) {
            IMaterialRegistry materialRegistry = MaterialRegistry.getInstance();
            for (int i = 0; i < max; i++) {
                builder.add(materialRegistry.getTraits(materials.get(i).getId(), this.statTypes.get(i)));
            }
        }
    }


    /* Builder */

    /**
     * Creates a new builder instance
     */
    public static Builder stats() {
        return new Builder();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final ImmutableList.Builder<MaterialStatsId> stats = ImmutableList.builder();
        private final ImmutableList.Builder<Float> scales = ImmutableList.builder();
        @Setter
        @Accessors(fluent = true)
        private int primaryPart = 0;

        /**
         * Adds a stat type
         */
        public Builder stat(MaterialStatsId stat, float scale) {
            this.stats.add(stat);
            this.scales.add(scale);
            return this;
        }

        /**
         * Adds a stat type
         */
        public Builder stat(MaterialStatsId stat) {
            return this.stat(stat, 1);
        }

        /**
         * Builds the array of scales from the list
         */
        static float[] buildScales(List<Float> list) {
            float[] scales = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                scales[i] = list.get(i);
            }
            return scales;
        }

        /**
         * Builds the module
         */
        public MaterialStatsModule build() {
            List<MaterialStatsId> stats = this.stats.build();
            if (this.primaryPart >= stats.size() || this.primaryPart < -1) {
                throw new IllegalStateException("Primary part must be within parts list, maximum " + stats.size() + ", got " + this.primaryPart);
            }
            return new MaterialStatsModule(stats, buildScales(this.scales.build()), this.primaryPart);
        }
    }
}
