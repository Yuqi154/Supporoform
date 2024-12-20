package slimeknights.tconstruct.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.shared.TinkerCommons;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigEnabledCondition implements ConditionJsonProvider, LootCondition {
    public static final Identifier ID = TConstruct.getResource("config");
    public static final ConfigSerializer SERIALIZER = new ConfigSerializer();
    /* Map of config names to condition cache */
    private static final Map<String, ConfigEnabledCondition> PROPS = new HashMap<>();

    private final String configName;
    private final BooleanSupplier supplier;

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return supplier.getAsBoolean();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return supplier.getAsBoolean();
    }

    @Override
    public LootConditionType getType() {
        return TinkerCommons.lootConfig.get();
    }

    private static class ConfigSerializer implements JsonSerializer<ConfigEnabledCondition>, IConditionSerializer<ConfigEnabledCondition> {
        @Override
        public Identifier getID() {
            return ID;
        }

        @Override
        public void write(JsonObject json, ConfigEnabledCondition value) {
            json.addProperty("prop", value.configName);
        }

        @Override
        public ConfigEnabledCondition read(JsonObject json) {
            String prop = JsonHelper.getString(json, "prop");
            ConfigEnabledCondition config = PROPS.get(prop.toLowerCase(Locale.ROOT));
            if (config == null) {
                throw new JsonSyntaxException("Invalid property name '" + prop + "'");
            }
            return config;
        }

        @Override
        public void toJson(JsonObject json, ConfigEnabledCondition condition, JsonSerializationContext context) {
            write(json, condition);
        }

        @Override
        public ConfigEnabledCondition fromJson(JsonObject json, JsonDeserializationContext context) {
            return read(json);
        }
    }

    /**
     * Adds a condition
     *
     * @param prop     Property name
     * @param supplier Boolean supplier
     * @return Added condition
     */
    private static ConfigEnabledCondition add(String prop, BooleanSupplier supplier) {
        ConfigEnabledCondition conf = new ConfigEnabledCondition(prop, supplier);
        PROPS.put(prop.toLowerCase(Locale.ROOT), conf);
        return conf;
    }

    /**
     * Adds a condition
     *
     * @param prop     Property name
     * @param supplier Config value
     * @return Added condition
     */
    private static ConfigEnabledCondition add(String prop, BooleanValue supplier) {
        return add(prop, supplier::get);
    }

    @Override
    public String toString() {
        return "config_setting_enabled(\"" + this.configName + "\")";
    }

    /* Properties */
    public static final ConfigEnabledCondition SPAWN_WITH_BOOK = add("spawn_with_book", Config.COMMON.shouldSpawnWithTinkersBook);
    public static final ConfigEnabledCondition GRAVEL_TO_FLINT = add("gravel_to_flint", Config.COMMON.addGravelToFlintRecipe);
    public static final ConfigEnabledCondition CHEAPER_NETHERITE_ALLOY = add("cheaper_netherite_alloy", Config.COMMON.cheaperNetheriteAlloy);
    public static final ConfigEnabledCondition WITHER_BONE_DROP = add("wither_bone_drop", Config.COMMON.witherBoneDrop);
    public static final ConfigEnabledCondition WITHER_BONE_CONVERSION = add("wither_bone_conversion", Config.COMMON.witherBoneConversion);
    public static final ConfigEnabledCondition SLIME_RECIPE_FIX = add("slime_recipe_fix", Config.COMMON.glassRecipeFix);
    public static final ConfigEnabledCondition GLASS_RECIPE_FIX = add("glass_recipe_fix", Config.COMMON.glassRecipeFix);
    public static final ConfigEnabledCondition FORCE_INTEGRATION_MATERIALS = add("force_integration_materials", Config.COMMON.forceIntegrationMaterials);
}
