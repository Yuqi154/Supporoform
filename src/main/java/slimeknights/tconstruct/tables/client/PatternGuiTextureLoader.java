package slimeknights.tconstruct.tables.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import slimeknights.mantle.data.listener.ResourceValidator;

/**
 * Stitches all GUI part textures into the texture sheet
 */
public class PatternGuiTextureLoader extends ResourceValidator {
    /**
     * Initializes the loader
     */
    public static void init() {
        PatternGuiTextureLoader loader = new PatternGuiTextureLoader();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventPriority.NORMAL, false, TextureStitchEvent.Pre.class, loader::onTextureStitch);
    }

    private PatternGuiTextureLoader() {
        super("textures/gui/tinker_pattern", "textures", ".png");
    }

    /**
     * Called during texture stitch to add the textures in
     */
    private void onTextureStitch(TextureStitchEvent.Pre event) {
        if (PlayerScreenHandler.BLOCK_ATLAS_TEXTURE.equals(event.getAtlas().location())) {
            // manually call reload to ensure it runs at the proper time
            this.onReloadSafe(MinecraftClient.getInstance().getResourceManager());
            this.resources.forEach(event::addSprite);
            this.clear();
        }
    }
}
