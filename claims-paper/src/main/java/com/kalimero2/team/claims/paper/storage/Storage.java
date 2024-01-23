package com.kalimero2.team.claims.paper.storage;

import com.kalimero2.team.claims.api.Claim;
import com.kalimero2.team.claims.api.ClaimsApi;
import com.kalimero2.team.claims.api.flag.Flag;
import com.kalimero2.team.claims.api.group.Group;
import com.kalimero2.team.claims.api.group.GroupMember;
import com.kalimero2.team.claims.api.group.PermissionLevel;
import com.kalimero2.team.claims.api.interactable.EntityInteractable;
import com.kalimero2.team.claims.api.interactable.MaterialInteractable;
import com.kalimero2.team.claims.paper.PaperClaims;
import com.kalimero2.team.claims.paper.claim.ClaimManager;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Storage {

    private final PaperClaims plugin;
    private final ClaimManager claimManager;
    private Connection connection;


    public Storage(PaperClaims plugin, ClaimManager claimManager, File dataBase) {
        this.plugin = plugin;
        this.claimManager = claimManager;

        try {
            Class.forName("org.sqlite.JDBC");
            String connectionUri = "jdbc:sqlite:" + dataBase.getPath();

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUri, null);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
            ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
            poolableConnectionFactory.setPool(connectionPool);

            PoolingDataSource<PoolableConnection> dataSource =
                    new PoolingDataSource<>(connectionPool);

            connection = dataSource.getConnection();

            /*Statement statement = connection.createStatement();
            statement.execute("pragma journal_mode = WAL;");
            statement.execute("pragma synchronous = NORMAL;");
            statement.execute("pragma temp_store = MEMORY;");
            statement.execute("pragma mmap_size = 300000000;");
            statement.execute("pragma page_size = 32768");
            statement.close();*/

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

    private int executeUpdate(@Language(value = "SQL") String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
        return statement.executeUpdate();
    }


    private void createTablesIfNotExists() throws SQLException {
        if (!plugin.getConfig().contains("database-version", true)) {
            plugin.getConfig().set("database-version", 1);

            plugin.getLogger().info("Migrating Database to version 1");
            // make name of groups not unique
            executeUpdate("ALTER TABLE GROUPS RENAME TO GROUPS_OLD;");
            executeUpdate("CREATE TABLE IF NOT EXISTS GROUPS(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "NAME VARCHAR(64) NULL," +
                    "MAX_CLAIMS INTEGER NOT NULL," +
                    "IS_PLAYER BOOLEAN NOT NULL," +
                    "UNIQUE (NAME, IS_PLAYER)" +
                    ");");
            try {
                executeUpdate("INSERT INTO GROUPS (ID, NAME, MAX_CLAIMS, IS_PLAYER) SELECT ID, NAME, MAX_CLAIMS, IS_PLAYER FROM GROUPS_OLD;");
                executeUpdate("DROP TABLE GROUPS_OLD;");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error while migrating database to version 1");
                plugin.getLogger().warning("Error: " + e.getMessage());
                plugin.getServer().shutdown();
                return;
            }

            plugin.saveConfig();
            plugin.getLogger().info("Database migrated to version 1");
        }

        //
        // Group related tables
        //

        // GROUP
        // ID, NAME, MAX_CHUNKS, IS_PLAYER

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUPS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME VARCHAR(64) NULL," +
                "MAX_CLAIMS INTEGER NOT NULL," +
                "IS_PLAYER BOOLEAN NOT NULL," +
                "UNIQUE (NAME, IS_PLAYER)" +
                ");");


        // GROUP_MEMBERS
        // GROUP_ID, PLAYER(UUID), PERMISSION_LEVEL

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUP_MEMBERS(" +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUPS(ID)," +
                "PLAYER VARCHAR(36) NOT NULL," +
                "PERMISSION_LEVEL TINYINT NOT NULL," +
                "UNIQUE (GROUP_ID, PLAYER)" +
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
                "EXPIRE_TIME SMALLINT NOT NULL," +
                "UNIQUE (CHUNK_X, CHUNK_Z, WORLD)" +
                ");");


        // CLAIM_MEMBERS
        // ID, CLAIM_ID, GROUP_ID

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_MEMBERS(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUPS(ID)," +
                "UNIQUE (CLAIM_ID, GROUP_ID)" +
                ");");


        // CLAIM_FLAGS
        // CLAIM_ID, FLAG_IDENTIFIER, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_FLAGS(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "FLAG_IDENTIFIER VARCHAR(256) NOT NULL," +
                "STATE BOOLEAN NOT NULL," +
                "UNIQUE (CLAIM_ID, FLAG_IDENTIFIER)" +
                ");");


        // BLOCK_INTERACTABLES
        // CLAIM_ID, BLOCK_IDENTIFIER, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS BLOCK_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "BLOCK_IDENTIFIER VARCHAR(64) NOT NULL," +
                "STATE BOOLEAN NOT NULL," +
                "UNIQUE (CLAIM_ID, BLOCK_IDENTIFIER)" +
                ");");

        // ENTITY_INTERACTABLES
        // CLAIM_ID, ENTITY_IDENTIFIER, INTERACT, DAMAGE

        executeUpdate("CREATE TABLE IF NOT EXISTS ENTITY_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "ENTITY_IDENTIFIER VARCHAR(64) NOT NULL," +
                "INTERACT BOOLEAN NOT NULL," +
                "DAMAGE BOOLEAN NOT NULL," +
                "UNIQUE (CLAIM_ID, ENTITY_IDENTIFIER)" +
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
                        claimManager.getGroup(resultSet.getInt("OWNER")),
                        chunk,
                        getMembers(claim_id),
                        getBlockInteractables(claim_id),
                        getEntityInteractables(claim_id),
                        getFlags(claim_id),
                        resultSet.getLong("CLAIMED_SINCE"),
                        resultSet.getLong("LAST_INTERACTION"),
                        resultSet.getLong("LAST_ONLINE")
                );
            }
            resultSet.close();

            return null;
        } catch (SQLException exception) {
            plugin.getLogger().warning("Error while loading claim for chunk " + chunk.getX() + " " + chunk.getZ() + " with error: " + exception.getMessage());
            return null;
        }
    }

    public boolean saveClaim(Claim claim) {
        try {
            executeUpdate("UPDATE CLAIMS SET LAST_INTERACTION = ?, LAST_ONLINE = ? WHERE ID = ?", claim.getLastInteraction(), claim.getLastOnline(), claim.getId());
            StoredClaim storedClaim = StoredClaim.cast(claim);
            Group owner = claim.getOwner();
            HashMap<Flag, Boolean> flags = claim.getFlags();
            List<MaterialInteractable> blockInteractables = claim.getMaterialInteractables();
            List<EntityInteractable> entityInteractables = claim.getEntityInteractables();
            List<Group> members = claim.getMembers();

            if (!storedClaim.originalOwner.equals(owner)) {
                // Update Owner
                executeUpdate("UPDATE CLAIMS SET OWNER = ? WHERE ID = ?", owner.getId(), claim.getId());
            }
            if (!storedClaim.originalFlags.equals(flags)) {
                // Update Flags
                executeUpdate("DELETE FROM CLAIM_FLAGS WHERE CLAIM_ID = ?", claim.getId());
                for (Flag flag : flags.keySet()) {
                    executeUpdate("INSERT OR IGNORE INTO CLAIM_FLAGS (CLAIM_ID, FLAG_IDENTIFIER, STATE) VALUES (?, ?, ?)", claim.getId(), flag.getKey().toString(), flags.get(flag));
                }
            }
            if (!storedClaim.originalBlockInteractables.equals(blockInteractables)) {
                // Update Block Interactables
                executeUpdate("DELETE FROM BLOCK_INTERACTABLES WHERE CLAIM_ID = ?", claim.getId());
                for (MaterialInteractable interactable : blockInteractables) {
                    executeUpdate("INSERT OR IGNORE INTO BLOCK_INTERACTABLES (CLAIM_ID, BLOCK_IDENTIFIER, STATE) VALUES (?, ?, ?)", claim.getId(), interactable.getBlockMaterial().name(), interactable.canInteract());
                }
            }
            if (!storedClaim.originalEntityInteractables.equals(entityInteractables)) {
                // Update Entity Interactables
                executeUpdate("DELETE FROM ENTITY_INTERACTABLES WHERE CLAIM_ID = ?", claim.getId());
                for (EntityInteractable interactable : entityInteractables) {
                    executeUpdate("INSERT OR IGNORE INTO ENTITY_INTERACTABLES (CLAIM_ID, ENTITY_IDENTIFIER, INTERACT, DAMAGE) VALUES (?, ?, ?, ?)", claim.getId(), interactable.getEntityType().name(), interactable.canInteract(), interactable.canDamage());
                }
            }
            if (!storedClaim.originalMembers.equals(members)) {
                // Update Members
                executeUpdate("DELETE FROM CLAIM_MEMBERS WHERE CLAIM_ID = ?", claim.getId());
                for (Group member : members) {
                    executeUpdate("INSERT OR IGNORE INTO CLAIM_MEMBERS (CLAIM_ID, GROUP_ID) VALUES (?, ?)", claim.getId(), member.getId());
                }
            }
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe("Error while saving claim for chunk " + claim.getChunk().getX() + " " + claim.getChunk().getZ() + " with error: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean saveGroup(Group group) {
        try {
            StoredGroup storedGroup = StoredGroup.cast(group);

            List<GroupMember> members = storedGroup.getMembers();
            String name = storedGroup.getName();
            int maxClaims = storedGroup.getMaxClaims();

            if(!storedGroup.originalMembers.equals(members)){
                // Update Members
                executeUpdate("DELETE FROM GROUP_MEMBERS WHERE GROUP_ID = ?", group.getId());
                for (GroupMember member : members) {
                    executeUpdate("INSERT OR IGNORE INTO GROUP_MEMBERS (GROUP_ID, PLAYER, PERMISSION_LEVEL) VALUES (?, ?, ?)", group.getId(), member.getPlayer().getUniqueId().toString(), member.getPermissionLevel().getLevel());
                }
            }

            if(!storedGroup.originalName.equals(name)){
                // Update Name
                executeUpdate("UPDATE GROUPS SET NAME = ? WHERE ID = ?", name, group.getId());
            }

            if(storedGroup.originalMaxClaims != maxClaims){
                // Update Max Claims
                executeUpdate("UPDATE GROUPS SET MAX_CLAIMS = ? WHERE ID = ?", maxClaims, group.getId());
            }

            return true;
        }catch (SQLException exception) {
            plugin.getLogger().severe("Error while saving group " + group.getName() + " with error: " + exception.getMessage());
            return false;
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
                if (flag == null) {
                    plugin.getLogger().severe("Flag " + flagIdentifier + " not found for claim " + claimId);
                } else {
                    flags.put(flag, state);
                }
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

    private @Nullable List<MaterialInteractable> getBlockInteractables(int id) {
        try {
            List<MaterialInteractable> interactables = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM BLOCK_INTERACTABLES WHERE CLAIM_ID = ?", id);
            while (resultSet.next()) {
                String blockIdentifier = resultSet.getString("BLOCK_IDENTIFIER");
                boolean state = resultSet.getBoolean("STATE");
                interactables.add(new StoredMaterialInteractable(Material.valueOf(blockIdentifier), state));
            }
            resultSet.close();

            return interactables;
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
            } else {
                return null;
            }
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<Group> getGroups(OfflinePlayer player) {
        try {
            List<Group> groups = new ArrayList<>();

            ResultSet resultSet = executeQuery("SELECT G.ID, G.NAME, G.MAX_CLAIMS, G.IS_PLAYER FROM GROUPS G INNER JOIN GROUP_MEMBERS GM ON G.ID = GM.GROUP_ID WHERE GM.PLAYER = ?", player.getUniqueId().toString());

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int maxClaims = resultSet.getInt("MAX_CLAIMS");
                boolean isPlayer = resultSet.getBoolean("IS_PLAYER");
                List<GroupMember> members = getGroupMembers(id);
                groups.add(new StoredGroup(id, name, maxClaims, isPlayer, members));
            }
            resultSet.close();

            return groups;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public List<Group> getGroups() {
        try {
            List<Group> groups = new ArrayList<>();

            ResultSet resultSet = executeQuery("SELECT * FROM GROUPS");

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int maxClaims = resultSet.getInt("MAX_CLAIMS");
                boolean isPlayer = resultSet.getBoolean("IS_PLAYER");
                List<GroupMember> members = getGroupMembers(id);
                groups.add(new StoredGroup(id, name, maxClaims, isPlayer, members));
            }
            resultSet.close();

            return groups;
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
                members.add(claimManager.getGroup(group_id));
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

    public boolean createPlayerGroup(OfflinePlayer player, int maxClaims) {
        try {
            executeUpdate("INSERT INTO GROUPS (NAME, MAX_CLAIMS, IS_PLAYER) VALUES (?, ?, ?)", player.getName(), maxClaims, true);
            executeUpdate("INSERT INTO GROUP_MEMBERS (GROUP_ID, PLAYER, PERMISSION_LEVEL) VALUES ((SELECT ID FROM GROUPS WHERE NAME = ? AND IS_PLAYER = TRUE), ?, ?)", player.getName(), player.getUniqueId().toString(), PermissionLevel.OWNER.getLevel());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean deleteGroup(Group group) {
        try {
            executeUpdate("DELETE FROM GROUPS WHERE ID = ?", group.getId());
            executeUpdate("DELETE FROM GROUP_MEMBERS WHERE GROUP_ID = ?", group.getId());
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

    public boolean unclaimChunk(Claim claim) {
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

    public GroupMember getGroupMember(Group group, OfflinePlayer player) {
        try {
            ResultSet resultSet = executeQuery("SELECT * FROM GROUP_MEMBERS WHERE GROUP_ID = ? AND PLAYER = ?", group.getId(), player.getUniqueId().toString());
            if (resultSet.next()) {
                PermissionLevel level = PermissionLevel.fromLevel(resultSet.getInt("PERMISSION_LEVEL"));
                resultSet.close();
                return new StoredGroupMember(player, level);
            }
            resultSet.close();

            return null;
        } catch (SQLException ignored) {
            return null;
        }

    }

    public List<Claim> getClaims(Group group) {
        try {
            List<Claim> claims = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM CLAIMS WHERE OWNER = ?", group.getId());
            while (resultSet.next()) {
                int claim_id = resultSet.getInt("ID");
                String world_id = resultSet.getString("WORLD");
                World world = plugin.getServer().getWorld(UUID.fromString(world_id));
                if (world == null) {
                    plugin.getLogger().warning("World (" + world_id + ") not found for claim " + claim_id + " in group " + group.getId());
                    continue;
                }
                Chunk chunk = world.getChunkAt(resultSet.getInt("CHUNK_X"), resultSet.getInt("CHUNK_Z"));
                claims.add(ClaimsApi.getApi().getClaim(chunk));
            }
            resultSet.close();

            return claims;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public Integer getClaimAmount(Group group) {
        try {
            ResultSet resultSet = executeQuery("SELECT COUNT(*) FROM CLAIMS WHERE OWNER = ?", group.getId());
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                resultSet.close();
                return count;
            }
            resultSet.close();
            return 0;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public void setFlagState(Claim claim, Flag flag, boolean state) {
        try {
            // TODO: Make this better
            executeUpdate("INSERT OR IGNORE INTO CLAIM_FLAGS (CLAIM_ID, FLAG_IDENTIFIER, STATE) VALUES (?, ?, ?)", claim.getId(), flag.getKey().toString(), state);
            executeUpdate("UPDATE CLAIM_FLAGS SET STATE = ? WHERE CLAIM_ID = ? AND FLAG_IDENTIFIER = ?", state, claim.getId(), flag.getKey().toString());
        } catch (SQLException ignored) {

        }
    }

    public void unsetFlagState(Claim claim, Flag flag) {
        try {
            executeUpdate("DELETE FROM CLAIM_FLAGS WHERE CLAIM_ID = ? AND FLAG_IDENTIFIER = ?", claim.getId(), flag.getKey().toString());
        } catch (SQLException ignored) {

        }
    }

    public boolean addGroupToClaim(Claim claim, Group group) {
        try {
            executeUpdate("INSERT INTO CLAIM_MEMBERS (CLAIM_ID, GROUP_ID) VALUES (?, ?)", claim.getId(), group.getId());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean removeGroupFromClaim(Claim claim, Group group) {
        try {
            executeUpdate("DELETE FROM CLAIM_MEMBERS WHERE CLAIM_ID = ? AND GROUP_ID = ?", claim.getId(), group.getId());
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public Group getGroup(String name) {
        try {
            ResultSet resultSet = executeQuery("SELECT * FROM GROUPS WHERE IS_PLAYER = FALSE AND NAME = ?", name);
            if (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String groupName = resultSet.getString("NAME");
                int maxClaims = resultSet.getInt("MAX_CLAIMS");
                List<GroupMember> members = getGroupMembers(id);
                resultSet.close();
                return new StoredGroup(id, groupName, maxClaims, false, members);
            }
            resultSet.close();

            return null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public void setBlockInteractable(Claim claim, Material material, boolean state) {
        try {
            executeUpdate("INSERT OR IGNORE INTO BLOCK_INTERACTABLES (CLAIM_ID, BLOCK_IDENTIFIER, STATE) VALUES (?, ?, ?)", claim.getId(), material.name(), state);
            executeUpdate("UPDATE BLOCK_INTERACTABLES SET STATE = ? WHERE CLAIM_ID = ? AND BLOCK_IDENTIFIER = ?", state, claim.getId(), material.name());
        } catch (SQLException ignored) {

        }
    }

    public void setEntityInteractable(Claim claim, EntityType entityType, boolean damage, boolean interact) {
        try {
            executeUpdate("INSERT OR IGNORE INTO ENTITY_INTERACTABLES (CLAIM_ID, ENTITY_IDENTIFIER, INTERACT, DAMAGE) VALUES (?, ?, ?, ?)", claim.getId(), entityType.name(), interact, damage);
            executeUpdate("UPDATE ENTITY_INTERACTABLES SET INTERACT = ?, DAMAGE = ? WHERE CLAIM_ID = ? AND ENTITY_IDENTIFIER = ?", interact, damage, claim.getId(), entityType.name());
        } catch (SQLException ignored) {

        }
    }

    public void removeBlockInteractable(Claim claim, Material material) {
        try {
            executeUpdate("DELETE FROM BLOCK_INTERACTABLES WHERE CLAIM_ID = ? AND BLOCK_IDENTIFIER = ?", claim.getId(), material.name());
        } catch (SQLException ignored) {

        }
    }

    public void removeEntityInteractable(Claim claim, EntityType entityType) {
        try {
            executeUpdate("DELETE FROM ENTITY_INTERACTABLES WHERE CLAIM_ID = ? AND ENTITY_IDENTIFIER = ?", claim.getId(), entityType.name());
        } catch (SQLException ignored) {

        }
    }

    public void updateLastSeen(Group group) {
        try {
            executeUpdate("UPDATE CLAIMS SET LAST_ONLINE = ? WHERE OWNER = ?", System.currentTimeMillis(), group.getId());
        } catch (SQLException ignored) {

        }
    }

    public void setOwner(Chunk chunk, Group target) {
        try {
            executeUpdate("UPDATE CLAIMS SET OWNER = ? WHERE CHUNK_X = ? AND CHUNK_Z = ? AND WORLD = ?", target.getId(), chunk.getX(), chunk.getZ(), chunk.getWorld().getUID().toString());
        } catch (SQLException ignored) {

        }
    }

    @Deprecated(forRemoval = true)
    public List<Claim> getClaims(World world) {
        try {
            List<Claim> claims = new ArrayList<>();
            ResultSet resultSet = executeQuery("SELECT * FROM CLAIMS WHERE WORLD = ?", world.getUID().toString());
            while (resultSet.next()) {
                Chunk chunk = world.getChunkAt(resultSet.getInt("CHUNK_X"), resultSet.getInt("CHUNK_Z"), false);

                HashMap<Chunk, Claim> loadedClaims = claimManager.getLoadedClaims();
                if (loadedClaims.containsKey(chunk)) {
                    claims.add(loadedClaims.get(chunk));
                    continue;
                }

                int claim_id = resultSet.getInt("ID");

                // TODO: Implement lazy loading, for members, flags, interactables (flags are currently loaded because they are needed for the squaremap integration)

                StoredClaim storedClaim = new StoredClaim(
                        claim_id,
                        claimManager.getGroup(resultSet.getInt("OWNER")),
                        chunk,
                        List.of(),
                        List.of(),
                        List.of(),
                        getFlags(claim_id),
                        resultSet.getLong("CLAIMED_SINCE"),
                        resultSet.getLong("LAST_INTERACTION"),
                        resultSet.getLong("LAST_ONLINE")
                );

                claims.add(storedClaim);
            }
            resultSet.close();
            return claims;
        } catch (SQLException ignored) {
            return List.of();
        }
    }


    public void shutdown() {
        try {
            connection.close();
        } catch (SQLException ignored) {

        }
    }
}