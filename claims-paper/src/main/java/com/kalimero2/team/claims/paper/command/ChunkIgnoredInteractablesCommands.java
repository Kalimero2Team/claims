package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChunkIgnoredInteractablesCommands extends CommandHandler {
    public ChunkIgnoredInteractablesCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        CommandArgument<CommandSender, Material> interactableMaterial = MaterialArgument.<CommandSender>builder("material")
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    for (Material value : Material.values()) {
                        if (value.isInteractable()) {
                            suggestions.add(value.name());
                        }
                    }
                    return suggestions;
                })
                .build();

        CommandArgument<CommandSender, String> entityArgument = StringArgument.<CommandSender>builder("entity")
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    for (EntityType value : EntityType.values()) {
                        if (value.isAlive()) {
                            suggestions.add(value.getKey().toString());
                        }
                    }
                    return suggestions;
                })
                .build();

        CommandArgument<CommandSender, String> entityStateArgument = StringArgument.<CommandSender>builder("state")
                .withSuggestionsProvider((context, s) -> List.of("both", "damage", "interact", "none"))
                .build();

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("block")
                        .literal("allow")
                        .argument(interactableMaterial.copy())
                        .handler(this::interactablesAddMaterial)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("block")
                        .literal("deny")
                        .argument(interactableMaterial.copy())
                        .handler(this::interactablesRemoveMaterial)
        );

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("entity")
                        .literal("set")
                        .argument(entityArgument.copy())
                        .argument(entityStateArgument.copy())
                        .handler(this::interactablesSetEntity)
        );


        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("list")
                        .handler(this::interactablesList)
        );
    }

    private void interactablesSetEntity(CommandContext<CommandSender> commandSenderCommandContext) {

    }

    // TODO: Add EntityInteractables to list command
    private void interactablesList(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                List<BlockInteractable> materials = claim.getBlockInteractables();
                if (materials.isEmpty()) {
                    plugin.getMessageUtil().sendMessage(player, "chunk.no_ignored_interactables");
                } else {
                    List<Material> blockMaterials = materials.stream().map(BlockInteractable::getBlockMaterial).toList();
                    plugin.getMessageUtil().sendMessage(player, "chunk.ignored_interactables", Placeholder.unparsed("materials", String.join(", ", blockMaterials.stream().map(Material::name).toArray(String[]::new))));
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }
        }
    }


    private void interactablesAddMaterial(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                    Material material = context.get("material");
                    if (!material.isInteractable()) {
                        plugin.getMessageUtil().sendMessage(player, "chunk.material_should_be_interactable_block");
                        return;
                    }
                    if (claim.getBlockInteractables().stream().map(BlockInteractable::getBlockMaterial).toList().contains(material)) {
                        plugin.getMessageUtil().sendMessage(player, "chunk.material_already_added", Placeholder.unparsed("material", material.name()));
                        return;
                    }
                    // TODO: Add addBlockInteractable method to api
                    // api.addBlockInteractable(claim, material);
                    plugin.getMessageUtil().sendMessage(player, "chunk.material_added", Placeholder.unparsed("material", material.name()));
                } else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }

        }
    }

    private void interactablesRemoveMaterial(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (groupMember != null && groupMember.getPermissionLevel().isHigherOrEqual(PermissionLevel.ADMIN)) {
                    Material material = context.get("material");
                    if (claim.getBlockInteractables().stream().map(BlockInteractable::getBlockMaterial).toList().contains(material)) {
                        // TODO: Add removeBlockInteractable method to api
                        // api.removeBlockInteractable(claim, material);

                        plugin.getMessageUtil().sendMessage(player, "chunk.material_removed", Placeholder.unparsed("material", material.name()));
                    } else {
                        plugin.getMessageUtil().sendMessage(player, "chunk.material_not_added", Placeholder.unparsed("material", material.name()));
                    }
                }else {
                    plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_no_permission");
                }
            } else {
                plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }
        }
    }
}
