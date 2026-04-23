package com.example.communicationoptimizer.dto;

import java.util.List;

public class HistoryPageDto {

    private List<HistoryItemDto> items;
    private int page;
    private int limit;
    private boolean hasMore;

    public List<HistoryItemDto> getItems() {
        return items;
    }

    public void setItems(List<HistoryItemDto> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
