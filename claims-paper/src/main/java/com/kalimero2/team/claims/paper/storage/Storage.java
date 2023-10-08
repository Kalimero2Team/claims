package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.BlockInteractable;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Storage {

    private final PaperClaims plugin;
    private Connection connection;


    public Storage(PaperClaims plugin, File dataBase) {
        this.plugin = plugin;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataBase.getPath());
            createTablesIfNotExists();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getSLF4JLogger().error("Error while creating database connection", e);
        }
    }


    private ResultSet executeQuery(@Language(value = "SQL") String query, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement.executeQuery();
    }

    private void executeUpdate(@Language(value = "SQL") String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        statement.executeUpdate();
    }


    private void createTablesIfNotExists() throws SQLException {

        //
        // Group related tables
        //

        // GROUP
        // ID, NAME, MAX_CHUNKS, IS_PLAYER

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUPS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME VARCHAR(64) NULL," +
                "MAX_CLAIMS INTEGER NOT NULL," +
                "IS_PLAYER BOOLEAN NOT NULL" +
                ");");


        // GROUP_MEMBERS
        // GROUP_ID, PLAYER(UUID), PERMISSION_LEVEL

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUP_MEMBERS(" +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUPS(ID)," +
                "PLAYER VARCHAR(36) NOT NULL," +
                "PERMISSION_LEVEL TINYINT NOT NULL" +
                ");");


        //
        //  Claim related Tables
        //

        // CLAIM
        // ID, OWNER (GROUP), CHUNK_X, CHUNK_Z, WORLD(UUID), CLAIMED_SINCE, LAST_INTERACTION, LAST_ONLINE, EXPIRE_TIME

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIMS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "OWNER INTEGER NOT NULL REFERENCES GROUPS(ID)," +
                "CHUNK_X INTEGER NOT NULL," +
                "CHUNK_Z INTEGER NOT NULL," +
                "WORLD VARCHAR(36) NOT NULL," +
                "CLAIMED_SINCE DATETIME NOT NULL, " +
                "LAST_INTERACTION DATETIME NOT NULL," +
                "LAST_ONLINE DATETIME NOT NULL," +
                "EXPIRE_TIME SMALLINT NOT NULL" +
                ");");


        // CLAIM_MEMBERS
        // ID, CLAIM_ID, GROUP_ID

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_MEMBERS(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUPS(ID)" +
                ");");


        // CLAIM_FLAGS
        // CLAIM_ID, FLAG_IDENTIFIER, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_FLAGS(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "FLAG_IDENTIFIER VARCHAR(256) NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");


        // BLOCK_INTERACTABLES
        // CLAIM_ID, BLOCK_IDENTIFIER, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS BLOCK_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "BLOCK_IDENTIFIER VARCHAR(64) NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");

        // ENTITY_INTERACTABLES
        // CLAIM_ID, ENTITY_IDENTIFIER, INTERACT, DAMAGE

        executeUpdate("CREATE TABLE IF NOT EXISTS ENTITY_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "ENTITY_IDENTIFIER VARCHAR(64) NOT NULL," +
                "INTERACT BOOLEAN NOT NULL," +
                "DAMAGE BOOLEAN NOT NULL" +
                ");");

    }

    public Claim getClaimData(@NotNull Chunk chunk) {
        try {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            UUID world = chunk.getWorld().getUID();

            ResultSet resultSet = executeQuery("SELECT * FROM CLAIMS WHERE CHUNK_X = ? AND CHUNK_Z = ? AND WORLD = ?", chunkX, chunkZ, world);

            if (resultSet.next()) {
                int claim_id = resultSet.getInt("ID");

                return new StoredClaim(
                        claim_id,
                        getGroup(resultSet.getInt("OWNER")),
                        chunk,
                        getMembers(claim_id),
                        getBlockInteractables(claim_id),
                        getEntityInteractables(claim_id),
                        getFlags(claim_id),
                        resultSet.getTimestamp("CLAIMED_SINCE").toLocalDateTime(),
                        resultSet.getTimestamp("LAST_INTERACTION").toLocalDateTime(),
                        resultSet.getTimestamp("LAST_ONLINE").toLocalDateTime()
                );
            }
            resultSet.close();

            return null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    private @Nullable HashMap<Flag, Boolean> getFlags(int claimId) {
        try {
            HashMap<Flag, Boolean> flags = new HashMap<>();
            ResultSet resultSet = executeQuery("SELECT * FROM CLAIM_FLAGS WHERE CLAIM_ID = ?", claimId);
            while (resultSet.next()) {
                String flagIdentifier = resultSet.getString("FLAG_IDENTIFIER");
                boolean state = resultSet.getBoolean("STATE");
                Flag flag = ClaimsApi.getApi().getFlag(NamespacedKey.fromString(flagIdentifier));
                flags.put(flag, state);
            }
            resultSet.close();

            return flags;
        } catch (SQLException ignored) {
            return null;
        }
    }

    private @Nullable List<EntityInteractable> getEntityInteractables(int id) {
        try {
            List<EntityInteractable> interactables = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM ENTITY_INTERACTABLES WHERE CLAIM_ID = ?", id);
            while (resultSet.next()) {
                String entityIdentifier = resultSet.getString("ENTITY_IDENTIFIER");
                boolean interact = resultSet.getBoolean("INTERACT");
                boolean damage = resultSet.getBoolean("DAMAGE");
                interactables.add(new StoredEntityInteractable(EntityType.valueOf(entityIdentifier), interact, damage));
            }
            resultSet.close();

            return interactables;
        } catch (SQLException ignored) {
            return null;
        }
    }

    private @Nullable List<BlockInteractable> getBlockInteractables(int id) {
        try {
            List<BlockInteractable> interactables = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM BLOCK_INTERACTABLES WHERE CLAIM_ID = ?", id);
            while (resultSet.next()) {
                String blockIdentifier = resultSet.getString("BLOCK_IDENTIFIER");
                boolean state = resultSet.getBoolean("STATE");
                interactables.add(new StoredBlockInteractable(Material.valueOf(blockIdentifier), state));
            }
            resultSet.close();

            return interactables;
        } catch (SQLException ignored) {
            return null;
        }
    }


    public StoredGroup getGroup(int id) {
        try {
            ResultSet resultSet = executeQuery("SELECT * FROM GROUPS WHERE ID = ?", id);
            if (resultSet.next()) {
                int maxClaims = resultSet.getInt("MAX_CLAIMS");
                String name = resultSet.getString("NAME");
                boolean isPlayer = resultSet.getBoolean("IS_PLAYER");
                List<GroupMember> members = getGroupMembers(id);
                resultSet.close();

                return new StoredGroup(id, name, maxClaims, isPlayer, members);
            }
            resultSet.close();

            return null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<GroupMember> getGroupMembers(int group_id) {
        try {
            List<GroupMember> groupMembers = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM GROUP_MEMBERS WHERE GROUP_ID = ?", group_id);
            while (resultSet.next()) {
                String player = resultSet.getString("PLAYER");
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(player));
                PermissionLevel level = PermissionLevel.fromLevel(resultSet.getInt("PERMISSION_LEVEL"));
                groupMembers.add(new StoredGroupMember(offlinePlayer, level));
            }
            resultSet.close();

            return groupMembers;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public Group getPlayerGroup(@NotNull OfflinePlayer player) {
        try {
            ResultSet resultSet = executeQuery("SELECT G.ID, G.NAME, G.MAX_CLAIMS, G.IS_PLAYER FROM GROUPS G INNER JOIN GROUP_MEMBERS GM ON G.ID = GM.GROUP_ID WHERE G.IS_PLAYER = TRUE AND GM.PLAYER = ?", player.getUniqueId().toString());

            if (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int maxClaims = resultSet.getInt("MAX_CLAIMS");
                boolean isPlayer = resultSet.getBoolean("IS_PLAYER");
                List<GroupMember> members = getGroupMembers(id);
                resultSet.close();
                return new StoredGroup(id, name, maxClaims, isPlayer, members);
            }else {
                return null;
            }
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<Group> getGroups(OfflinePlayer player) {
        try {
            List<Group> groups = new ArrayList<>();

            ResultSet resultSet = executeQuery("SELECT * FROM GROUPS WHERE IS_PLAYER = TRUE");
            return null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<Group> getMembers(int claim_id) {
        try {
            List<Group> members = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM CLAIM_MEMBERS WHERE CLAIM_ID = ?", claim_id);
            while (resultSet.next()) {
                int group_id = resultSet.getInt("GROUP_ID");
                members.add(getGroup(group_id));
            }
            resultSet.close();

            return members;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public boolean createGroup(String name, int maxClaims, boolean isPlayer) {
        try {
            executeUpdate("INSERT INTO GROUPS (NAME, MAX_CLAIMS, IS_PLAYER) VALUES (?, ?, ?)", name, maxClaims, isPlayer);
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean deleteGroup(Group group) {
        try {
            executeUpdate("DELETE FROM GROUPS WHERE ID = ?", group.getId());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean setMaxClaims(Group group, int max) {
        try {
            executeUpdate("UPDATE GROUPS SET MAX_CLAIMS = ? WHERE ID = ?", max, group.getId());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean addGroupMember(Group group, OfflinePlayer player, PermissionLevel level) {
        try {
            executeUpdate("INSERT INTO GROUP_MEMBERS (GROUP_ID, PLAYER, PERMISSION_LEVEL) VALUES (?, ?, ?)", group.getId(), player.getUniqueId().toString(), level.getLevel());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean removeGroupMember(Group group, GroupMember member) {
        try {
            executeUpdate("DELETE FROM GROUP_MEMBERS WHERE GROUP_ID = ? AND PLAYER = ?", group.getId(), member.getPlayer().getUniqueId().toString());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean setPermissionLevel(Group group, GroupMember member, PermissionLevel level) {
        try {
            executeUpdate("UPDATE GROUP_MEMBERS SET PERMISSION_LEVEL = ? WHERE GROUP_ID = ? AND PLAYER = ?", level.getLevel(), group.getId(), member.getPlayer().getUniqueId().toString());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean unclaimClaim(Claim claim) {
        try {

            executeUpdate("DELETE FROM CLAIMS WHERE ID = ?", claim.getId());
            executeUpdate("DELETE FROM ENTITY_INTERACTABLES WHERE CLAIM_ID = ?", claim.getId());
            executeUpdate("DELETE FROM BLOCK_INTERACTABLES WHERE CLAIM_ID = ?", claim.getId());
            executeUpdate("DELETE FROM CLAIM_FLAGS WHERE CLAIM_ID = ?", claim.getId());
            executeUpdate("DELETE FROM CLAIM_MEMBERS WHERE CLAIM_ID = ?", claim.getId());

            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean claimChunk(Chunk chunk, Group group) {
        try {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            UUID world = chunk.getWorld().getUID();

            executeUpdate("INSERT INTO CLAIMS (OWNER, CHUNK_X, CHUNK_Z, WORLD, CLAIMED_SINCE, LAST_INTERACTION, LAST_ONLINE, EXPIRE_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    group.getId(),
                    chunkX,
                    chunkZ,
                    world.toString(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    0
            );

            executeUpdate("INSERT INTO CLAIM_MEMBERS (CLAIM_ID, GROUP_ID) VALUES (?, ?)", getClaimData(chunk).getId(), group.getId());

            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }
}