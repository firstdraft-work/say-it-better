package com.example.communicationoptimizer.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class MysqlMediaStore implements MediaStore {

    private final MysqlConnectionFactory connectionFactory;

    public MysqlMediaStore(MysqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public StoredMedia save(String fileName, String source, int durationMs, String fileUrl, String localFilePath) {
        String sql = """
                INSERT INTO media_asset (
                    user_id, record_id, variant_id, asset_type, storage_provider, bucket_name,
                    object_key, file_url, local_file_path, mime_type, duration_ms, size_bytes, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, 0L);
            statement.setObject(2, null);
            statement.setObject(3, null);
            statement.setString(4, "input_audio");
            statement.setString(5, "mock-storage");
            statement.setString(6, null);
            statement.setString(7, fileName);
            statement.setString(8, fileUrl);
            statement.setString(9, localFilePath);
            statement.setString(10, "audio/mpeg");
            statement.setInt(11, durationMs);
            statement.setLong(12, 0L);
            statement.setString(13, "active");
            statement.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    StoredMedia storedMedia = new StoredMedia();
                    storedMedia.setMediaId(keys.getLong(1));
                    storedMedia.setFileName(fileName);
                    storedMedia.setSource(source);
                    storedMedia.setDurationMs(durationMs);
                    storedMedia.setFileUrl(fileUrl);
                    storedMedia.setLocalFilePath(localFilePath);
                    return storedMedia;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save media", exception);
        }
        throw new IllegalStateException("Failed to create media record");
    }

    @Override
    public StoredMedia get(Long mediaId) {
        String sql = """
                SELECT id, object_key, duration_ms, file_url, local_file_path
                FROM media_asset
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, mediaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NoSuchElementException("media not found");
                }
                StoredMedia storedMedia = new StoredMedia();
                storedMedia.setMediaId(resultSet.getLong("id"));
                storedMedia.setFileName(resultSet.getString("object_key"));
                storedMedia.setSource("voice");
                storedMedia.setDurationMs(resultSet.getInt("duration_ms"));
                storedMedia.setFileUrl(resultSet.getString("file_url"));
                storedMedia.setLocalFilePath(resultSet.getString("local_file_path"));
                return storedMedia;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load media", exception);
        }
    }
}
