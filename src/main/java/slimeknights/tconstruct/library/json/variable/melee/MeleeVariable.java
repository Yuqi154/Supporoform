package slimeknights.tconstruct.library.json.variable.melee;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.tconstruct.library.json.variable.VariableLoaderRegistry;
import slimeknights.tconstruct.library.modifiers.modules.combat.ConditionalMeleeDamageModule;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.LivingEntity;

/**
 * Variable for use in {@link ConditionalMeleeDamageModule}
 */
public interface MeleeVariable extends IHaveLoader {
    GenericLoaderRegistry<MeleeVariable> LOADER = new VariableLoaderRegistry<>("Melee Variable", Constant::new);

    /**
     * Gets the value of the variable
     *
     * @param tool     Tool instance
     * @param context  Attack context, will be null in tooltips
     * @param attacker Entity using the tool, may be null conditionally in tooltips
     * @return Value of this variable
     */
    float getValue(IToolStackView tool, @Nullable ToolAttackContext context, @Nullable LivingEntity attacker);


    /**
     * Constant value instance for this object
     */
    record Constant(float value) implements VariableLoaderRegistry.ConstantFloat, MeleeVariable {
        public static final RecordLoadable<Constant> LOADER = VariableLoaderRegistry.constantLoader(Constant::new);

        @Override
        public float getValue(IToolStackView tool, @Nullable ToolAttackContext context, @Nullable LivingEntity attacker) {
            return value;
        }

        @Override
        public IGenericLoader<? extends MeleeVariable> getLoader() {
            return LOADER;
        }
    }
}
