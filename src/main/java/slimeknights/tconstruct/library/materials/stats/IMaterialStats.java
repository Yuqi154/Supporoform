package slimeknights.tconstruct.library.materials.stats;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.utils.Util;

import java.util.List;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Basic interface for all material stats.
 */
public interface IMaterialStats {
    /**
     * Returns the stat type, which is used for parsing the stat and getting default stats.
     */
    MaterialStatType<?> getType();

    /**
     * Returns a unique ResourceLocation to identify the type of stats the material has.
     */
    @NonExtendable
    default MaterialStatsId getIdentifier() {
        return getType().getId();
    }

    /**
     * Returns the name of the stat type, to be displayed to the player.
     */
    default MutableText getLocalizedName() {
        return Text.translatable(Util.makeTranslationKey("stat", getIdentifier()));
    }

    /**
     * Returns a list containing a String for each player-relevant value.</br>
     * Each line should consist of the name of the value followed by the value itself.</br>
     * Example: "Durability: 25"</br>
     * </br>
     * This is used to display properties of materials to the user.
     */
    List<Text> getLocalizedInfo();

    /**
     * Returns a list containing a Text Component describing each player-relevant value.</br>
     * The indices of the lines must line up with the lines from getLocalizedInfo()!</br>
     * *
     * This is used to display properties of materials to the user.
     *
     * @return a list of Text Components
     */
    List<Text> getLocalizedDescriptions();

    /**
     * Applies this stat to the builder
     *
     * @param builder Builder instance
     * @param scale   Scaling factor for applying these stats, used to allow multiple stats of the same type to exist on one tool
     */
    void apply(ModifierStatsBuilder builder, float scale);


    /* Helpers */

    /**
     * Helper to make a translation key for the given name
     *
     * @param name name
     * @return Text component
     */
    static String makeTooltipKey(Identifier name) {
        return Util.makeTranslationKey("tool_stat", name);
    }

    /**
     * Helper to make a text component for the given name
     *
     * @param name name
     * @return Text component
     */
    static Text makeTooltip(Identifier name) {
        return Text.translatable(makeTooltipKey(name));
    }
}
