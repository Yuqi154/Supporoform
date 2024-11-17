package slimeknights.tconstruct.tools.modifiers.ability.ranged;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.item.CrystalshotItem;

import java.util.function.Predicate;

public class CrystalshotModifier extends NoLevelsModifier implements BowAmmoModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BOW_AMMO);
    }

    @Override
    public int getPriority() {
        return 60; // before bulk quiver, after
    }

    @Override
    public Text getDisplayName(IToolStackView tool, ModifierEntry entry) {
        // color the display name for the variant
        String variant = tool.getPersistentData().getString(this.getId());
        if (!variant.isEmpty()) {
            String key = this.getTranslationKey();
            return Text.translatable(this.getTranslationKey())
                    .styled(style -> style.withColor(ResourceColorManager.getTextColor(key + "." + variant)));
        }
        return super.getDisplayName();
    }

    @Override
    public ItemStack findAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack standardAmmo, Predicate<ItemStack> ammoPredicate) {
        return CrystalshotItem.withVariant(tool.getPersistentData().getString(this.getId()), 64);
    }

    @Override
    public void shrinkAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack ammo, int needed) {
        ToolDamageUtil.damageAnimated(tool, 4 * needed, shooter, shooter.getActiveHand());
    }
}