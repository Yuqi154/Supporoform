package slimeknights.tconstruct.shared.command.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.mutable.MutableInt;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.command.HeldModifiableItemIterator;
import slimeknights.tconstruct.shared.command.argument.ModifierArgument;

import java.util.List;

/**
 * Command to apply a modifier to a tool without using slots
 */
public class ModifiersCommand {
    private static final String ADD_SUCCESS = TConstruct.makeTranslationKey("command", "modifiers.success.add.single");
    private static final String ADD_SUCCESS_MULTIPLE = TConstruct.makeTranslationKey("command", "modifiers.success.add.multiple");
    private static final String REMOVE_SUCCESS = TConstruct.makeTranslationKey("command", "modifiers.success.remove.single");
    private static final String REMOVE_SUCCESS_MULTIPLE = TConstruct.makeTranslationKey("command", "modifiers.success.remove.multiple");
    private static final DynamicCommandExceptionType MODIFIER_ERROR = new DynamicCommandExceptionType(error -> (Text) error);
    private static final Dynamic2CommandExceptionType CANNOT_REMOVE = new Dynamic2CommandExceptionType((name, entity) -> TConstruct.makeTranslation("command", "modifiers.failure.too_few_levels", name, entity));

    /**
     * Registers this sub command with the root command
     *
     * @param subCommand Command builder
     */
    public static void register(LiteralArgumentBuilder<ServerCommandSource> subCommand) {
        subCommand.requires(sender -> sender.hasPermissionLevel(MantleCommand.PERMISSION_GAME_COMMANDS))
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                        // modifiers <target> add <modifier> [<level>]
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("modifier", ModifierArgument.modifier())
                                        .executes(context -> add(context, 1))
                                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> add(context, IntegerArgumentType.getInteger(context, "level"))))))
                        // modifiers <target> remove <modifier> [<level>]
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("modifier", ModifierArgument.modifier())
                                        .executes(context -> remove(context, -1))
                                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                                                .executes(context -> remove(context, IntegerArgumentType.getInteger(context, "level")))))));
    }

    /**
     * Runs the command
     */
    private static int add(CommandContext<ServerCommandSource> context, int level) throws CommandSyntaxException {
        Modifier modifier = ModifierArgument.getModifier(context, "modifier");
        List<LivingEntity> successes = HeldModifiableItemIterator.apply(context, (living, stack) -> {
            // add modifier
            ToolStack tool = ToolStack.from(stack).copy();
            // add the modifier
            tool.addModifier(modifier.getId(), level);
            // ensure no modifier problems after adding
            Text toolValidation = tool.tryValidate();
            if (toolValidation != null) {
                throw MODIFIER_ERROR.create(toolValidation);
            }

            // if successful, update held item
            living.setStackInHand(Hand.MAIN_HAND, tool.createStack(stack.getCount()));
            return true;
        });

        // success message
        ServerCommandSource source = context.getSource();
        int size = successes.size();
        if (size == 1) {
            source.sendFeedback(() -> Text.translatable(ADD_SUCCESS, modifier.getDisplayName(level), successes.get(0).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(ADD_SUCCESS_MULTIPLE, modifier.getDisplayName(level), size), true);
        }
        return size;
    }

    /**
     * Runs the command
     */
    private static int remove(CommandContext<ServerCommandSource> context, int level) throws CommandSyntaxException {
        Modifier modifier = ModifierArgument.getModifier(context, "modifier");
        MutableInt maxRemove = new MutableInt(1);
        List<LivingEntity> successes = HeldModifiableItemIterator.apply(context, (living, stack) -> {
            // add modifier
            ToolStack original = ToolStack.from(stack);

            // first, see if the modifier exists
            int currentLevel = original.getUpgrades().getLevel(modifier.getId());
            if (currentLevel == 0) {
                throw CANNOT_REMOVE.create(modifier.getDisplayName(level), living.getName());
            }
            int removeLevel = level == -1 ? currentLevel : level;
            if (removeLevel > maxRemove.intValue()) {
                maxRemove.setValue(removeLevel);
            }
            ToolStack tool = original.copy();

            // first remove hook, primarily for removing raw NBT which is highly discouraged using
            int newLevel = currentLevel - removeLevel;
            if (newLevel <= 0) {
                modifier.getHook(ModifierHooks.RAW_DATA).removeRawData(tool, modifier, tool.getRestrictedNBT());
            }

            // remove the actual modifier
            tool.removeModifier(modifier.getId(), removeLevel);

            // ensure the tool is still valid
            Text validated = tool.tryValidate();
            if (validated != null) {
                throw MODIFIER_ERROR.create(validated);
            }

            // ask modifiers if it's okay to remove them
            validated = ModifierRemovalHook.onRemoved(original, tool);
            if (validated != null) {
                throw MODIFIER_ERROR.create(validated);
            }

            // if successful, update held item
            living.setStackInHand(Hand.MAIN_HAND, tool.createStack(stack.getCount()));
            return true;
        });

        // success message
        ServerCommandSource source = context.getSource();
        int size = successes.size();
        if (size == 1) {
            source.sendFeedback(() -> Text.translatable(REMOVE_SUCCESS, modifier.getDisplayName(maxRemove.intValue()), successes.get(0).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(REMOVE_SUCCESS_MULTIPLE, modifier.getDisplayName(maxRemove.intValue()), size), true);
        }
        return size;
    }
}