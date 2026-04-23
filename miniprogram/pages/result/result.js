const api = require("../../services/api");

Page({
  data: {
    result: null,
    isFavorite: false,
    playingVariantType: "",
    ttsLoadingVariantType: "",
    isLoading: true
  },

  async onLoad(options) {
    this.audioContext = wx.createInnerAudioContext();
    this.audioContext.onEnded(() => {
      this.setData({ playingVariantType: "" });
    });
    this.audioContext.onError(() => {
      this.setData({ playingVariantType: "" });
      wx.showToast({ title: "播放失败", icon: "none" });
    });

    const latestResult = wx.getStorageSync("latestResult");
    if (latestResult && String(latestResult.recordId) === options.recordId) {
      this.setData({ result: this.normalizeResult(latestResult), isFavorite: !!latestResult.favorite, isLoading: false });
      return;
    }

    if (!options.recordId) {
      this.setData({ isLoading: false });
      return;
    }

    try {
      const response = await api.fetchCommunicationDetail(options.recordId);
      this.setData({
        result: this.normalizeResult({
          recordId: response.data.recordId,
          analysis: response.data.analysis,
          variants: response.data.variants
        }),
        isFavorite: !!response.data.favorite,
        isLoading: false
      });
    } catch (error) {
      this.setData({ isLoading: false });
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
    }
  },

  onUnload() {
    if (this.audioContext) {
      this.audioContext.destroy();
    }
  },

  normalizeResult(result) {
    const analysis = result.analysis || {};
    const variants = (result.variants || []).map((variant) => ({
      ...variant,
      audioUrl: variant.audioUrl || ""
    }));

    return {
      ...result,
      variants,
      analysis: {
        ...analysis,
        toneTagsText: (analysis.toneTags || []).join("、"),
        riskPointsText: (analysis.riskPoints || []).join("；")
      }
    };
  },

  onCopy(event) {
    const text = event.currentTarget.dataset.text;
    const variantType = event.currentTarget.dataset.type;
    wx.setClipboardData({ data: text });
    this.submitFeedback("copy", variantType);
  },

  async onPlay(event) {
    const text = event.currentTarget.dataset.text;
    const variantType = event.currentTarget.dataset.type;
    const result = this.data.result;
    if (!result) {
      return;
    }

    if (this.data.playingVariantType === variantType && this.audioContext) {
      this.audioContext.stop();
      this.setData({ playingVariantType: "" });
      return;
    }

    const targetVariant = (result.variants || []).find((item) => item.type === variantType);
    if (!targetVariant) {
      return;
    }

    try {
      let audioUrl = targetVariant.audioUrl;

      if (!audioUrl) {
        this.setData({ ttsLoadingVariantType: variantType });
        const response = await api.synthesizeSpeech(result.recordId, text);
        audioUrl = response.data.audioUrl;
        this.updateVariantAudioUrl(variantType, audioUrl);
      }

      this.audioContext.src = audioUrl;
      this.audioContext.play();
      this.setData({ playingVariantType: variantType });
      this.submitFeedback("play", variantType);
    } catch (error) {
      wx.showToast({ title: getErrorMessage(error), icon: "none", duration: 3000 });
    } finally {
      if (this.data.ttsLoadingVariantType === variantType) {
        this.setData({ ttsLoadingVariantType: "" });
      }
    }
  },

  updateVariantAudioUrl(variantType, audioUrl) {
    const result = this.data.result;
    if (!result) {
      return;
    }

    const nextResult = {
      ...result,
      variants: (result.variants || []).map((item) => {
        if (item.type === variantType) {
          return { ...item, audioUrl };
        }
        return item;
      })
    };

    this.setData({ result: nextResult });
    wx.setStorageSync("latestResult", nextResult);
  },

  async onFavorite() {
    const result = this.data.result;
    if (!result) {
      return;
    }

    const nextFavorite = !this.data.isFavorite;
    try {
      await api.updateFavorite(result.recordId, nextFavorite);
      this.setData({ isFavorite: nextFavorite });
      this.submitFeedback(nextFavorite ? "favorite" : "unfavorite", null);
      wx.showToast({ title: nextFavorite ? "已收藏" : "已取消", icon: "success" });
    } catch (error) {
      wx.showToast({ title: getErrorMessage(error), icon: "none" });
    }
  },

  async submitFeedback(actionType, variantType) {
    const result = this.data.result;
    if (!result) {
      return;
    }

    try {
      await api.submitFeedback({
        recordId: result.recordId,
        actionType,
        variantType
      });
    } catch (error) {
      // Feedback should never block the main action.
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
