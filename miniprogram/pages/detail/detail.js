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
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
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
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
    }
  }
});

function getErrorMessage(error) {
  if (!error) return "请求失败，请稍后重试";
  if (error.type === "timeout") return "请求超时，请稍后重试";
  if (error.type === "network") return "网络不给力，请检查网络设置";
  if (error.type === "server") return "服务开小差了，请稍后重试";
  if (error.type === "auth") return "登录已过期，请重新打开小程序";
  if (error.message) return error.message;
  return "请求失败，请稍后重试";
}
