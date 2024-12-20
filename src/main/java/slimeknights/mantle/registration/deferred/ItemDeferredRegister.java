package slimeknights.mantle.registration.deferred;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringIdentifiable;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register that registers items with wrappers
 */
@SuppressWarnings("unused")
public class ItemDeferredRegister extends DeferredRegisterWrapper<Item> {

    public ItemDeferredRegister(String modID) {
        super(modID);
    }

    /**
     * Adds a new item to the list to be registered, using the given supplier
     *
     * @param name Item name
     * @param sup  Supplier returning an item
     * @return Item registry object
     */
    public <I extends Item> ItemObject<I> register(String name, Supplier<? extends I> sup) {
        return new ItemObject<>(RegistryEntry.of(Registry.register(Registries.ITEM, TConstruct.getResource(name), sup.get())));
    }

    /**
     * Adds a new item to the list to be registered, based on the given item properties
     *
     * @param name  Item name
     * @param props Item properties
     * @return Item registry object
     */
    public ItemObject<Item> register(String name, Item.Settings props) {
        return this.register(name, () -> new Item(props));
    }


    /* Specialty */

    /**
     * Registers an item with multiple variants, prefixing the name with the value name
     *
     * @param values Enum values to use for this item
     * @param name   Name of the block
     * @param mapper Function to get a item for the given enum value
     * @return EnumObject mapping between different item types
     */
    public <T extends Enum<T> & StringIdentifiable, I extends Item> EnumObject<T, I> registerEnum(T[] values, String name, Function<T, ? extends I> mapper) {
        return registerEnum(values, name, (fullName, type) -> this.register(fullName, () -> mapper.apply(type)));
    }

    /**
     * Registers an item with multiple variants, suffixing the name with the value name
     *
     * @param values Enum values to use for this item
     * @param name   Name of the block
     * @param mapper Function to get a item for the given enum value
     * @return EnumObject mapping between different item types
     */
    public <T extends Enum<T> & StringIdentifiable, I extends Item> EnumObject<T, I> registerEnum(String name, T[] values, Function<T, ? extends I> mapper) {
        return registerEnum(name, values, (fullName, type) -> this.register(fullName, () -> mapper.apply(type)));
    }
}
