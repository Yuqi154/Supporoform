package slimeknights.tconstruct.tables.menu;

import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import slimeknights.mantle.util.sync.LambdaDataSlot;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.LazyResultSlot;

import org.jetbrains.annotations.Nullable;

public class PartBuilderContainerMenu extends TabbedContainerMenu<PartBuilderBlockEntity> {
    // slots
    @Getter
    private final Slot patternSlot;
    @Getter
    private final Slot inputSlot;
    @Getter
    private final LazyResultSlot outputSlot;

    public PartBuilderContainerMenu(int windowIdIn, PlayerInventory playerInventoryIn, @Nullable PartBuilderBlockEntity partBuilderTileEntity) {
        super(TinkerTables.partBuilderContainer.get(), windowIdIn, playerInventoryIn, partBuilderTileEntity);

        // unfortunately, nothing works with no tile
        if (tile != null) {
            // slots
            this.addSlot(this.outputSlot = new LazyResultSlot(tile.getCraftingResult(), 148, 33));
            // inputs
            this.addSlot(this.patternSlot = new PatternSlot(tile, 8, 34));
            this.addSlot(this.inputSlot = new MaterialSlot(tile, PartBuilderBlockEntity.MATERIAL_SLOT, 29, 34));

            // other inventories
            this.addChestSideInventory();
            this.addInventorySlots();

            // listen for the button to change in the tile
            this.addProperty(new LambdaDataSlot(-1, tile::getSelectedIndex, i -> {
                tile.selectRecipe(i);
                this.updateScreen();
            }));
            // update for the first time
            this.updateScreen();
        } else {
            this.patternSlot = null;
            this.inputSlot = null;
            this.outputSlot = null;
        }
    }

    public PartBuilderContainerMenu(int id, PlayerInventory inv, PacketByteBuf buf) {
        this(id, inv, getTileEntityFromBuf(buf, PartBuilderBlockEntity.class));
    }

    @Override
    public void onContentChanged(Inventory inventoryIn) {
    }

    /**
     * Called when a pattern button is pressed
     */
    @Override
    public boolean onButtonClick(PlayerEntity playerIn, int id) {
        // no letting ghosts choose patterns
        if (playerIn.isSpectator()) {
            return false;
        }
        if (id >= 0 && tile != null) {
            tile.selectRecipe(id);
        }
        return true;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slotIn) {
        return slotIn != this.outputSlot && super.canInsertIntoSlot(stack, slotIn);
    }

    /**
     * Slot to update recipe on change
     */
    private static class PartBuilderSlot extends Slot {
        private final LazyResultContainer craftResult;

        public PartBuilderSlot(PartBuilderBlockEntity tile, int index, int xPosition, int yPosition) {
            super(tile, index, xPosition, yPosition);
            craftResult = tile.getCraftingResult();
        }

        @Override
        public void markDirty() {
            craftResult.clear();
            super.markDirty();
        }
    }

    /**
     * Slot for the material, which wants to force a screen update
     */
    private class MaterialSlot extends PartBuilderSlot {
        public MaterialSlot(PartBuilderBlockEntity tile, int index, int xPosition, int yPosition) {
            super(tile, index, xPosition, yPosition);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            updateScreen(); // no other good way to detect stack size decreasing, e.g. on right click
        }
    }

    /**
     * Slot for the pattern, updates buttons on change
     */
    private static class PatternSlot extends PartBuilderSlot {
        private PatternSlot(PartBuilderBlockEntity tile, int x, int y) {
            super(tile, PartBuilderBlockEntity.PATTERN_SLOT, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isIn(TinkerTags.Items.PATTERNS);
        }
    }
}