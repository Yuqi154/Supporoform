package slimeknights.tconstruct.library.tools.definition.module.mining;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.utils.HarvestTiers;

import java.util.List;

import net.minecraft.item.ToolMaterial;

/**
 * Module that limits the tier to the given max
 * TODO 1.20: rename to MaxTierModule
 */
public record MaxTierHarvestLogic(ToolMaterial tier) implements MiningTierToolHook, ToolModule {
    public static final RecordLoadable<MaxTierHarvestLogic> LOADER = RecordLoadable.create(TinkerLoadables.TIER.requiredField("tier", MaxTierHarvestLogic::tier), MaxTierHarvestLogic::new);
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaxTierHarvestLogic>defaultHooks(ToolHooks.MINING_TIER);

    @Override
    public RecordLoadable<MaxTierHarvestLogic> getLoader() {
        return LOADER;
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public ToolMaterial modifyTier(IToolStackView tool, ToolMaterial tier) {
        return HarvestTiers.min(this.tier, tier);
    }
}
