package com.kalimero2.team.claims.paper.storage;


import com.kalimero2.team.claims.paper.PaperClaims;
import org.intellij.lang.annotations.Language;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        // ID, CHUNK, GROUP

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_MEMBERS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "GROUP_ID INTEGER NOT NULL REFERENCES GROUPS(ID)" +
                ");");


        // CLAIM_FLAGS
        // CHUNK_ID, FLAG_ID, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS CLAIM_FLAGS(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "FLAG_ID TINYINT NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");


        // BLOCK_INTERACTABLES
        // CHUNK_ID, BLOCK_IDENTIFIER, STATE

        executeUpdate("CREATE TABLE IF NOT EXISTS BLOCK_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "BLOCK_IDENTIFIER VARCHAR(64) NOT NULL," +
                "STATE BOOLEAN NOT NULL" +
                ");");

        // ENTITY_INTERACTABLES
        // CHUNK_ID, ENTITY_IDENTIFIER, INTERACT, DAMAGE

        executeUpdate("CREATE TABLE IF NOT EXISTS ENTITY_INTERACTABLES(" +
                "CLAIM_ID INTEGER NOT NULL REFERENCES CLAIMS(ID)," +
                "ENTITY_IDENTIFIER VARCHAR(64) NOT NULL," +
                "INTERACT BOOLEAN NOT NULL," +
                "DAMAGE BOOLEAN NOT NULL" +
                ");");

    }

}