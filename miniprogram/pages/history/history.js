const api = require("../../services/api");
const PAGE_SIZE = 20;

Page({
  data: {
    list: [],
    filteredList: [],
    searchKeyword: "",
    activeFilter: "all",
    isLoading: true,
    isLoadingMore: false,
    hasMore: true,
    currentPage: 0
  },

  async onShow() {
    await this.refreshList();
  },

  onPullDownRefresh() {
    this.refreshList().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.isLoadingMore) {
      this.loadMore();
    }
  },

  async refreshList() {
    this.setData({ isLoading: true, currentPage: 0, hasMore: true });
    try {
      const response = await api.fetchHistory(0, PAGE_SIZE);
      const items = (response.data && response.data.items) || [];
      this.setData({
        list: items,
        hasMore: response.data ? response.data.hasMore : false,
        currentPage: 0,
        isLoading: false
      }, () => {
        this.applyFilters();
      });
    } catch (error) {
      this.setData({ list: [], filteredList: [], isLoading: false });
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
    }
  },

  async loadMore() {
    const nextPage = this.data.currentPage + 1;
    this.setData({ isLoadingMore: true });
    try {
      const response = await api.fetchHistory(nextPage, PAGE_SIZE);
      const newItems = (response.data && response.data.items) || [];
      this.setData({
        list: [...this.data.list, ...newItems],
        hasMore: response.data ? response.data.hasMore : false,
        currentPage: nextPage,
        isLoadingMore: false
      }, () => {
        this.applyFilters();
      });
    } catch (error) {
      this.setData({ isLoadingMore: false });
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
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
      wx.showToast({ title: getErrorMessage(error), icon: "none" });
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

function getErrorMessage(error) {
  if (!error) return "请求失败，请稍后重试";
  if (error.type === "network") return "网络不给力，请检查网络设置";
  if (error.type === "auth") return "登录已过期，请重新打开小程序";
  if (error.statusCode === 500 || (error.data && error.data.message && error.data.message.includes("500"))) return "服务开小差了，请稍后重试";
  if (error.statusCode) return `请求失败（${error.statusCode}）`;
  const msg = error.message || error.errMsg || "";
  if (msg.includes("timeout")) return "请求超时，请稍后重试";
  return "请求失败，请稍后重试";
}
