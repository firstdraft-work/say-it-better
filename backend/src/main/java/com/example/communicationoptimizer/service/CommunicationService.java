package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.HistoryPageDto;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.TtsResponse;

import java.util.List;

public interface CommunicationService {

    OptimizeResponse optimize(Long userId, OptimizeRequest request);

    HistoryPageDto listHistory(Long userId, int page, int limit);

    CommunicationDetailDto getDetail(Long userId, Long recordId);

    HistoryItemDto updateFavorite(Long userId, Long recordId, boolean favorite);

    void deleteRecord(Long userId, Long recordId);

    TtsResponse synthesize(Long userId, Long recordId, Long variantId, String text);
}
