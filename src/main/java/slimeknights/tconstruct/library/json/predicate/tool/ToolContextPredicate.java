package slimeknights.tconstruct.library.json.predicate.tool;

import slimeknights.mantle.data.predicate.FallbackPredicateRegistry;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.SingletonLoader;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

/**
 * Simple serializable tool predicate, for early loading context
 */
public interface ToolContextPredicate extends IJsonPredicate<IToolContext> {
    /**
     * Predicate that matches all tools
     */
    ToolContextPredicate ANY = simple(tool -> true);
    /**
     * Loader for tool predicates
     */
    FallbackPredicateRegistry<IToolContext, Item> LOADER = new FallbackPredicateRegistry<>("Tool Context Predicate", ANY, ItemPredicate.LOADER, IToolContext::getItem, "item");

    @Override
    default IJsonPredicate<IToolContext> inverted() {
        return LOADER.invert(this);
    }


    /* Singleton */

    /**
     * Predicate checking for a tool having any upgrades
     */
    ToolContextPredicate HAS_UPGRADES = simple(tool -> !tool.getUpgrades().isEmpty());

    /**
     * Creates a new simple predicate
     */
    static ToolContextPredicate simple(Predicate<IToolContext> predicate) {
        return SingletonLoader.singleton(loader -> new ToolContextPredicate() {
            @Override
            public boolean matches(IToolContext tool) {
                return predicate.test(tool);
            }

            @Override
            public IGenericLoader<? extends ToolContextPredicate> getLoader() {
                return loader;
            }
        });
    }


    /* Helper methods */

    /**
     * Creates an item predicate
     */
    static IJsonPredicate<IToolContext> fallback(IJsonPredicate<Item> predicate) {
        return LOADER.fallback(predicate);
    }

    /**
     * Creates an item set predicate
     */
    static IJsonPredicate<IToolContext> set(Item... items) {
        return LOADER.fallback(ItemPredicate.set(items));
    }

    /**
     * Creates a tag predicate
     */
    static IJsonPredicate<IToolContext> tag(TagKey<Item> tag) {
        return LOADER.fallback(ItemPredicate.tag(tag));
    }

    /**
     * Creates an and predicate
     */
    @SafeVarargs
    static IJsonPredicate<IToolContext> and(IJsonPredicate<IToolContext>... predicates) {
        return LOADER.and(List.of(predicates));
    }

    /**
     * Creates an or predicate
     */
    @SafeVarargs
    static IJsonPredicate<IToolContext> or(IJsonPredicate<IToolContext>... predicates) {
        return LOADER.or(List.of(predicates));
    }
}
