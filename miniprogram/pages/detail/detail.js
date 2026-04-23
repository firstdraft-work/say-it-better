const api = require("../../services/api");

Page({
  data: {
    detail: null,
    isLoading: true
  },

  async onLoad(options) {
    try {
      const response = await api.fetchCommunicationDetail(options.id);
      this.setData({ detail: this.normalizeDetail(response.data), isLoading: false });
    } catch (error) {
      this.setData({ isLoading: false });
      wx.showToast({ title: "详情加载失败", icon: "none" });
    }
  },

  normalizeDetail(detail) {
    const analysis = detail.analysis || {};
    return {
      ...detail,
      analysis: {
        ...analysis,
        toneTagsText: (analysis.toneTags || []).join("、"),
        riskPointsText: (analysis.riskPoints || []).join("；")
      }
    };
  },

  onDelete() {
    const detail = this.data.detail;
    if (!detail) {
      return;
    }

    wx.showModal({
      title: "确认删除",
      content: "删除后无法恢复，确定要删除这条记录吗？",
      confirmColor: "#B85C38",
      success: (res) => {
        if (res.confirm) {
          this.doDelete();
        }
      }
    });
  },

  async doDelete() {
    try {
      await api.deleteCommunication(this.data.detail.recordId);
      wx.showToast({ title: "已删除", icon: "success" });
      setTimeout(() => {
        wx.navigateBack();
      }, 400);
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" });
    }
  }
});
