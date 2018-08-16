package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpecialModel {

    private static final String INSERT = "INSERT INTO `special` (`name`) VALUES(?)";

    private final List<String> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private SpecialModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static SpecialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new SpecialModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        insert.executeBatch();

        ResultSet resultSet = insert.getGeneratedKeys();

        for (String specialName : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setSpawnerId(specialName, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<String> specials) throws SQLException {
        for (String special : specials) {
            addBatch(special);
        }
    }

    public void addBatch(String specialName) throws SQLException {
        if (insertionQueue.contains(specialName) || identityCache.hasSpawner(specialName)) {
            return;
        }

        insert.setString(1, specialName);
        insert.addBatch();
        insertionQueue.add(specialName);
    }
}
