package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.adapter.llm.LlmProvider;
import com.example.communicationoptimizer.adapter.tts.TtsProvider;
import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.HistoryPageDto;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.TtsResponse;
import com.example.communicationoptimizer.repository.CommunicationStore;
import com.example.communicationoptimizer.service.CommunicationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunicationServiceImpl implements CommunicationService {

    private final LlmProvider llmProvider;
    private final TtsProvider ttsProvider;
    private final CommunicationStore communicationStore;

    public CommunicationServiceImpl(LlmProvider llmProvider, TtsProvider ttsProvider, CommunicationStore communicationStore) {
        this.llmProvider = llmProvider;
        this.ttsProvider = ttsProvider;
        this.communicationStore = communicationStore;
    }

    @Override
    public OptimizeResponse optimize(Long userId, OptimizeRequest request) {
        OptimizeResponse generated = llmProvider.generate(request);
        return communicationStore.saveGenerated(
                userId,
                request.getText() != null ? request.getText() : "",
                generated.getAnalysis(),
                generated.getVariants()
        );
    }

    @Override
    public HistoryPageDto listHistory(Long userId, int page, int limit) {
        List<HistoryItemDto> items = communicationStore.listHistory(userId, page + 1, limit);
        HistoryPageDto result = new HistoryPageDto();
        result.setItems(items);
        result.setPage(page);
        result.setLimit(limit);
        result.setHasMore(items.size() == limit);
        return result;
    }

    @Override
    public CommunicationDetailDto getDetail(Long userId, Long recordId) {
        return communicationStore.getDetail(userId, recordId);
    }

    @Override
    public HistoryItemDto updateFavorite(Long userId, Long recordId, boolean favorite) {
        return communicationStore.updateFavorite(userId, recordId, favorite);
    }

    @Override
    public void deleteRecord(Long userId, Long recordId) {
        communicationStore.delete(userId, recordId);
    }

    @Override
    public TtsResponse synthesize(Long userId, Long recordId, Long variantId, String text) {
        communicationStore.getDetail(userId, recordId);
        TtsResponse response = new TtsResponse();
        response.setAudioUrl(ttsProvider.synthesize(text));
        return response;
    }
}
