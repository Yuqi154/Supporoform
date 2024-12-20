package slimeknights.tconstruct.library.modifiers.fluid;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.SingletonLoader;
import slimeknights.tconstruct.library.events.teleport.FluidEffectTeleportEvent;
import slimeknights.tconstruct.library.utils.TeleportHelper;

/**
 * Represents an effect applied by a fluid to an entity or block
 */
public interface FluidEffect<C extends FluidEffectContext> extends IHaveLoader, UnloadableFluidEffect<C> {
    /**
     * Registry for fluid effect loaders
     */
    GenericLoaderRegistry<FluidEffect<? super FluidEffectContext.Block>> BLOCK_EFFECTS = new GenericLoaderRegistry<>("Fluid block effect", false);
    /**
     * Registry for fluid effect loaders
     */
    GenericLoaderRegistry<FluidEffect<? super FluidEffectContext.Entity>> ENTITY_EFFECTS = new GenericLoaderRegistry<>("Fluid entity effect", false);

    /**
     * Registers an effect to both blocks and entities
     */
    static void registerGeneral(Identifier id, IGenericLoader<? extends FluidEffect<FluidEffectContext>> loader) {
        BLOCK_EFFECTS.register(id, loader);
        ENTITY_EFFECTS.register(id, loader);
    }

    @Override
    IGenericLoader<? extends FluidEffect<C>> getLoader();


    /* Singletons */

    /**
     * Effect that does nothing
     */
    FluidEffect<FluidEffectContext> EMPTY = simple(((fluid, scale, context, action) -> 0));

    /**
     * Effect which extinguishes fire from the entity
     */
    FluidEffect<FluidEffectContext.Entity> EXTINGUISH_FIRE = simple((fluid, level, context, action) -> {
        Entity target = context.getTarget();
        if (target.isOnFire() && level.isFull()) {
            context.getTarget().setOnFire(false);
            return 1;
        }
        return 0;
    });

    /**
     * Effect which randomly teleports the target
     */
    FluidEffect<FluidEffectContext.Entity> TELEPORT = simple((fluid, level, context, action) -> {
        LivingEntity target = context.getLivingTarget();
        if (target != null && level.isFull()) {
            TeleportHelper.randomNearbyTeleport(target, FluidEffectTeleportEvent.TELEPORT_FACTORY);
            return 1;
        }
        return 0;
    });


    /**
     * Creates a simple fluid effect with no JSON parameters
     */
    static <C extends FluidEffectContext> FluidEffect<C> simple(UnloadableFluidEffect<C> effect) {
        return SingletonLoader.singleton(loader -> new FluidEffect<>() {
            @Override
            public IGenericLoader<? extends FluidEffect<C>> getLoader() {
                return loader;
            }

            @Override
            public float apply(FluidStack fluid, EffectLevel level, C context, FluidAction action) {
                return effect.apply(fluid, level, context, FluidAction.EXECUTE);
            }
        });
    }
}
