package slimeknights.tconstruct.library.json.variable.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/**
 * Fetches a value of an integer property
 */
public record StatePropertyVariable(String name) implements BlockVariable {
    public static final RecordLoadable<StatePropertyVariable> LOADER = RecordLoadable.create(StringLoadable.DEFAULT.requiredField("name", StatePropertyVariable::name), StatePropertyVariable::new);

    @Override
    public float getValue(BlockState state) {
        Property<?> property = state.getBlock().getStateManager().getProperty(this.name);
        if (property != null && Number.class.isAssignableFrom(property.getType())) {
            return ((Number) state.get(property)).floatValue();
        }
        return 0;
    }

    @Override
    public IGenericLoader<? extends BlockVariable> getLoader() {
        return LOADER;
    }
}
