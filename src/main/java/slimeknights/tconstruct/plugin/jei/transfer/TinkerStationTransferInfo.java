package slimeknights.tconstruct.plugin.jei.transfer;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.tools.item.ArmorSlotType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TinkerStationTransferInfo<T>(
        RecipeType<T> getRecipeType) implements IRecipeTransferInfo<TinkerStationContainerMenu, T> {
    @Override
    public Class<TinkerStationContainerMenu> getContainerClass() {
        return TinkerStationContainerMenu.class;
    }

    @Override
    public Optional<ScreenHandlerType<TinkerStationContainerMenu>> getMenuType() {
        return Optional.of(TinkerTables.tinkerStationContainer.get());
    }

    @Override
    public boolean canHandle(TinkerStationContainerMenu container, T recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(TinkerStationContainerMenu container, T recipe) {
        return container.getInputSlots();
    }

    @Override
    public List<Slot> getInventorySlots(TinkerStationContainerMenu container, T recipe) {
        List<Slot> slots = new ArrayList<>();
        // skip over inputs, output slot, tool slot, armor, and offhand
        int start = container.getInputSlots().size() + 3 + ArmorSlotType.values().length;
        for (int i = start; i < start + 36; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }

        return slots;
    }
}
