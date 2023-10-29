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
import com.kalimero2.team.claims.api.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class GroupArgument<C> extends CommandArgument<C, Group> {

    private static final ClaimsApi api = ClaimsApi.getApi();

    protected GroupArgument(
            final boolean required,
            final @NotNull String name,
            final @NotNull String defaultValue,
            final @Nullable BiFunction<@NotNull CommandContext<C>, @NotNull String,
                    @NotNull List<@NotNull String>> suggestionsProvider,
            final @NotNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new GroupArgument.GroupParser<>(), defaultValue, Group.class, suggestionsProvider, defaultDescription);
    }


    public static <C> GroupArgument.@NotNull Builder<C> builder(final @NotNull String name) {
        return new GroupArgument.Builder<>(name);
    }


    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NotNull CommandArgument<C, Group> of(final @NotNull String name) {
        return GroupArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NotNull CommandArgument<C, Group> optional(final @NotNull String name) {
        return GroupArgument.<C>builder(name).asOptional().build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Group> {

        private Builder(final @NotNull String name) {
            super(Group.class, name);
        }

        /**
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Override
        public @NotNull GroupArgument<C> build() {
            return new GroupArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    public static final class GroupParser<C> implements ArgumentParser<C, Group> {

        @Override
        public @NotNull ArgumentParseResult<Group> parse(final @NotNull CommandContext<C> commandContext, final @NotNull Queue<@NotNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(GroupArgument.GroupParser.class, commandContext));
            }

            Group group = null;
            try {
                group = api.getGroup(Integer.parseInt(input));
            } catch (NumberFormatException ignored) {
            }

            if (group == null) {
                group = api.getGroups().stream().filter(g -> (g.getName().equals(input)) && !g.isPlayer()).findFirst().orElse(null);
            }

            if (group == null) {
                return ArgumentParseResult.failure(new GroupParseException(input, commandContext));
            }

            inputQueue.remove();

            return ArgumentParseResult.success(group);
        }

        @Override
        public @NotNull List<@NotNull String> suggestions(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull String input
        ) {
            List<String> output = new ArrayList<>();

            for (Group group : api.getGroups()) {
                if (!group.isPlayer()) {
                    output.add(group.getName());
                }
            }

            return output;
        }
    }

    /**
     * Group parse exception
     */
    public static final class GroupParseException extends ParserException {

        private final String input;

        /**
         * Construct a new Group parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public GroupParseException(
                final @NotNull String input,
                final @NotNull CommandContext<?> context
        ) {
            super(
                    GroupArgument.GroupParser.class,
                    context,
                    Caption.of("argument.parse.failure.group"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;

            if (!context.isSuggestions()) {
                ((CommandSender) context.getSender()).sendMessage(Component.text("No Group with ID or name " + input + " exists.").color(NamedTextColor.RED));
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
