package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.adapter.llm.LlmProvider;
import com.example.communicationoptimizer.adapter.tts.TtsProvider;
import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
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
    public OptimizeResponse optimize(OptimizeRequest request) {
        OptimizeResponse generated = llmProvider.generate(request);
        return communicationStore.saveGenerated(
                request.getText() != null ? request.getText() : "",
                generated.getAnalysis(),
                generated.getVariants()
        );
    }

    @Override
    public List<HistoryItemDto> listHistory() {
        return communicationStore.listHistory();
    }

    @Override
    public CommunicationDetailDto getDetail(Long recordId) {
        return communicationStore.getDetail(recordId);
    }

    @Override
    public HistoryItemDto updateFavorite(Long recordId, boolean favorite) {
        return communicationStore.updateFavorite(recordId, favorite);
    }

    @Override
    public void deleteRecord(Long recordId) {
        communicationStore.delete(recordId);
    }

    @Override
    public TtsResponse synthesize(Long recordId, Long variantId, String text) {
        communicationStore.getDetail(recordId);
        TtsResponse response = new TtsResponse();
        response.setAudioUrl(ttsProvider.synthesize(text));
        return response;
    }
}
