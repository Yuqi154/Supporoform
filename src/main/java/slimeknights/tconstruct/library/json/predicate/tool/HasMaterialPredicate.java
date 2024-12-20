package slimeknights.tconstruct.library.json.predicate.tool;

import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

/**
 * Tool predicate checking for the given material on the tool
 *
 * @param material Material variant to locate.
 * @param index    Index to check for the material. If -1, will check all materials on the tool.
 */
public record HasMaterialPredicate(MaterialVariantId material, int index) implements ToolContextPredicate {
    public static final RecordLoadable<HasMaterialPredicate> LOADER = RecordLoadable.create(
            MaterialVariantId.LOADABLE.requiredField("material", HasMaterialPredicate::material),
            IntLoadable.FROM_MINUS_ONE.defaultField("index", -1, HasMaterialPredicate::index),
            HasMaterialPredicate::new);

    public HasMaterialPredicate(MaterialVariantId material) {
        this(material, -1);
    }

    @Override
    public boolean matches(IToolContext input) {
        // if given an index, use exact location match
        if (this.index >= 0) {
            return this.material.matchesVariant(input.getMaterial(this.index));
        }
        // otherwise, search each material
        for (MaterialVariant variant : input.getMaterials().getList()) {
            if (this.material.matchesVariant(variant)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IGenericLoader<? extends ToolContextPredicate> getLoader() {
        return LOADER;
    }
}
