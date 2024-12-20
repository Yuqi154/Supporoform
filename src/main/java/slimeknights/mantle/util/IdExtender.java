package slimeknights.mantle.util;

import net.minecraft.util.Identifier;

/**
 * Simple helper methods to extend a resource location, either by prefixing or suffixing values
 */
public interface IdExtender<T extends Identifier> {
    /**
     * Extender for standard resource locations
     */
    LocationExtender INSTANCE = new LocationExtender() {
    };

    /**
     * Creates a resource location
     */
    T location(String namespace, String path);

    /**
     * Wraps the resource location in the given prefix and suffix
     *
     * @param location Location to extend
     * @param prefix   Path prefix
     * @param suffix   Path suffix
     * @return Location with the given prefix and suffix
     */
    default T wrap(Identifier location, String prefix, String suffix) {
        return this.location(location.getNamespace(), prefix + location.getPath() + suffix);
    }

    /**
     * Prefixes the resource location
     *
     * @param location Location to extend
     * @param prefix   Path prefix
     * @return Location with the given prefix
     */
    default T prefix(Identifier location, String prefix) {
        return this.location(location.getNamespace(), prefix + location.getPath());
    }

    /**
     * Suffixes the resource location
     *
     * @param location Location to extend
     * @param suffix   Path suffix
     * @return Location with the given suffix
     */
    default T suffix(Identifier location, String suffix) {
        return this.location(location.getNamespace(), location.getPath() + suffix);
    }

    /**
     * Extender for specifically resource locations, used in recipe helpers
     */
    interface LocationExtender extends IdExtender<Identifier> {
        @Override
        default Identifier location(String namespace, String path) {
            return new Identifier(namespace, path);
        }
    }
}
