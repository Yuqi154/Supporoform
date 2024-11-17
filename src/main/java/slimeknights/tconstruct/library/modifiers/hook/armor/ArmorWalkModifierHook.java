package slimeknights.tconstruct.library.modifiers.hook.armor;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Modifier hook for boots when the player walks.
 */
public interface ArmorWalkModifierHook {
    /**
     * Called when an entity's block position changes
     *
     * @param tool     Tool in boots slot
     * @param modifier Entry calling this hook
     * @param living   Living entity instance
     * @param prevPos  Previous block position
     * @param newPos   New block position, will match the entity's position
     */
    void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos, BlockPos newPos);


    /**
     * Walk modifier hook merger: runs hooks of all children
     */
    record AllMerger(Collection<ArmorWalkModifierHook> modules) implements ArmorWalkModifierHook {
        @Override
        public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos, BlockPos newPos) {
            for (ArmorWalkModifierHook module : this.modules) {
                module.onWalk(tool, modifier, living, prevPos, newPos);
            }
        }
    }
}