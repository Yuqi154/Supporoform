package slimeknights.tconstruct.tools.modifiers.traits.melee;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.NamespacedNBT;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class LaceratingModifier extends Modifier implements ProjectileHitModifierHook, MeleeHitModifierHook, OnAttackedModifierHook {
    /**
     * Applies the effect to the target
     */
    private static void applyEffect(LivingEntity target, int level) {
        // potions are 0 indexed instead of 1 indexed
        // 81 ticks will do about 5 damage at level 1
        TinkerModifiers.bleeding.get().apply(target, 1 + 20 * (2 + (RANDOM.nextInt(level + 3))), level - 1, true);
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        // 50% chance of applying
        LivingEntity target = context.getLivingTarget();
        if (target != null && context.isFullyCharged() && target.isAlive() && RANDOM.nextFloat() < 0.50f) {
            // set entity so the potion is attributed as a player kill
            target.onAttacking(context.getAttacker());
            applyEffect(target, modifier.getLevel());
        }
    }

    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, NamespacedNBT persistentData, ModifierEntry modifier, ProjectileEntity projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        if (target != null && (!(projectile instanceof PersistentProjectileEntity arrow) || arrow.isCritical()) && target.isAlive() && RANDOM.nextFloat() < 0.50f) {
            Entity owner = projectile.getOwner();
            if (owner != null) {
                target.onAttacking(owner);
            }
            applyEffect(target, modifier.getLevel());
        }
        return false;
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        // this works like vanilla, damage is capped due to the hurt immunity mechanics, so if multiple pieces apply thorns between us and vanilla, damage is capped at 4
        if (isDirectDamage && source.getAttacker() instanceof LivingEntity attacker) {
            // 15% chance of working per level, doubled bonus on shields
            int level = modifier.getLevel();
            if (slotType.getType() == Type.HAND) {
                level *= 2;
            }
            if (RANDOM.nextFloat() < (level * 0.25f)) {
                applyEffect(attacker, modifier.getLevel());
            }
        }
    }
}
