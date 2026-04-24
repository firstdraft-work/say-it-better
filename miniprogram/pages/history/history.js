const api = require("../../services/api");
const PAGE_SIZE = 20;

const SCENE_LABELS = {
  workplace: "职场",
  family: "家庭",
  social: "社交"
};

Page({
  data: {
    list: [],
    groupedList: [],
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
        list: items.map(this.enrichItem),
        hasMore: response.data ? response.data.hasMore : false,
        currentPage: 0,
        isLoading: false
      }, () => {
        this.applyFilters();
      });
    } catch (error) {
      this.setData({ list: [], groupedList: [], isLoading: false });
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
        list: [...this.data.list, ...newItems.map(this.enrichItem)],
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

  enrichItem(item) {
    return {
      ...item,
      sceneLabel: SCENE_LABELS[item.scene] || item.scene
    };
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
      wx.showToast({ title: favorite ? "已收藏" : "已取消", icon: "none", duration: 1000 });
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

    this.setData({ groupedList: this.groupByDate(filteredList) });
  },

  groupByDate(items) {
    const groups = {};
    const today = this.formatDate(new Date());
    const yesterday = this.formatDate(new Date(Date.now() - 86400000));

    items.forEach((item) => {
      const date = item.createdAt ? item.createdAt.split(" ")[0] || item.createdAt.split("T")[0] : "未知日期";
      let displayDate = date;
      if (date === today) displayDate = "今天";
      else if (date === yesterday) displayDate = "昨天";

      if (!groups[displayDate]) {
        groups[displayDate] = { date: displayDate, records: [] };
      }
      groups[displayDate].records.push(item);
    });

    return Object.values(groups);
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
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
