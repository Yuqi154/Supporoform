package slimeknights.tconstruct.tools.modifiers.traits.skull;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.modules.technical.ArmorLevelModule;
import slimeknights.tconstruct.library.modifiers.modules.technical.CureOnRemovalModule;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class StrongBonesModifier extends NoLevelsModifier {
    /**
     * Key for modifiers that are boosted by drinking milk
     */
    public static final TinkerDataKey<Integer> CALCIFIABLE = TConstruct.createKey("calcifable");
    /**
     * Module to add to any calcifiable modifiers
     */
    public static final ArmorLevelModule CALCIFIABLE_MODULE = new ArmorLevelModule(CALCIFIABLE, false, TinkerTags.Items.HELD_ARMOR);

    public StrongBonesModifier() {
        // TODO: move this out of constructor to generalized logic
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LivingEntityUseItemEvent.Finish.class, StrongBonesModifier::onItemFinishUse);
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addModule(CureOnRemovalModule.HELMET);
    }

    private static boolean drinkMilk(LivingEntity living, int duration) {
        // strong bones has to be the helmet as we use it for curing
        // TODO 1.20: can use the new cure effects to make this work in any slot
        ItemStack helmet = living.getEquippedStack(EquipmentSlot.HEAD);
        boolean didSomething = false;
        if (ModifierUtil.getModifierLevel(helmet, TinkerModifiers.strongBones.getId()) > 0) {
            StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.RESISTANCE, duration);
            effect.getCurativeItems().clear();
            effect.getCurativeItems().add(new ItemStack(helmet.getItem()));
            didSomething = living.addStatusEffect(effect);
        }
        if (ArmorLevelModule.getLevel(living, CALCIFIABLE) > 0) {
            didSomething |= living.addStatusEffect(new StatusEffectInstance(TinkerModifiers.calcifiedEffect.get(), duration, 0));
        }
        return didSomething;
    }

    /**
     * Called when you finish drinking milk
     */
    private static void onItemFinishUse(LivingEntityUseItemEvent.Finish event) {
        LivingEntity living = event.getEntity();
        if (event.getItem().getItem() == Items.MILK_BUCKET) {
            drinkMilk(living, 1200);
        }
    }


    /* Spilling effect */

    /**
     * Singleton instance spilling effect
     */
    public static final FluidEffect<FluidEffectContext.Entity> FLUID_EFFECT = FluidEffect.simple((fluid, scale, context, action) -> {
        LivingEntity target = context.getLivingTarget();
        // while we could scale, doing it flat ensures we don't charge extra
        if (target != null && drinkMilk(target, (int) (400 * scale.value()))) {
            return scale.value();
        }
        return 0;
    });
}
