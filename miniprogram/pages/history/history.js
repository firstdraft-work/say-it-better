const api = require("../../services/api");

Page({
  data: {
    list: [],
    filteredList: [],
    searchKeyword: "",
    activeFilter: "all"
  },

  async onShow() {
    try {
      const response = await api.fetchHistory();
      this.setData({ list: response.data || [] }, () => {
        this.applyFilters();
      });
    } catch (error) {
      wx.showToast({ title: "历史加载失败", icon: "none" });
    }
  },

  onSearchChange(event) {
    this.setData({ searchKeyword: event.detail.value }, () => {
      this.applyFilters();
    });
  },

  onFilterChange(event) {
    this.setData({ activeFilter: event.currentTarget.dataset.filter }, () => {
      this.applyFilters();
    });
  },

  onOpenDetail(event) {
    const recordId = event.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${recordId}` });
  },

  async onToggleFavorite(event) {
    const recordId = event.currentTarget.dataset.id;
    const favorite = !event.currentTarget.dataset.favorite;

    try {
      await api.updateFavorite(recordId, favorite);
      this.setData({
        list: this.data.list.map((item) => {
          if (item.recordId === recordId) {
            return { ...item, favorite };
          }
          return item;
        })
      }, () => {
        this.applyFilters();
      });
    } catch (error) {
      wx.showToast({ title: "收藏失败", icon: "none" });
    }
  },

  applyFilters() {
    const keyword = this.data.searchKeyword.trim().toLowerCase();
    const filteredList = this.data.list.filter((item) => {
      const matchesFavorite = this.data.activeFilter === "favorites" ? item.favorite : true;
      const searchableText = `${item.originalText} ${item.scene} ${item.relation}`.toLowerCase();
      const matchesKeyword = keyword ? searchableText.includes(keyword) : true;
      return matchesFavorite && matchesKeyword;
    });

    this.setData({ filteredList });
  }
});
