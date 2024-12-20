package slimeknights.tconstruct.common.registration;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.StringIdentifiable;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.deferred.DeferredRegisterWrapper;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic deferred register for an object using registry objects and wanting enums
 */
public class EnumDeferredRegister<T> extends DeferredRegisterWrapper<T> {
    public EnumDeferredRegister(RegistryKey<Registry<T>> reg, String modID) {
        super(reg, modID);
    }

    /**
     * Registers a standard object
     */
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> value) {
        return this.register.register(name, value);
    }

    /**
     * Registers an object with multiple variants, prefixing the name with the value name
     *
     * @param values Enum values to use for this item
     * @param name   Name of the object
     * @param mapper Function to get an object for the given enum value
     * @return EnumObject mapping between different item types
     */
    public <E extends Enum<E> & StringIdentifiable, I extends T> EnumObject<E, I> registerEnum(E[] values, String name, Function<E, ? extends I> mapper) {
        return registerEnum(values, name, (fullName, type) -> this.register(fullName, () -> mapper.apply(type)));
    }

    /**
     * Registers an object with multiple variants, suffixing the name with the value name
     *
     * @param values Enum values to use for this item
     * @param name   Name of the object
     * @param mapper Function to get an object for the given enum value
     * @return EnumObject mapping between different item types
     */
    public <E extends Enum<E> & StringIdentifiable, I extends T> EnumObject<E, I> registerEnum(String name, E[] values, Function<E, ? extends I> mapper) {
        return registerEnum(name, values, (fullName, type) -> this.register(fullName, () -> mapper.apply(type)));
    }
}
