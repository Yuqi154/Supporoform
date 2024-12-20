package slimeknights.tconstruct.tables.client.inventory.widget;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

public class SlotButtonItem extends ButtonWidget {

    public static int WIDTH = 18, HEIGHT = 18;

    protected static final ElementScreen BUTTON_PRESSED_GUI = new ElementScreen(144, 216, WIDTH, HEIGHT, 256, 256);
    protected static final ElementScreen BUTTON_NORMAL_GUI = new ElementScreen(144 + WIDTH * 2, 216, WIDTH, HEIGHT, 256, 256);
    protected static final ElementScreen BUTTON_HOVER_GUI = new ElementScreen(144 + WIDTH * 4, 216, WIDTH, HEIGHT, 256, 256);

    @Getter
    private final StationSlotLayout layout;
    public boolean pressed;
    public final int buttonId;

    private ElementScreen pressedGui = BUTTON_PRESSED_GUI;
    private ElementScreen normalGui = BUTTON_NORMAL_GUI;
    private ElementScreen hoverGui = BUTTON_HOVER_GUI;
    private Identifier backgroundLocation = Icons.ICONS;

    public SlotButtonItem(int buttonId, int x, int y, StationSlotLayout layout, PressAction onPress) {
        super(x, y, WIDTH, HEIGHT, layout.getDisplayName(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.layout = layout;
        this.buttonId = buttonId;
    }

    public SlotButtonItem setGraphics(ElementScreen normal, ElementScreen hover, ElementScreen pressed, Identifier background) {
        this.pressedGui = pressed;
        this.normalGui = normal;
        this.hoverGui = hover;
        this.backgroundLocation = background;

        return this;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        RenderUtils.setup(this.backgroundLocation);

        if (this.visible) {
            this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            if (this.pressed) {
                this.pressedGui.draw(context, this.getX(), this.getY());
            } else if (this.hovered) {
                this.hoverGui.draw(context, this.getX(), this.getY());
            } else {
                this.normalGui.draw(context, this.getX(), this.getY());
            }

            //this.drawIcon(context, Minecraft.getInstance());
            TinkerStationScreen.renderIcon(context, this.layout.getIcon(), this.getX() + 1, this.getY() + 1);
        }
    }

//  protected void drawIcon(MatrixStack matrices, Minecraft mc) {
//    mc.getItemRenderer().renderItemIntoGUI(this.icon, this.x + 1, this.y + 1);
//  }
}
