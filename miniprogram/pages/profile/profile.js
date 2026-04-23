Page({
  data: {
    voiceStyle: "natural"
  },

  goPrivacy() {
    wx.navigateTo({ url: "/pages/privacy/privacy" });
  }
});
