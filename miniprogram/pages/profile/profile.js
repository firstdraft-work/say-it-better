const api = require("../../services/api");

Page({
  data: {
    nickName: "",
    totalCount: 0,
    favoriteCount: 0,
    daysUsed: 0
  },

  async onShow() {
    this.loadStats();
    this.loadUserProfile();
  },

  async loadStats() {
    try {
      const response = await api.fetchHistory(0, 1);
      const items = (response.data && response.data.items) || [];
      const favorites = items.filter(item => item.favorite).length;
      this.setData({
        totalCount: response.data ? response.data.total || 0 : 0,
        favoriteCount: favorites
      });
    } catch (error) {
      // Stats are non-critical, silently fail
    }
  },

  loadUserProfile() {
    const userInfo = wx.getStorageSync("userInfo");
    if (userInfo && userInfo.nickName) {
      this.setData({ nickName: userInfo.nickName });
    }

    const firstUseDate = wx.getStorageSync("firstUseDate");
    if (firstUseDate) {
      const days = Math.max(1, Math.ceil((Date.now() - firstUseDate) / 86400000));
      this.setData({ daysUsed: days });
    } else {
      wx.setStorageSync("firstUseDate", Date.now());
      this.setData({ daysUsed: 1 });
    }
  },

  goPrivacy() {
    wx.navigateTo({ url: "/pages/privacy/privacy" });
  }
});
