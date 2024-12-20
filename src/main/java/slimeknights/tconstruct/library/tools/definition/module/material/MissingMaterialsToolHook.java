package slimeknights.tconstruct.library.tools.definition.module.material;

import net.minecraft.util.math.random.Random;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;

/**
 * Hook for filling a tool with materials
 */
public interface MissingMaterialsToolHook {
    /**
     * Fills the tool with materials
     */
    MaterialNBT fillMaterials(ToolDefinition definition, Random random);
}
