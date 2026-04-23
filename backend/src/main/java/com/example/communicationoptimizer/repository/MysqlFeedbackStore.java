package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.FeedbackRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class MysqlFeedbackStore implements FeedbackStore {

    private final MysqlConnectionFactory connectionFactory;

    public MysqlFeedbackStore(MysqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(Long userId, FeedbackRequest request) {
        String sql = """
                INSERT INTO user_feedback (
                    user_id, record_id, variant_type, action_type, score, comment_text, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, request.getRecordId());
            statement.setString(3, request.getVariantType());
            statement.setString(4, request.getActionType());
            statement.setObject(5, request.getScore());
            statement.setString(6, request.getCommentText());
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save feedback", exception);
        }
    }
}
