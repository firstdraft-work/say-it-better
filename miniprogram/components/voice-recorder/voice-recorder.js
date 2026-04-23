Component({
  properties: {
    disabled: {
      type: Boolean,
      value: false
    }
  },

  data: {
    isRecording: false
  },

  lifetimes: {
    attached() {
      this.recorderManager = wx.getRecorderManager();

      this.recorderManager.onStop((result) => {
        this.setData({ isRecording: false });
        this.triggerEvent("recordingchange", { isRecording: false });
        this.triggerEvent("recorded", {
          tempFilePath: result.tempFilePath,
          duration: result.duration
        });
      });

      this.recorderManager.onError((error) => {
        this.setData({ isRecording: false });
        this.triggerEvent("recordingchange", { isRecording: false });
        this.triggerEvent("recorderror", error);
      });
    }
  },

  methods: {
    onRecordTap() {
      if (this.properties.disabled) {
        return;
      }

      if (this.data.isRecording) {
        this.recorderManager.stop();
        return;
      }

      this.setData({ isRecording: true });
      this.triggerEvent("recordingchange", { isRecording: true });
      this.recorderManager.start({
        duration: 60000,
        format: "mp3"
      });
    }
  }
});
