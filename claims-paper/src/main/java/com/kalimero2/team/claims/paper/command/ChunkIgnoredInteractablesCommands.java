package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.kalimero2.team.claims.paper.claim.ClaimManager.forcedPlayers;

public class ChunkIgnoredInteractablesCommands extends CommandHandler {
    public ChunkIgnoredInteractablesCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("allow")
                        .argument(MaterialArgument.of("material"))
                        .handler(this::interactablesAdd)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("deny")
                        .argument(MaterialArgument.of("material"))
                        .handler(this::interactablesRemove)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("interactables")
                        .literal("list")
                        .handler(this::interactablesList)
        );
    }

    private void interactablesList(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed()) {
                List<Material> materials = chunk.getIgnoredInteractableBukkitMaterials();
                if (materials.isEmpty()) {
                    PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.no_ignored_interactables");
                } else {
                    PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.ignored_interactables", Placeholder.unparsed("materials", String.join(", ", materials.stream().map(Material::name).toArray(String[]::new))));
                }
            } else {
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }
        }
    }


    private void interactablesAdd(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed()) {
                if ((chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId())) || forcedPlayers.contains(player.getUniqueId()) || (!chunk.hasOwner() && player.hasPermission("claims.admin.teamclaim"))) {
                    Material material = context.get("material");
                    if (!material.isInteractable()) {
                        PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.material_should_be_interactable_block");
                        return;
                    }
                    if (chunk.getIgnoredInteractableBukkitMaterials().contains(material)) {
                        PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.material_already_added", Placeholder.unparsed("material", material.name()));
                        return;
                    }
                    chunk.addIgnoredInteractable(material);
                    PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.material_added", Placeholder.unparsed("material", material.name()));
                }
            } else {
                PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.generic_fail_chunk_not_claimed");
            }

        }
    }

    private void interactablesRemove(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if (chunk.isClaimed()) {
                if ((chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId())) || forcedPlayers.contains(player.getUniqueId()) || (!chunk.hasOwner() && player.hasPermission("claims.admin.teamclaim"))) {
                    Material material = context.get("material");
                    if (chunk.getIgnoredInteractableBukkitMaterials().contains(material)) {
                        chunk.removeIgnoredInteractable(material);
                        PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.material_removed", Placeholder.unparsed("material", material.name()));
                    } else {
                        PaperClaims.plugin.getMessageUtil().sendMessage(player, "chunk.material_not_added", Placeholder.unparsed("material", material.name()));
                    }
                }
            }
        }
    }
}
