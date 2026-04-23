package com.example.communicationoptimizer.dto;

import java.util.List;

public class ProviderInfoDto {

    private String storageMode;
    private String selectedLlm;
    private String selectedAsr;
    private String selectedTts;
    private List<String> availableLlms;
    private List<String> availableAsrs;
    private List<String> availableTts;

    public String getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(String storageMode) {
        this.storageMode = storageMode;
    }

    public String getSelectedLlm() {
        return selectedLlm;
    }

    public void setSelectedLlm(String selectedLlm) {
        this.selectedLlm = selectedLlm;
    }

    public String getSelectedAsr() {
        return selectedAsr;
    }

    public void setSelectedAsr(String selectedAsr) {
        this.selectedAsr = selectedAsr;
    }

    public String getSelectedTts() {
        return selectedTts;
    }

    public void setSelectedTts(String selectedTts) {
        this.selectedTts = selectedTts;
    }

    public List<String> getAvailableLlms() {
        return availableLlms;
    }

    public void setAvailableLlms(List<String> availableLlms) {
        this.availableLlms = availableLlms;
    }

    public List<String> getAvailableAsrs() {
        return availableAsrs;
    }

    public void setAvailableAsrs(List<String> availableAsrs) {
        this.availableAsrs = availableAsrs;
    }

    public List<String> getAvailableTts() {
        return availableTts;
    }

    public void setAvailableTts(List<String> availableTts) {
        this.availableTts = availableTts;
    }
}
