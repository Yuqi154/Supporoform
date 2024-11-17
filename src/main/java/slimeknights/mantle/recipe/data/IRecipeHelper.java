package slimeknights.mantle.recipe.data;

import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.mantle.util.IdExtender.LocationExtender;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Interface for common resource location and condition methods
 */
@SuppressWarnings("unused")
public interface IRecipeHelper extends LocationExtender {
    /* Location helpers */

    /**
     * Gets the ID of the mod adding recipes
     */
    String getModId();

    /**
     * Use {@link #location(String)}, this method just exists to simplify implementation.
     */
    @ApiStatus.Internal
    @Override
    default Identifier location(String namespace, String path) {
        return this.location(path);
    }

    /**
     * Gets a resource location for the mod
     *
     * @param name Location path
     * @return Location for the mod
     */
    default Identifier location(String name) {
        return new Identifier(this.getModId(), name);
    }

    /**
     * Gets a resource location string for your mod
     *
     * @param id Location path
     * @return Location for your mod as a string
     */
    default String prefix(String id) {
        return this.getModId() + ":" + id;
    }

    /**
     * Gets a registry ID for the given item
     *
     * @param item Item to fetch ID
     * @return ID for the item put in your namespace
     */
    @SuppressWarnings("deprecation")  // won't be for long
    default Identifier id(ItemConvertible item) {
        return this.location(Registries.ITEM.getId(item.asItem()).getPath());
    }

    /**
     * Gets a registry ID for the given item
     *
     * @param registry Registry to fetch IDs
     * @param value    Registry value
     * @return ID for the item put in your namespace
     */
    default <T> Identifier id(Registry<T> registry, T value) {
        return this.location(Objects.requireNonNull(registry.getId(value)).getPath());
    }


    /* Registry object location helpers */

    /**
     * Wraps the registry object ID in the given prefix and suffix
     *
     * @param location Object to use for location
     * @param prefix   Path prefix
     * @param suffix   Path suffix
     * @return Location with the given prefix and suffix
     */
    default Identifier wrap(RegistryObject<?> location, String prefix, String suffix) {
        return wrap(location.getId(), prefix, suffix);
    }

    /**
     * Prefixes the registry object ID
     *
     * @param location Object to use for location
     * @param prefix   Path prefix
     * @return Location with the given prefix
     */
    default Identifier prefix(RegistryObject<?> location, String prefix) {
        return prefix(location.getId(), prefix);
    }

    /**
     * Suffixes the registry object ID
     *
     * @param location Object to use for location
     * @param suffix   Path suffix
     * @return Location with the given suffix
     */
    default Identifier suffix(RegistryObject<?> location, String suffix) {
        return suffix(location.getId(), suffix);
    }


    /* Other named object location helpers */

    /**
     * Wraps the registry object ID in the given prefix and suffix
     *
     * @param location Object to use for location
     * @param prefix   Path prefix
     * @param suffix   Path suffix
     * @return Location with the given prefix and suffix
     */
    default Identifier wrap(IdAwareObject location, String prefix, String suffix) {
        return this.wrap(location.getId(), prefix, suffix);
    }

    /**
     * Prefixes the registry object ID
     *
     * @param location Object to use for location
     * @param prefix   Path prefix
     * @return Location with the given prefix
     */
    default Identifier prefix(IdAwareObject location, String prefix) {
        return this.prefix(location.getId(), prefix);
    }

    /**
     * Suffixes the registry object ID
     *
     * @param location Object to use for location
     * @param suffix   Path suffix
     * @return Location with the given suffix
     */
    default Identifier suffix(IdAwareObject location, String suffix) {
        return this.suffix(location.getId(), suffix);
    }


    /* Tags and conditions */

    /**
     * Gets a tag by name
     *
     * @param modId Mod ID for tag
     * @param name  Tag name
     * @return Tag instance
     */
    default TagKey<Item> getItemTag(String modId, String name) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(modId, name));
    }

    /**
     * Gets a tag by name
     *
     * @param modId Mod ID for tag
     * @param name  Tag name
     * @return Tag instance
     */
    default TagKey<Fluid> getFluidTag(String modId, String name) {
        return TagKey.of(RegistryKeys.FLUID, new Identifier(modId, name));
    }

    /**
     * Creates a condition for a tag existing
     *
     * @param name Forge tag name
     * @return Condition for tag existing
     */
    default ICondition tagCondition(String name) {
        return new NotCondition(new TagEmptyCondition("forge", name));
    }

    /**
     * Creates a consumer instance with the added conditions
     *
     * @param consumer   Base consumer
     * @param conditions Extra conditions
     * @return Wrapped consumer
     */
    default Consumer<RecipeJsonProvider> withCondition(Consumer<RecipeJsonProvider> consumer, ICondition... conditions) {
        ConsumerWrapperBuilder builder = ConsumerWrapperBuilder.wrap();
        for (ICondition condition : conditions) {
            builder.addCondition(condition);
        }
        return builder.build(consumer);
    }
}