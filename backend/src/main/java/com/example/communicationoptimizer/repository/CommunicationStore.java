package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.VariantDto;

import java.util.List;

public interface CommunicationStore {

    OptimizeResponse saveGenerated(Long userId, String originalText, AnalysisDto analysis, List<VariantDto> variants);

    List<HistoryItemDto> listHistory(Long userId, int page, int limit);

    CommunicationDetailDto getDetail(Long userId, Long recordId);

    HistoryItemDto updateFavorite(Long userId, Long recordId, boolean favorite);

    void delete(Long userId, Long recordId);
}
