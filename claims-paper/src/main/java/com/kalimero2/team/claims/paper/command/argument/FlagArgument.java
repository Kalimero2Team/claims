package com.kalimero2.team.claims.paper.command.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class FlagArgument<C> extends CommandArgument<C, Flag> {

    private static final ClaimsApi api = ClaimsApi.getApi();

    protected FlagArgument(
            final boolean required,
            final @NotNull String name,
            final @NotNull String defaultValue,
            final @Nullable BiFunction<@NotNull CommandContext<C>, @NotNull String,
                    @NotNull List<@NotNull String>> suggestionsProvider,
            final @NotNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new FlagArgument.FlagParser<>(), defaultValue, Flag.class, suggestionsProvider, defaultDescription);
    }


    public static <C> FlagArgument.@NotNull Builder<C> builder(final @NotNull String name) {
        return new FlagArgument.Builder<>(name);
    }


    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NotNull CommandArgument<C, Flag> of(final @NotNull String name) {
        return FlagArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NotNull CommandArgument<C, Flag> optional(final @NotNull String name) {
        return FlagArgument.<C>builder(name).asOptional().build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Flag> {

        private Builder(final @NotNull String name) {
            super(Flag.class, name);
        }

        /**
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Override
        public @NotNull FlagArgument<C> build() {
            return new FlagArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    public static final class FlagParser<C> implements ArgumentParser<C, Flag> {

        @Override
        public @NotNull ArgumentParseResult<Flag> parse(final @NotNull CommandContext<C> commandContext, final @NotNull Queue<@NotNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(FlagArgument.FlagParser.class, commandContext));
            }

            Flag flag = null;
            try {
                flag = api.getFlag(NamespacedKey.fromString(input));
            } catch (NumberFormatException ignored) {
            }

            if (flag == null) {
                flag = api.getFlag(new NamespacedKey("claims", input));
            }

            if (flag == null) {
                return ArgumentParseResult.failure(new FlagParseException(input, commandContext));
            }

            inputQueue.remove();

            return ArgumentParseResult.success(flag);
        }

        @Override
        public @NotNull List<@NotNull String> suggestions(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull String input
        ) {
            List<String> output = new ArrayList<>();

            for (Flag flag : api.getFlags()) {
                if(!(flag.getPermission() != null && !commandContext.hasPermission(flag.getPermission()))){
                    output.add(flag.getKeyString());
                }
            }

            return output;
        }
    }

    /**
     * StoredWaystone parse exception
     */
    public static final class FlagParseException extends ParserException {

        private final String input;

        /**
         * Construct a new StoredWaystone parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public FlagParseException(
                final @NotNull String input,
                final @NotNull CommandContext<?> context
        ) {
            super(
                    FlagArgument.FlagParser.class,
                    context,
                    Caption.of("argument.parse.failure.flag"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;

            if (!context.isSuggestions()) {
                ((CommandSender) context.getSender()).sendMessage(Component.text("No Flag with ID or key " + input + " exists.").color(NamedTextColor.RED));
            }
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NotNull String getInput() {
            return this.input;
        }
    }

}
