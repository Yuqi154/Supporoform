package slimeknights.tconstruct.library.json.variable.tool;

import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.tconstruct.library.json.predicate.tool.ToolContextPredicate;
import slimeknights.tconstruct.library.json.variable.ConditionalVariable;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Gets one of two entity properties based on the condition
 */
public record ConditionalToolVariable(IJsonPredicate<IToolContext> condition, ToolVariable ifTrue,
                                      ToolVariable ifFalse) implements ToolVariable, ConditionalVariable<IJsonPredicate<IToolContext>, ToolVariable> {
    public static final IGenericLoader<ConditionalToolVariable> LOADER = ConditionalVariable.loadable(ToolContextPredicate.LOADER, ToolVariable.LOADER, ConditionalToolVariable::new);

    @Override
    public float getValue(IToolStackView tool) {
        return this.condition.matches(tool) ? this.ifTrue.getValue(tool) : this.ifFalse.getValue(tool);
    }

    @Override
    public IGenericLoader<? extends ToolVariable> getLoader() {
        return LOADER;
    }
}
