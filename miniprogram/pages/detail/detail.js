const api = require("../../services/api");

Page({
  data: {
    detail: null
  },

  async onLoad(options) {
    try {
      const response = await api.fetchCommunicationDetail(options.id);
      this.setData({ detail: this.normalizeDetail(response.data) });
    } catch (error) {
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

  async onDelete() {
    const detail = this.data.detail;
    if (!detail) {
      return;
    }

    try {
      await api.deleteCommunication(detail.recordId);
      wx.showToast({ title: "已删除", icon: "success" });
      setTimeout(() => {
        wx.navigateBack();
      }, 400);
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" });
    }
  }
});
