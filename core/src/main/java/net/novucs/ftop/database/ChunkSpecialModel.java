package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkSpecialModel {

    private static final String UPDATE = "UPDATE `chunk_special_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `chunk_special_count` (`chunk_id`, `special_id`, `count`) VALUES(?, ?, ?)";

    private final List<Map.Entry<Integer, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private ChunkSpecialModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static ChunkSpecialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new ChunkSpecialModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

        // Add newly created chunk-special relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<Integer, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                int chunkId = entry.getKey();
                int specialId = entry.getValue();
                identityCache.setChunkSpecialId(chunkId, specialId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
    }

    public void addBatch(int chunkId, Map<String, Integer> specials) throws SQLException {
        // Persist all special counters for this specific chunk worth.
        for (Map.Entry<String, Integer> entry : specials.entrySet()) {
            String specialName = entry.getKey();
            int count = entry.getValue();
            int specialId = identityCache.getSpecialId(specialName);
            addBatch(chunkId, specialId, count);
        }
    }

    public void addBatch(int chunkId, int specialId, int count) throws SQLException {
        Integer relationId = identityCache.getChunkSpecialId(chunkId, specialId);
        Map.Entry<Integer, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(chunkId, specialId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(chunkId, specialId, count);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(int chunkId, int specialId, int count) throws SQLException {
        insert.setInt(1, chunkId);
        insert.setInt(2, specialId);
        insert.setInt(3, count);
        insert.addBatch();
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
