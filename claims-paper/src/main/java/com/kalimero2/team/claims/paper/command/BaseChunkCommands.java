package com.kalimero2.team.claims.paper.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import com.kalimero2.team.claims.paper.claim.ClaimsChunk;
import com.kalimero2.team.claims.paper.util.ExtraPlayerData;
import com.kalimero2.team.claims.paper.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.kalimero2.team.claims.paper.claim.ClaimManager.forcedPlayers;

public class BaseChunkCommands extends CommandHandler{
    public BaseChunkCommands(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("chunk").handler(this::info)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("claim")
                        .handler(this::claim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("unclaim")
                        .handler(this::unClaim)
        );
        commandManager.command(
                commandManager.commandBuilder("chunk")
                        .literal("list")
                        .handler(this::listClaims)
        );
    }

    private void listClaims(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(player);
            player.sendMessage("Claims: "+extraPlayerData.chunks.size());
            extraPlayerData.chunks.forEach(serializableChunk -> {
                TagResolver.Single chunk_x = Placeholder.unparsed("chunk_x", String.valueOf(serializableChunk.x()));
                TagResolver.Single chunk_z = Placeholder.unparsed("chunk_z", String.valueOf(serializableChunk.z()));
                Chunk obj = serializableChunk.toBukkitChunk();
                Location location = obj.getBlock(0, 0, 0).getLocation();
                TagResolver.Single x = Placeholder.unparsed("x", String.valueOf(location.getBlockX()));
                TagResolver.Single z = Placeholder.unparsed("z", String.valueOf(location.getBlockZ()));
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.list", chunk_x, chunk_z,x,z);
            });
        }
    }

    private void unClaim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.isClaimed()){
                if(chunk.hasOwner() && chunk.getOwner().equals(player.getUniqueId()) || forcedPlayers.contains(player.getUniqueId())){
                    ClaimManager.unclaimChunk(chunk,player);
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_success");
                } else if (!chunk.hasOwner() && player.hasPermission("claims.admin.teamClaim")) {
                    chunk.setClaimed(false);
                    chunk.setProperties(new HashMap<>());
                    chunk.setTrusted(null);
                    chunk.clearIgnoredInteractables();

                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.team_unclaim_success");
                } else{
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_fail_not_owner");
                }
            }else{
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.unclaim_fail_not_claimed");
            }

        }
    }

    private void claim(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            if(chunk.isClaimed()){
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_already_claimed");
            } else if(!PaperClaims.plugin.getConfig().getStringList("claims.worlds").contains(player.getWorld().getWorldFolder().getName())) {
                PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_world_not_claimable");
            } else{
                if(ClaimManager.claimChunk(chunk, player)){
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_success");
                }else{
                    ExtraPlayerData extraPlayerData = ClaimManager.getExtraPlayerData(player);
                    TagResolver.Single count = Placeholder.unparsed("count", String.valueOf(extraPlayerData.chunks.size()));
                    TagResolver.Single max = Placeholder.unparsed("max_count", String.valueOf(extraPlayerData.maxclaims));
                    PaperClaims.plugin.getMessageUtil().sendMessage(player,"chunk.claim_fail_too_many_claims",count,max);
                }

            }
        }
    }


    private void info(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            ClaimsChunk chunk = ClaimsChunk.of(player.getChunk());
            Server server = PaperClaims.plugin.getServer();
            MessageUtil messageUtil = PaperClaims.plugin.getMessageUtil();

            messageUtil.sendMessage(player,"chunk.info", Placeholder.component("chunk_x", Component.text(chunk.getChunkX())), Placeholder.component("chunk_z", Component.text(chunk.getChunkZ())));

            if(chunk.isClaimed()){
                messageUtil.sendMessage(player, "chunk.claimed_true");
                if(chunk.hasOwner()){
                    String owner_name = server.getOfflinePlayer(chunk.getOwner()).getName();
                    if(owner_name == null){
                        owner_name = chunk.getOwner().toString();
                    }
                    messageUtil.sendMessage(player, "chunk.claim_owner", Placeholder.unparsed("player", owner_name));
                }else{
                    messageUtil.sendMessage(player, "chunk.team_chunk");
                }

                List<UUID> trusted = chunk.getTrustedList();
                if(trusted.size() >= 1){
                    messageUtil.sendMessage(player, "chunk.claim_trusted_start");
                    for(UUID trustedPlayer:trusted){
                        OfflinePlayer offlinePlayer = server.getOfflinePlayer(trustedPlayer);
                        messageUtil.sendMessage(player, "chunk.claim_trusted_player", Placeholder.unparsed("player", Objects.requireNonNullElse(offlinePlayer.getName(),"player")));
                    }
                }
            }else{
                messageUtil.sendMessage(player, "chunk.claimed_false");
            }

        }
    }
}
