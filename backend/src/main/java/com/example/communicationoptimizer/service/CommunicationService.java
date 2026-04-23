package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.TtsResponse;

import java.util.List;

public interface CommunicationService {

    OptimizeResponse optimize(OptimizeRequest request);

    List<HistoryItemDto> listHistory();

    CommunicationDetailDto getDetail(Long recordId);

    HistoryItemDto updateFavorite(Long recordId, boolean favorite);

    void deleteRecord(Long recordId);

    TtsResponse synthesize(Long recordId, Long variantId, String text);
}
