package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.VariantDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class MysqlCommunicationStore implements CommunicationStore {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MysqlConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;

    public MysqlCommunicationStore(MysqlConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public OptimizeResponse saveGenerated(Long userId, String originalText, AnalysisDto analysis, List<VariantDto> variants) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            long recordId = insertRecord(connection, userId, originalText, analysis);
            insertVariants(connection, recordId, variants);
            connection.commit();

            OptimizeResponse response = new OptimizeResponse();
            response.setRecordId(recordId);
            response.setAnalysis(cloneAnalysis(analysis));
            response.setVariants(cloneVariants(variants));
            return response;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save communication record", exception);
        }
    }

    @Override
    public List<HistoryItemDto> listHistory(Long userId, int page, int limit) {
        String sql = """
                SELECT id, normalized_text, scene_code, relation_code, created_at, favorite
                FROM communication_record
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, limit);
            statement.setInt(3, (page - 1) * limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<HistoryItemDto> items = new ArrayList<>();
                while (resultSet.next()) {
                    HistoryItemDto item = new HistoryItemDto();
                    item.setRecordId(resultSet.getLong("id"));
                    item.setOriginalText(resultSet.getString("normalized_text"));
                    item.setScene(resultSet.getString("scene_code"));
                    item.setRelation(resultSet.getString("relation_code"));
                    item.setCreatedAt(TIME_FORMATTER.format(resultSet.getTimestamp("created_at").toLocalDateTime()));
                    item.setFavorite(resultSet.getBoolean("favorite"));
                    items.add(item);
                }
                return items;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list communication records", exception);
        }
    }

    @Override
    public CommunicationDetailDto getDetail(Long userId, Long recordId) {
        String recordSql = """
                SELECT id, normalized_text, scene_code, relation_code, goal_code, tone_tags, extra_meta,
                       emotion_level, favorite, created_at
                FROM communication_record
                WHERE user_id = ? AND id = ?
                """;
        String variantSql = """
                SELECT variant_type, title, content
                FROM communication_variant
                WHERE record_id = ?
                ORDER BY sort_order ASC, id ASC
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement recordStatement = connection.prepareStatement(recordSql);
             PreparedStatement variantStatement = connection.prepareStatement(variantSql)) {
            recordStatement.setLong(1, userId);
            recordStatement.setLong(2, recordId);
            try (ResultSet recordResultSet = recordStatement.executeQuery()) {
                if (!recordResultSet.next()) {
                    throw new NoSuchElementException("record not found");
                }

                CommunicationDetailDto detail = new CommunicationDetailDto();
                detail.setRecordId(recordResultSet.getLong("id"));
                detail.setOriginalText(recordResultSet.getString("normalized_text"));
                detail.setFavorite(recordResultSet.getBoolean("favorite"));
                detail.setCreatedAt(TIME_FORMATTER.format(recordResultSet.getTimestamp("created_at").toLocalDateTime()));

                AnalysisDto analysis = new AnalysisDto();
                analysis.setScene(recordResultSet.getString("scene_code"));
                analysis.setRelation(recordResultSet.getString("relation_code"));
                analysis.setGoal(recordResultSet.getString("goal_code"));
                analysis.setToneTags(readStringList(recordResultSet.getString("tone_tags")));
                analysis.setRiskPoints(readRiskPoints(recordResultSet.getString("extra_meta")));
                analysis.setEmotionLevel(recordResultSet.getInt("emotion_level"));
                detail.setAnalysis(analysis);

                variantStatement.setLong(1, recordId);
                try (ResultSet variantResultSet = variantStatement.executeQuery()) {
                    List<VariantDto> variants = new ArrayList<>();
                    while (variantResultSet.next()) {
                        VariantDto variant = new VariantDto();
                        variant.setType(variantResultSet.getString("variant_type"));
                        variant.setTitle(variantResultSet.getString("title"));
                        variant.setText(variantResultSet.getString("content"));
                        variants.add(variant);
                    }
                    detail.setVariants(variants);
                }

                return detail;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load communication detail", exception);
        }
    }

    @Override
    public HistoryItemDto updateFavorite(Long userId, Long recordId, boolean favorite) {
        String updateSql = "UPDATE communication_record SET favorite = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setBoolean(1, favorite);
            statement.setLong(2, userId);
            statement.setLong(3, recordId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new NoSuchElementException("record not found");
            }
            return toHistoryItem(getDetail(userId, recordId));
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update favorite", exception);
        }
    }

    @Override
    public void delete(Long userId, Long recordId) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            deleteByRecordId(connection, "DELETE FROM communication_variant WHERE record_id = ?", recordId);
            deleteByRecordId(connection, "DELETE FROM user_feedback WHERE record_id = ?", recordId);
            deleteByRecordId(connection, "DELETE FROM media_asset WHERE record_id = ?", recordId);

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM communication_record WHERE user_id = ? AND id = ?")) {
                statement.setLong(1, userId);
                statement.setLong(2, recordId);
                int deleted = statement.executeUpdate();
                if (deleted == 0) {
                    throw new NoSuchElementException("record not found");
                }
            }
            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete communication record", exception);
        }
    }

    private long insertRecord(Connection connection, Long userId, String originalText, AnalysisDto analysis) throws SQLException {
        String sql = """
                INSERT INTO communication_record (
                    user_id, source_type, input_text, asr_text, normalized_text, scene_code, scene_source,
                    relation_code, goal_code, tone_tags, emotion_level, provider_code, model_name, prompt_version,
                    status, favorite, selected_variant_type, extra_meta, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setString(2, "text");
            statement.setString(3, originalText);
            statement.setString(4, null);
            statement.setString(5, originalText);
            statement.setString(6, analysis.getScene());
            statement.setString(7, "auto");
            statement.setString(8, analysis.getRelation());
            statement.setString(9, analysis.getGoal());
            statement.setString(10, writeJson(analysis.getToneTags()));
            statement.setInt(11, analysis.getEmotionLevel());
            statement.setString(12, null);
            statement.setString(13, null);
            statement.setString(14, null);
            statement.setString(15, "success");
            statement.setBoolean(16, false);
            statement.setString(17, null);
            statement.setString(18, writeRiskMeta(analysis.getRiskPoints()));
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(19, now);
            statement.setTimestamp(20, now);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new IllegalStateException("Failed to create communication record");
    }

    private void insertVariants(Connection connection, long recordId, List<VariantDto> variants) throws SQLException {
        String sql = """
                INSERT INTO communication_variant (
                    record_id, variant_type, title, content, style_tags, sort_order, tts_media_id, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int sortOrder = 1;
            for (VariantDto variant : variants) {
                statement.setLong(1, recordId);
                statement.setString(2, variant.getType());
                statement.setString(3, variant.getTitle());
                statement.setString(4, variant.getText());
                statement.setString(5, null);
                statement.setInt(6, sortOrder++);
                statement.setObject(7, null);
                statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteByRecordId(Connection connection, String sql, Long recordId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, recordId);
            statement.executeUpdate();
        }
    }

    private HistoryItemDto toHistoryItem(CommunicationDetailDto detail) {
        HistoryItemDto item = new HistoryItemDto();
        item.setRecordId(detail.getRecordId());
        item.setOriginalText(detail.getOriginalText());
        item.setScene(detail.getAnalysis().getScene());
        item.setRelation(detail.getAnalysis().getRelation());
        item.setCreatedAt(detail.getCreatedAt());
        item.setFavorite(detail.isFavorite());
        return item;
    }

    private AnalysisDto cloneAnalysis(AnalysisDto source) {
        AnalysisDto target = new AnalysisDto();
        target.setScene(source.getScene());
        target.setRelation(source.getRelation());
        target.setGoal(source.getGoal());
        target.setToneTags(source.getToneTags() != null ? List.copyOf(source.getToneTags()) : List.of());
        target.setRiskPoints(source.getRiskPoints() != null ? List.copyOf(source.getRiskPoints()) : List.of());
        target.setEmotionLevel(source.getEmotionLevel());
        return target;
    }

    private List<VariantDto> cloneVariants(List<VariantDto> source) {
        List<VariantDto> variants = new ArrayList<>();
        for (VariantDto item : source) {
            VariantDto copy = new VariantDto();
            copy.setType(item.getType());
            copy.setTitle(item.getTitle());
            copy.setText(item.getText());
            copy.setAudioUrl(item.getAudioUrl());
            variants.add(copy);
        }
        return variants;
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values != null ? values : List.of());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize JSON column", exception);
        }
    }

    private String writeRiskMeta(List<String> values) {
        try {
            return objectMapper.writeValueAsString(new RiskMeta(values != null ? values : List.of()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize risk meta", exception);
        }
    }

    private List<String> readStringList(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<String>>() { });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse JSON column", exception);
        }
    }

    private List<String> readRiskPoints(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            RiskMeta meta = objectMapper.readValue(rawJson, RiskMeta.class);
            return meta.getRiskPoints() != null ? meta.getRiskPoints() : List.of();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse risk meta", exception);
        }
    }

    public static class RiskMeta {
        private List<String> riskPoints;

        public RiskMeta() {
        }

        public RiskMeta(List<String> riskPoints) {
            this.riskPoints = riskPoints;
        }

        public List<String> getRiskPoints() {
            return riskPoints;
        }

        public void setRiskPoints(List<String> riskPoints) {
            this.riskPoints = riskPoints;
        }
    }
}
