package slimeknights.tconstruct.library.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.fabricators_of_create.porting_lib.util.CraftingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.OnDatapackSyncEvent;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.network.TinkerNetwork;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Helpers for a few JSON related tasks
 */
public class JsonUtils {
    private JsonUtils() {
    }

    /**
     * Reads an integer with a minimum value
     *
     * @param json Json
     * @param key  Key to read
     * @param min  Minimum and default value
     * @return Read int
     * @throws JsonSyntaxException if the key is not an int or below the min
     */
    public static int getIntMin(JsonObject json, String key, int min) {
        int value = net.minecraft.util.JsonHelper.getInt(json, key, min);
        if (value < min) {
            throw new JsonSyntaxException(key + " must be at least " + min);
        }
        return value;
    }

    /**
     * Reads an integer with a minimum value
     *
     * @param json Json element to parse as an integer
     * @param key  Key to read
     * @param min  Minimum
     * @return Read int
     * @throws JsonSyntaxException if the key is not an int or below the min
     */
    public static int convertToIntMin(JsonElement json, String key, int min) {
        int value = net.minecraft.util.JsonHelper.asInt(json, key);
        if (value < min) {
            throw new JsonSyntaxException(key + " must be at least " + min);
        }
        return value;
    }

    /**
     * Called when the player logs in to send packets
     */
    public static void syncPackets(OnDatapackSyncEvent event, ISimplePacket... packets) {
        JsonHelper.syncPackets(event, TinkerNetwork.getInstance(), packets);
    }

    /**
     * Creates a JSON object with the given key set to a resource location
     */
    public static JsonObject withLocation(String key, Identifier value) {
        JsonObject json = new JsonObject();
        json.addProperty(key, value.toString());
        return json;
    }

    /**
     * Creates a JSON object with the given type set, makes using {@link slimeknights.mantle.data.gson.GenericRegisteredSerializer} easier
     */
    public static JsonObject withType(Identifier type) {
        return withLocation("type", type);
    }

    /**
     * Reads the result from the given JSON
     *
     * @param element element to parse
     * @param name    Tag name
     * @return Item stack result
     * @throws JsonSyntaxException If the syntax is invalid
     */
    public static ItemStack convertToItemStack(JsonElement element, String name) {
        if (element.isJsonPrimitive()) {
            return new ItemStack(net.minecraft.util.JsonHelper.asItem(element, name));
        } else {
            return CraftingHelper.getItemStack(net.minecraft.util.JsonHelper.asObject(element, name), true);
        }
    }

    /**
     * Reads the result from the given JSON
     *
     * @param parent Parent JSON
     * @param name   Tag name
     * @return Item stack result
     * @throws JsonSyntaxException If the syntax is invalid
     */
    public static ItemStack getAsItemStack(JsonObject parent, String name) {
        return convertToItemStack(JsonHelper.getElement(parent, name), name);
    }

    /**
     * Serializes the given result to JSON
     *
     * @param result Result
     * @return JSON element
     */
    public static JsonElement serializeItemStack(ItemStack result) {
        // if the item has NBT, write both, else write just the name
        String itemName = Registries.ITEM.getKey(result.getItem()).toString();
        if (result.hasNbt()) {
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty("item", itemName);
            int count = result.getCount();
            if (count > 1) {
                jsonResult.addProperty("count", count);
            }
            jsonResult.addProperty("nbt", Objects.requireNonNull(result.getNbt()).toString());
            return jsonResult;
        } else {
            return new JsonPrimitive(itemName);
        }
    }

    /**
     * Parses a color as a string
     *
     * @param color Color to parse
     * @return Parsed string
     * @deprecated use {@link ColorLoadable#parseString(String, String)}
     */
    @Deprecated(forRemoval = true)
    public static int parseColor(@Nullable String color) {
        if (color == null || color.isEmpty()) {
            return -1;
        }
        return ColorLoadable.NO_ALPHA.parseString(color, "[unknown]");
    }

    /**
     * Writes the color as a 6 character string
     */
    public static String colorToString(int color) {
        return ColorLoadable.NO_ALPHA.getString(color);
    }
}