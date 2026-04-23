package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.VariantDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryCommunicationStore implements CommunicationStore {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AtomicLong recordIdGenerator = new AtomicLong(1000);
    private final ConcurrentMap<Long, StoredRecord> records = new ConcurrentHashMap<>();

    @Override
    public OptimizeResponse saveGenerated(String originalText, AnalysisDto analysis, List<VariantDto> variants) {
        long recordId = recordIdGenerator.incrementAndGet();

        StoredRecord storedRecord = new StoredRecord();
        storedRecord.recordId = recordId;
        storedRecord.originalText = originalText;
        storedRecord.analysis = cloneAnalysis(analysis);
        storedRecord.variants = cloneVariants(variants);
        storedRecord.favorite = false;
        storedRecord.createdAt = LocalDateTime.now();
        records.put(recordId, storedRecord);

        OptimizeResponse response = new OptimizeResponse();
        response.setRecordId(recordId);
        response.setAnalysis(cloneAnalysis(storedRecord.analysis));
        response.setVariants(cloneVariants(storedRecord.variants));
        return response;
    }

    @Override
    public List<HistoryItemDto> listHistory() {
        return records.values().stream()
                .sorted(Comparator.comparing((StoredRecord record) -> record.createdAt).reversed())
                .map(this::toHistoryItem)
                .toList();
    }

    @Override
    public CommunicationDetailDto getDetail(Long recordId) {
        StoredRecord record = getRecord(recordId);
        CommunicationDetailDto detail = new CommunicationDetailDto();
        detail.setRecordId(record.recordId);
        detail.setOriginalText(record.originalText);
        detail.setAnalysis(cloneAnalysis(record.analysis));
        detail.setVariants(cloneVariants(record.variants));
        detail.setFavorite(record.favorite);
        detail.setCreatedAt(TIME_FORMATTER.format(record.createdAt));
        return detail;
    }

    @Override
    public HistoryItemDto updateFavorite(Long recordId, boolean favorite) {
        StoredRecord record = getRecord(recordId);
        record.favorite = favorite;
        return toHistoryItem(record);
    }

    @Override
    public void delete(Long recordId) {
        if (records.remove(recordId) == null) {
            throw new NoSuchElementException("record not found");
        }
    }

    private StoredRecord getRecord(Long recordId) {
        StoredRecord record = records.get(recordId);
        if (record == null) {
            throw new NoSuchElementException("record not found");
        }
        return record;
    }

    private HistoryItemDto toHistoryItem(StoredRecord record) {
        HistoryItemDto item = new HistoryItemDto();
        item.setRecordId(record.recordId);
        item.setOriginalText(record.originalText);
        item.setScene(record.analysis.getScene());
        item.setRelation(record.analysis.getRelation());
        item.setCreatedAt(TIME_FORMATTER.format(record.createdAt));
        item.setFavorite(record.favorite);
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

    private static class StoredRecord {
        private Long recordId;
        private String originalText;
        private AnalysisDto analysis;
        private List<VariantDto> variants;
        private boolean favorite;
        private LocalDateTime createdAt;
    }
}
