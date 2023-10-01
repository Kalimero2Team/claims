package com.kalimero2.team.claims.paper.storage;


import com.kalimero2.team.claims.paper.PaperClaims;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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


    public ResultSet executeQuery(@Language(value = "SQL") String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int executeUpdate(@Language(value = "SQL") String sql) {
        try {
            return connection.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private void createTablesIfNotExists() {

        //
        // Group related tables
        //

        // GROUP
        // ID, OWNER, MAX_CHUNKS, IS_PLAYER

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUPS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "OWNER VARCHAR(36) NOT NULL," +
                "MAX_CHUNKS INTEGER NOT NULL," +
                "IS_PLAYER BOOLEAN NOT NULL" +
                ");");


        // GROUP_MEMBERS
        // ID, NAME, PLAYER(UUID)

        executeUpdate("CREATE TABLE IF NOT EXISTS GROUP_MEMBERS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUP(ID)," +
                "PLAYER VARCHAR(36) NOT NULL" +
                ");");



        //
        //  Chunk related Tables
        //

        // CHUNKS
        // ID, NAME, OWNER (GROUP), CHUNK_X, CHUNK_Z, WORLD(UUID), FIRST_CLAIMED, LAST_INTERACTION, LAST_ONLINE

        executeUpdate("CREATE TABLE IF NOT EXISTS CHUNKS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "OWNER INTEGER NOT NULL REFERENCES GROUP(ID)," +
                "CHUNK_X INTEGER NOT NULL," +
                "CHUNK_Z INTEGER NOT NULL," +
                "WORLD_UUID VARCHAR(36) NOT NULL," +
                "FIRST_CLAIMED DATETIME NOT NULL, " +
                "LAST_INTERACTION DATETIME NOT NULL," +
                "LAST_ONLINE DATETIME NOT NULL" +
                ");");


        // CHUNK_MEMBERS
        // ID, CHUNK, PLAYER

        executeUpdate("CREATE TABLE IF NOT EXISTS CHUNK_MEMBERS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CHUNK INTEGER NOT NULL REFERENCES CHUNKS(ID)," +
                "GROUP INTEGER NOT NULL REFERENCES GROUPS(ID)" +
                ");");


        // FLAGS
        // ID, CHUNK_ID, FLAG_ID, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS FLAGS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CHUNK_ID INTEGER NOT NULL REFERENCES CHUNKS(ID)," +
                "FLAG_ID TINYINT NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");


        // BLOCK_INTERACTABLES
        // ID, CHUNK_ID, BLOCK_ID, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS BLOCK_INTERACTABLES(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CHUNK_ID INTEGER NOT NULL REFERENCES CHUNKS(ID)," +
                "BLOCK_ID INTEGER NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");

        // ENTITY_INTERACTABLES
        // ID, CHUNK_ID, BLOCK_ID, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS ENTITY_INTERACTABLES(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CHUNK_ID INTEGER NOT NULL REFERENCES CHUNKS(ID)," +
                "ENTITY_ID INTEGER NOT NULL," +
                "INTERACT BOOLEAN NOT NULL," +
                "DAMAGE BOOLEAN NOT NULL," +
                ");");

    }




    //
    //  WAYSTONES
    //

    public void addWaystone(@NotNull String name, @NotNull UUID owner, int visibility, int category, @NotNull int x, @NotNull int y, @NotNull int z, @NotNull UUID world) {
        executeUpdate("INSERT INTO WAYSTONES(NAME, OWNER_UUID, VISIBILITY, CATEGORY, CHUNK_X, CHUNK_Z, BLOCK_X, BLOCK_Y, BLOCK_Z, WORLD_UUID, USES) VALUES('" + name + "', '" + owner + "', " + visibility + ", " + category + ", " + (x >> 4) + ", " + (z >> 4) + ", " + x + ", " + y + ", " + z + ", '" + world + "', 0);");
    }

    public void removeWaystone(int id) {
        executeUpdate("DELETE FROM WAYSTONES WHERE ID = " + id + ";");
    }

    public void renameWaystone(int id, String newName) {
        executeUpdate("UPDATE WAYSTONES SET NAME = '" + newName + "' WHERE ID = " + id + ";");
    }

    public void updateWaystone(StoredWaystone waystone) {
        executeUpdate("UPDATE WAYSTONES SET NAME = '" + waystone.name() + "', OWNER_UUID = '" + waystone.owner() + "', VISIBILITY = " + waystone.visibility().ordinal() + ", CATEGORY = " + waystone.category().id() + ", CHUNK_X = " + waystone.chunk_x() + ", CHUNK_Z = " + waystone.chunk_z() + ", BLOCK_X = " + waystone.block_x() + ", BLOCK_Y = " + waystone.block_y() + ", BLOCK_Z = " + waystone.block_z() + ", WORLD_UUID = '" + waystone.world() + "', USES = '" + waystone.uses() + "' WHERE ID = " + waystone.id() + ";");
        display.updateDisplay(getWaystone(waystone.id()));
    }


    /**
     * Get a waystone by its id
     * @param id ID of the requestd Waystone
     * @return the waystone if it exists, otherwise returns null
     */
    public StoredWaystone getWaystone(int id) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE ID = " + id + ";")) {
            if (resultSet.next()) {
                return new StoredWaystone(resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("OWNER_UUID"),
                        resultSet.getInt("VISIBILITY"),
                        resultSet.getInt("CATEGORY"),
                        resultSet.getInt("BLOCK_X"),
                        resultSet.getInt("BLOCK_Y"),
                        resultSet.getInt("BLOCK_Z"),
                        resultSet.getString("WORLD_UUID"),
                        resultSet.getInt("USES"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get a waystone by its name
     * @param name Name of the requestd Waystone
     * @return the waystone if it exists, otherwise returns null
     */
    public StoredWaystone getWaystone(String name) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE NAME = '" + name + "' ORDER BY ID ASC;")) {
            if (resultSet.next()) {
                return new StoredWaystone(resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("OWNER_UUID"),
                        resultSet.getInt("VISIBILITY"),
                        resultSet.getInt("CATEGORY"),
                        resultSet.getInt("BLOCK_X"),
                        resultSet.getInt("BLOCK_Y"),
                        resultSet.getInt("BLOCK_Z"),
                        resultSet.getString("WORLD_UUID"),
                        resultSet.getInt("USES"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StoredWaystone getWaystone(int block_x, int block_y, int block_z, UUID world) {
        try (ResultSet resultSet = executeQuery("SELECT  * FROM WAYSTONES WHERE BLOCK_X = " + block_x + " AND BLOCK_Y = " + block_y + " AND BLOCK_Z = " + block_z + " AND WORLD_UUID = '"+world+"';")) {
            if (resultSet.next()) {
                return new StoredWaystone(resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("OWNER_UUID"),
                        resultSet.getInt("VISIBILITY"),
                        resultSet.getInt("CATEGORY"),
                        resultSet.getInt("BLOCK_X"),
                        resultSet.getInt("BLOCK_Y"),
                        resultSet.getInt("BLOCK_Z"),
                        resultSet.getString("WORLD_UUID"),
                        resultSet.getInt("USES"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StoredWaystone[] getWaystones(int chunk_x, int chunk_z, UUID world) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE CHUNK_X = " + chunk_x + " AND CHUNK_Z = " + chunk_z + " AND WORLD_UUID = '"+world+"';")) {
            return getWaystonesFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones(Player player) {
        SortMode sortMode = getSortMode(player);
        String sql = "SELECT * FROM WAYSTONES";
        try  {
            ResultSet resultSet = null;
            switch (sortMode) {
                case ALPHABETICAL -> resultSet = executeQuery(sql + " ORDER BY NAME COLLATE NOCASE ASC;");
                case ALPHABETICAL_DESCENDING -> resultSet = executeQuery(sql + " ORDER BY NAME COLLATE NOCASE DESC;");
                case NUMERIC -> resultSet = executeQuery(sql + " ORDER BY ID ASC;");
                case NUMERIC_DESCENDING -> resultSet = executeQuery(sql + " ORDER BY ID DESC;");
                case POPULARITY -> resultSet = executeQuery(sql + " ORDER BY USES DESC;");
                case POPULARITY_ASCENDING -> resultSet = executeQuery(sql + " ORDER BY USES ASC;");
            }
            assert resultSet != null;
            StoredWaystone[] all = getWaystonesFromResultSet(resultSet, player);
            StoredWaystone[] favs = getFavoriteWaystones(player);
            List<StoredWaystone> result = new ArrayList<StoredWaystone>(Arrays.stream(all).toList());
            result.removeAll(Arrays.stream(favs).toList());
            result.addAll(0, Arrays.stream(favs).toList());
            return result.toArray(new StoredWaystone[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones(Player player, String search) {
        SortMode sortMode = getSortMode(player);
        String sql = "SELECT * FROM WAYSTONES WHERE NAME LIKE '%" + search + "%'";
        try  {
            ResultSet resultSet = null;
            switch (sortMode) {
                case ALPHABETICAL -> resultSet = executeQuery(sql + " ORDER BY NAME COLLATE NOCASE ASC;");
                case ALPHABETICAL_DESCENDING -> resultSet = executeQuery(sql + " ORDER BY NAME COLLATE NOCASE DESC;");
                case NUMERIC -> resultSet = executeQuery(sql + " ORDER BY ID ASC;");
                case NUMERIC_DESCENDING -> resultSet = executeQuery(sql + " ORDER BY ID DESC;");
                case POPULARITY -> resultSet = executeQuery(sql + " ORDER BY USES DESC;");
                case POPULARITY_ASCENDING -> resultSet = executeQuery(sql + " ORDER BY USES ASC;");
            }
            assert resultSet != null;
            return getWaystonesFromResultSet(resultSet, player);
//            StoredWaystone[] favs = getFavoriteWaystones(player);
//            List<StoredWaystone> result = new ArrayList<StoredWaystone>(Arrays.stream(all).toList());
//            result.removeAll(Arrays.stream(favs).toList());
//            result.addAll(0, Arrays.stream(favs).toList());
//            return result.toArray(new StoredWaystone[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones(UUID world) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"';")) {
            return getWaystonesFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones(UUID world, String searchTerm) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' AND NAME LIKE '%" + searchTerm + "%' ORDER BY NAME COLLATE NOCASE ASC;")) {
            return getWaystonesFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones(UUID world, Player player) {
        SortMode sortMode = getSortMode(player);
        try  {
            ResultSet resultSet = null;
            switch (sortMode) {
                case ALPHABETICAL -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY NAME COLLATE NOCASE ASC;");
                case ALPHABETICAL_DESCENDING -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY NAME COLLATE NOCASE DESC;");
                case NUMERIC -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY ID ASC;");
                case NUMERIC_DESCENDING -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY ID DESC;");
                case POPULARITY -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY USES DESC;");
                case POPULARITY_ASCENDING -> resultSet = executeQuery("SELECT * FROM WAYSTONES WHERE WORLD_UUID = '"+world+"' ORDER BY USES ASC;");
            }
            assert resultSet != null;
            StoredWaystone[] all = getWaystonesFromResultSet(resultSet, player);
            StoredWaystone[] favs = getFavoriteWaystones(world, player);
            List<StoredWaystone> result = new ArrayList<StoredWaystone>(Arrays.stream(all).toList());
            result.removeAll(Arrays.stream(favs).toList());
            result.addAll(0, Arrays.stream(favs).toList());
            return result.toArray(new StoredWaystone[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];
    }


    public StoredWaystone[] getFavoriteWaystones(Player player) {
        SortMode sortMode = getSortMode(player);
        try  {
            ResultSet resultSet = null;
            String sql =    "SELECT WAYSTONES.ID, WAYSTONES.NAME, WAYSTONES.OWNER_UUID, WAYSTONES.VISIBILITY, WAYSTONES.CATEGORY, WAYSTONES.CHUNK_X, WAYSTONES.CHUNK_Z, WAYSTONES.BLOCK_X, WAYSTONES.BLOCK_Y, WAYSTONES.BLOCK_Z, WAYSTONES.WORLD_UUID, WAYSTONES.USES " +
                    "FROM WAYSTONES, FAVORITES " +
                    "WHERE FAVORITES.WAYSTONE = WAYSTONES.ID " +
                    "AND PLAYER = '"+player.getUniqueId()+"' ";
            switch (sortMode) {
                case ALPHABETICAL -> resultSet = executeQuery(sql + "ORDER BY NAME COLLATE NOCASE ASC;");
                case ALPHABETICAL_DESCENDING -> resultSet = executeQuery(sql + "ORDER BY NAME COLLATE NOCASE DESC;");
                case NUMERIC -> resultSet = executeQuery(sql + "ORDER BY WAYSTONES.ID ASC;");
                case NUMERIC_DESCENDING -> resultSet = executeQuery(sql + "ORDER BY WAYSTONES.ID DESC;");
                case POPULARITY -> resultSet = executeQuery(sql + "ORDER BY USES DESC;");
                case POPULARITY_ASCENDING -> resultSet = executeQuery(sql + "ORDER BY USES ASC;");
            }
            return getWaystonesFromResultSet(resultSet, player);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {}
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getFavoriteWaystones(UUID world, Player player) {
        SortMode sortMode = getSortMode(player);
        try  {
            ResultSet resultSet = null;
            String sql =    "SELECT WAYSTONES.ID, WAYSTONES.NAME, WAYSTONES.OWNER_UUID, WAYSTONES.VISIBILITY, WAYSTONES.CATEGORY, WAYSTONES.CHUNK_X, WAYSTONES.CHUNK_Z, WAYSTONES.BLOCK_X, WAYSTONES.BLOCK_Y, WAYSTONES.BLOCK_Z, WAYSTONES.WORLD_UUID, WAYSTONES.USES " +
                    "FROM WAYSTONES, FAVORITES " +
                    "WHERE FAVORITES.WAYSTONE = WAYSTONES.ID " +
                    "AND WORLD_UUID = '"+world+"' " +
                    "AND PLAYER = '"+player.getUniqueId()+"' ";
            switch (sortMode) {
                case ALPHABETICAL -> resultSet = executeQuery(sql + "ORDER BY NAME COLLATE NOCASE ASC;");
                case ALPHABETICAL_DESCENDING -> resultSet = executeQuery(sql + "ORDER BY NAME COLLATE NOCASE DESC;");
                case NUMERIC -> resultSet = executeQuery(sql + "ORDER BY WAYSTONES.ID ASC;");
                case NUMERIC_DESCENDING -> resultSet = executeQuery(sql + "ORDER BY WAYSTONES.ID DESC;");
                case POPULARITY -> resultSet = executeQuery(sql + "ORDER BY USES DESC;");
                case POPULARITY_ASCENDING -> resultSet = executeQuery(sql + "ORDER BY USES ASC;");
            }
            return getWaystonesFromResultSet(resultSet, player);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {}
        return new StoredWaystone[0];
    }

    public StoredWaystone[] getWaystones() {
        try (ResultSet resultSet = executeQuery("SELECT * FROM WAYSTONES;")) {
            return getWaystonesFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StoredWaystone[0];

    }

    @NotNull
    private StoredWaystone[] getWaystonesFromResultSet(ResultSet resultSet) throws SQLException {
        Set<StoredWaystone> waystones = new LinkedHashSet<>();
        try {
            while (resultSet.next()) {
                waystones.add(new StoredWaystone(resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("OWNER_UUID"),
                        resultSet.getInt("VISIBILITY"),
                        resultSet.getInt("CATEGORY"),
                        resultSet.getInt("BLOCK_X"),
                        resultSet.getInt("BLOCK_Y"),
                        resultSet.getInt("BLOCK_Z"),
                        resultSet.getString("WORLD_UUID"),
                        resultSet.getInt("USES"))
                );
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return waystones.toArray(new StoredWaystone[0]);
    }

    @NotNull
    private StoredWaystone[] getWaystonesFromResultSet(ResultSet resultSet, Player player) throws SQLException {
        Set<StoredWaystone> waystones = new LinkedHashSet<>();
        try {
            while (resultSet.next()) {
                StoredWaystone waystone = new StoredWaystone(resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("OWNER_UUID"),
                        resultSet.getInt("VISIBILITY"),
                        resultSet.getInt("CATEGORY"),
                        resultSet.getInt("BLOCK_X"),
                        resultSet.getInt("BLOCK_Y"),
                        resultSet.getInt("BLOCK_Z"),
                        resultSet.getString("WORLD_UUID"),
                        resultSet.getInt("USES"));
                if (waystone.visibleTo(player) || forceMode(player)) waystones.add(waystone);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return waystones.toArray(new StoredWaystone[0]);
    }

    /**
     * @param name the requested name
     * @return true if the name is not used by any other waystone – false if used by at least one waystone
     */
    public boolean nameFree(String name) {
        return getWaystone(name) == null;
    }




    //
    // Accesslist
    //

    public void setVisibility(int id, Visibility visibility) {
        executeUpdate("UPDATE WAYSTONES SET VISIBILITY = " + visibility.ordinal() + " WHERE ID = " + id + ";");
    }

    public boolean onAccesslist(OfflinePlayer player, int id) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM ACCESSLISTS WHERE PLAYER = '"+player.getUniqueId()+"';")) {
            return resultSet.next();
        } catch (SQLException ignored) {}
        return false;
    }

    public void addAccess(OfflinePlayer player, int id) {
        executeUpdate("INSERT INTO ACCESSLISTS(PLAYER, WAYSTONE) VALUES('"+player.getUniqueId()+"', " + id + ");");
    }

    public void removeAccess(OfflinePlayer player, int id) {
        executeUpdate("DELETE FROM ACCESSLISTS WHERE PLAYER = '"+player.getUniqueId()+"' AND WAYSTONE = " + id + ";");
    }

    public List<OfflinePlayer> accesslist(int id) {
        List<OfflinePlayer> result = new ArrayList<>();
        try (ResultSet resultSet = executeQuery("SELECT * FROM ACCESSLISTS WHERE WAYSTONE = "+id+";")) {
            while (resultSet.next()) {
                result.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("PLAYER"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }





    //
    // Favorites
    //
    public Integer[] getFavorites(Player player) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM FAVORITES WHERE PLAYER = '"+player.getUniqueId()+"';")) {
            Set<Integer> favorites = new LinkedHashSet<>();
            while (resultSet.next()) {
                favorites.add(resultSet.getInt("WAYSTONE"));
            }
            return favorites.toArray(new Integer[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Integer[0];
    }

    public void addFavorite(Player player, int id) {
        executeUpdate("INSERT INTO FAVORITES(PLAYER, WAYSTONE) VALUES('"+player.getUniqueId()+"', " + id + ");");
    }

    public void removeFavorite(Player player, int id) {
        executeUpdate("DELETE FROM FAVORITES WHERE PLAYER = '"+player.getUniqueId()+"' AND WAYSTONE = " + id + ";");
    }






    //
    // SortMode
    //

    public SortMode getSortMode(Player player) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM SORTMODE WHERE PLAYER = '"+player.getUniqueId()+"';")) {
            return SortMode.valueByNumber(resultSet.getInt("MODE"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return SortMode.ALPHABETICAL;
    }

    public void setSortMode(Player player, SortMode sortMode) {
        executeUpdate("REPLACE INTO SORTMODE(PLAYER, MODE) VALUES('" + player.getUniqueId() + "', " + sortMode.ordinal() + ");");
    }



    //
    // Force Mode Users
    //

    public boolean forceMode(Player player) {
        return forceMode.contains(player);
    }

    public boolean forceMode(Player player, boolean active) {
        if (active && !forceMode.contains(player)) {
            forceMode.add(player);
            return true;
        }
        else forceMode.remove(player);
        return false;
    }



    //
    // Categories
    //

    public List<Category> getCategories() {
        List<Category> result = new ArrayList<>();
        try (ResultSet resultSet = executeQuery("SELECT * FROM CATEGORIES;")) {
            while (resultSet.next()) {
                result.add(new Category(resultSet.getInt("ID"), resultSet.getString("NAME"), resultSet.getBoolean("PUBLIC")));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (result.size() == 0) {
            result.add(Category.NONE);
        }
        return result;
    }

    public @Nullable Category getCategory(int id) {
        try (ResultSet resultSet = executeQuery("SELECT * FROM CATEGORIES WHERE ID = " + id + ";")) {
            if (resultSet.next()) {
                return new Category(id, resultSet.getString("NAME"), resultSet.getBoolean("PUBLIC"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCategory(String name, boolean isPublic) {
        return executeUpdate("INSERT INTO CATEGORIES(NAME, PUBLIC) VALUES('"+ name + "', " + isPublic + ");") == 1;
    }

    public void removeCategory(String name) {
        executeUpdate("DELETE FROM CATEGORIES WHERE NAME ='"+ name + "';");
    }

    public void removeCategory(int id) {
        executeUpdate("DELETE FROM CATEGORIES WHERE ID ='"+ id + "';");
    }



}