package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChunkInteractablesCommands extends CommandHandler {
    public ChunkInteractablesCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        CommandArgument<CommandSender, Material> interactableMaterial = MaterialArgument.<CommandSender>builder("material")
                .withSuggestionsProvider((context, s) -> {
                    List<String> suggestions = new ArrayList<>();
                    for (Material value : Material.values()) {
                        if (value.isInteractable()) {
                            suggestions.add(value.name().toLowerCase());
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
                            suggestions.add(value.getKey().toString().replace("minecraft:", ""));
                        }
                    }
                    return suggestions;
                })
                .build();

        CommandArgument<CommandSender, String> blockStateArgument = StringArgument.<CommandSender>builder("state")
                .withSuggestionsProvider((context, s) -> List.of("allow", "deny"))
                .build();

        CommandArgument<CommandSender, String> entityStateArgument = StringArgument.<CommandSender>builder("state")
                .withSuggestionsProvider((context, s) -> List.of("both", "damage", "interact", "none"))
                .build();

        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("block")
                        .literal("set")
                        .argument(interactableMaterial.copy())
                        .argument(blockStateArgument.copy())
                        .handler(this::interactablesSetMaterial)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("block")
                        .literal("remove")
                        .argument(MaterialArgument.<CommandSender>builder("material")
                                .withSuggestionsProvider((context, s) -> {
                                    List<String> suggestions = new ArrayList<>();
                                    if (context.getSender() instanceof Player player) {
                                        Claim claim = api.getClaim(player.getChunk());
                                        if (claim != null) {
                                            for (MaterialInteractable blockInteractable : claim.getMaterialInteractables()) {
                                                suggestions.add(blockInteractable.getBlockMaterial().name().toLowerCase());
                                            }
                                        }
                                    }
                                    return suggestions;
                                })
                                .build()
                        )
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
                        .literal("entity")
                        .literal("remove")
                        .argument(StringArgument.<CommandSender>builder("entity")
                                .withSuggestionsProvider((context, s) -> {
                                    List<String> suggestions = new ArrayList<>();
                                    if (context.getSender() instanceof Player player) {
                                        Claim claim = api.getClaim(player.getChunk());
                                        if (claim != null) {
                                            for (EntityType value : EntityType.values()) {
                                                if (value.isAlive()) {
                                                    suggestions.add(value.getKey().toString().replace("minecraft:", ""));
                                                }
                                            }
                                        }
                                    }
                                    return suggestions;
                                }))
                        .handler(this::interactablesRemoveEntity)
        );


        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("list")
                        .handler(this::interactablesList)
        );
    }


    private void interactablesSetMaterial(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                    Material material = context.get("material");
                    if (!material.isInteractable()) {
                        messageUtil.sendMessage(player, "chunk.interactable.block.fail_not_interactable");
                        return;
                    }
                    if (claim.getMaterialInteractables().stream().map(MaterialInteractable::getBlockMaterial).toList().contains(material)) {
                        messageUtil.sendMessage(player, "chunk.interactable.block.fail_already_added",
                                Placeholder.unparsed("material", material.name().toLowerCase())
                        );
                        return;
                    }

                    boolean state = ((String) context.get("state")).equalsIgnoreCase("allow");

                    api.setBlockInteractable(claim, material, state);
                    if (state) {
                        messageUtil.sendMessage(player, "chunk.interactable.block.allow_material",
                                Placeholder.unparsed("material", material.name().toLowerCase())
                        );
                    } else {
                        messageUtil.sendMessage(player, "chunk.interactable.block.deny_material",
                                Placeholder.unparsed("material", material.name().toLowerCase())
                        );
                    }
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }

        }
    }

    private void interactablesRemoveMaterial(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                    Material material = context.get("material");
                    if (claim.getMaterialInteractables().stream().map(MaterialInteractable::getBlockMaterial).toList().contains(material)) {
                        api.removeBlockInteractable(claim, material);
                        messageUtil.sendMessage(player, "chunk.interactable.block.removed_material",
                                Placeholder.unparsed("material", material.name().toLowerCase())
                        );
                    } else {
                        messageUtil.sendMessage(player, "chunk.interactable.block.fail_not_added",
                                Placeholder.unparsed("material", material.name().toLowerCase())
                        );
                    }
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }


    private void interactablesSetEntity(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                    String entityName = context.get("entity");
                    EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                    String state = context.get("state");
                    boolean damage = false;
                    boolean interact = false;
                    if (state.equalsIgnoreCase("both")) {
                        damage = true;
                        interact = true;
                    } else if (state.equalsIgnoreCase("damage")) {
                        damage = true;
                    } else if (state.equalsIgnoreCase("interact")) {
                        interact = true;
                    }

                    api.setEntityInteractable(claim, entityType, damage, interact);

                    TagResolver.Single entityPlaceHolder = Placeholder.unparsed("entity", entityType.getKey().toString().replace("minecraft:", ""));
                    if (damage && interact) {
                        messageUtil.sendMessage(player, "chunk.interactable.entity.allow_both", entityPlaceHolder);
                    } else if (damage) {
                        messageUtil.sendMessage(player, "chunk.interactable.entity.allow_damage", entityPlaceHolder);
                    } else if (interact) {
                        messageUtil.sendMessage(player, "chunk.interactable.entity.allow_interact", entityPlaceHolder);
                    } else {
                        messageUtil.sendMessage(player, "chunk.interactable.entity.none", entityPlaceHolder);
                    }
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    private void interactablesRemoveEntity(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                Group group = claim.getOwner();
                GroupMember groupMember = api.getGroupMember(group, player);
                if (permissionCheck(player, groupMember, PermissionLevel.ADMIN)) {
                    String entityName = context.get("entity");
                    EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                    api.removeEntityInteractable(claim, entityType);
                    messageUtil.sendMessage(player, "chunk.interactable.entity.removed",
                            Placeholder.unparsed("entity", entityType.getKey().toString().replace("minecraft:", ""))
                    );
                } else {
                    messageUtil.sendMessage(player, "chunk.generic.fail_no_permission");
                }
            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    private void interactablesList(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            Claim claim = api.getClaim(player.getChunk());
            if (claim != null) {
                List<MaterialInteractable> materials = claim.getMaterialInteractables();
                if (materials.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.interactable.list.material_none_ignored");
                } else {
                    messageUtil.sendMessage(player, "chunk.interactable.list.material_header");
                    for (MaterialInteractable material : materials) {
                        messageUtil.sendMessage(player, "chunk.interactable.list.material_entry",
                                Placeholder.component("material", getMaterialComponent(material)),
                                Placeholder.parsed("state", material.canInteract() ? "<allowed>" : "<denied>")
                        );
                    }
                }
                List<EntityInteractable> entities = claim.getEntityInteractables();
                if (entities.isEmpty()) {
                    messageUtil.sendMessage(player, "chunk.interactable.list.entity_none_ignored");
                } else {
                    messageUtil.sendMessage(player, "chunk.interactable.list.entity_header");
                    for (EntityInteractable entity : entities) {
                        Component entityComponent = Component.translatable(entity.getEntityType().translationKey(), entity.getEntityType().getKey().toString().replace("minecraft:", ""));
                        entityComponent = entityComponent.hoverEvent(HoverEvent.showEntity(entity.getEntityType().key(), UUID.randomUUID()));
                        messageUtil.sendMessage(player, "chunk.interactable.list.entity_entry",
                                Placeholder.component("entity", entityComponent),
                                Placeholder.parsed("damage", entity.canDamage() ? "<allowed>" : "<denied>"),
                                Placeholder.parsed("interact", entity.canInteract() ? "<allowed>" : "<denied>")
                        );
                    }
                }


            } else {
                messageUtil.sendMessage(player, "chunk.generic.fail_chunk_not_claimed");
            }
        }
    }

    @NotNull
    private static Component getMaterialComponent(MaterialInteractable material) {
        Material blockMaterial = material.getBlockMaterial();
        String blockTranslationKey = blockMaterial.getBlockTranslationKey();
        if(blockTranslationKey == null){
            blockTranslationKey = blockMaterial.name().toLowerCase();
        }
        Component materialComponent = Component.translatable(blockTranslationKey, blockMaterial.name().toLowerCase());
        materialComponent = materialComponent.hoverEvent(HoverEvent.showItem(blockMaterial.key(),1));
        return materialComponent;
    }


}
