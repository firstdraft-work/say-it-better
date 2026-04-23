const api = require("../../services/api");

Page({
  data: {
    inputText: "",
    charCount: 0,
    maxChars: 500,
    selectedScene: "workplace",
    selectedRelation: "",
    selectedGoal: "",
    inputSourceType: "text",
    isSubmitting: false,
    isTranscribing: false,
    apiBaseUrl: "",
    voiceTips: "支持文字输入，也可以先录音转文字。",
    relationOptions: [
      { value: "leader", label: "领导" },
      { value: "colleague", label: "同事" },
      { value: "elder", label: "长辈" },
      { value: "partner", label: "伴侣" },
      { value: "friend", label: "朋友" }
    ],
    goalOptions: [
      { value: "request", label: "请求" },
      { value: "reject", label: "拒绝" },
      { value: "explain", label: "解释" },
      { value: "apologize", label: "道歉" },
      { value: "remind", label: "提醒" }
    ]
  },

  onLoad() {
    const app = getApp();
    this.setData({
      apiBaseUrl: app.globalData.apiBaseUrl
    });
  },

  onInputChange(event) {
    const value = event.detail.value;
    this.setData({
      inputText: value,
      inputSourceType: "text",
      charCount: value.length
    });
  },

  onSelectScene(event) {
    this.setData({ selectedScene: event.currentTarget.dataset.scene });
  },

  onSelectRelation(event) {
    const relation = event.currentTarget.dataset.relation;
    this.setData({
      selectedRelation: this.data.selectedRelation === relation ? "" : relation
    });
  },

  onSelectGoal(event) {
    const goal = event.currentTarget.dataset.goal;
    this.setData({
      selectedGoal: this.data.selectedGoal === goal ? "" : goal
    });
  },

  onRecordingChange(event) {
    const { isRecording } = event.detail;
    this.setData({
      voiceTips: isRecording ? "录音中，再点一次按钮即可结束。" : "录音完成，正在准备转写。"
    });
  },

  async onRecorded(event) {
    const { tempFilePath, duration } = event.detail;
    const fileName = tempFilePath.split("/").pop() || `voice-${Date.now()}.mp3`;

    this.setData({
      isTranscribing: true,
      voiceTips: "录音完成，正在转写..."
    });

    try {
      const uploadResponse = await api.uploadMediaFile(tempFilePath, {
        source: "voice",
        durationMs: duration || 0,
        fileName
      });
      const asrResponse = await api.transcribeSpeech(uploadResponse.data.mediaId);

      this.setData({
        inputText: asrResponse.data.text || "",
        inputSourceType: "voice",
        voiceTips: "语音已转成文字，你可以再微调一下再提交。"
      });
    } catch (error) {
      this.setData({ voiceTips: "语音转写失败，请改用文字输入或重试录音。" });
      wx.showToast({ title: this.getFriendlyErrorMessage(error), icon: "none", duration: 3000 });
    } finally {
      this.setData({ isTranscribing: false });
    }
  },

  onRecordError() {
    this.setData({
      isTranscribing: false,
      voiceTips: "录音失败，请检查麦克风权限后重试。"
    });
    wx.showToast({ title: "录音失败", icon: "none" });
  },

  async onSubmit() {
    if (!this.data.inputText.trim()) {
      wx.showToast({ title: "请输入内容", icon: "none" });
      return;
    }

    this.setData({ isSubmitting: true });
    try {
      const response = await api.optimizeCommunication({
        sourceType: this.data.inputSourceType,
        text: this.data.inputText,
        scene: this.data.selectedScene,
        relation: this.data.selectedRelation || null,
        goal: this.data.selectedGoal || null,
        needTts: false
      });

      wx.setStorageSync("latestResult", response.data);
      wx.navigateTo({ url: `/pages/result/result?recordId=${response.data.recordId}` });
    } catch (error) {
      wx.showToast({ title: this.getFriendlyErrorMessage(error), icon: "none", duration: 3000 });
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  getFriendlyErrorMessage(error) {
    if (!error) return "请求失败，请稍后重试";
    if (error.type === "timeout") return "生成较慢，请稍后重试";
    if (error.type === "network") return "网络不给力，请检查网络设置";
    if (error.type === "server") return "服务开小差了，请稍后重试";
    if (error.type === "auth") return "登录已过期，请重新打开小程序";
    if (error.type === "validation") return error.message || "输入有误";
    if (error.message) return error.message;
    return "请求失败，请稍后重试";
  }
});
