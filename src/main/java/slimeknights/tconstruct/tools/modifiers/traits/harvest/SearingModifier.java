package slimeknights.tconstruct.tools.modifiers.traits.harvest;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearingModifier extends Modifier implements BreakSpeedModifierHook, TooltipModifierHook {
    /**
     * Container for melting recipe lookup
     */
    private static final SearingContainer CONTAINER = new SearingContainer();
    /**
     * Cache of item forms of blocks which have a boost
     */
    private static final Map<Item, Boolean> BOOSTED_BLOCKS = new ConcurrentHashMap<>();

    static {
        RecipeCacheInvalidator.addReloadListener(client -> BOOSTED_BLOCKS.clear());
    }

    /**
     * Checks if the modifier is effective on the given block state
     */
    private static boolean isEffective(World world, Item item) {
        CONTAINER.setStack(new ItemStack(item));
        boolean effective = world.getRecipeManager().getFirstMatch(TinkerRecipeTypes.MELTING.get(), CONTAINER, world).isPresent();
        CONTAINER.setStack(ItemStack.EMPTY);
        return effective;
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BREAK_SPEED, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (isEffective) {
            BlockState state = event.getState();
            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                World world = event.getEntity().level;
                // +7 per level if it has a melting recipe, cache to save lookup time
                // TODO: consider whether we should use getCloneItemStack, problem is I don't want a position based logic and its possible the result is BE based
                if (BOOSTED_BLOCKS.computeIfAbsent(item, i -> isEffective(world, i)) == Boolean.TRUE) {
                    event.setNewSpeed(event.getNewSpeed() + modifier.getLevel() * 6 * tool.getMultiplier(ToolStats.MINING_SPEED) * miningSpeedModifier);
                }
            }
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable PlayerEntity player, List<Text> tooltip, TooltipKey tooltipKey, TooltipContext tooltipFlag) {
        TooltipModifierHook.addStatBoost(tool, this, ToolStats.MINING_SPEED, TinkerTags.Items.HARVEST, 7 * modifier.getLevel(), tooltip);
    }

    /**
     * Container implementation for recipe lookup
     */
    private static class SearingContainer implements IMeltingContainer {
        @Getter
        @Setter
        private ItemStack stack = ItemStack.EMPTY;

        @Override
        public IOreRate getOreRate() {
            return Config.COMMON.melterOreRate;
        }
    }
}