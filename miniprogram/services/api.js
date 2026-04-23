const request = require("../utils/request");

function optimizeCommunication(payload) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications/optimize`,
    method: "POST",
    data: payload,
    timeout: 60000
  });
}

function fetchHistory(page, limit) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications?page=${page}&limit=${limit}`,
    method: "GET"
  });
}

function uploadMedia(payload) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/media/upload`,
    method: "POST",
    data: payload,
    timeout: 15000
  });
}

function uploadMediaFile(tempFilePath, formData) {
  const app = getApp();

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${app.globalData.apiBaseUrl}/media/upload-file`,
      filePath: tempFilePath,
      name: "file",
      formData,
      timeout: 10000,
      success(res) {
        try {
          const data = JSON.parse(res.data);
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(data);
            return;
          }
          reject({
            type: "http",
            statusCode: res.statusCode,
            data,
            message: `HTTP ${res.statusCode}`
          });
        } catch (error) {
          reject({
            type: "parse",
            message: "upload response parse failed"
          });
        }
      },
      fail(err) {
        reject({
          type: "network",
          errMsg: err.errMsg || "",
          message: err.errMsg || "upload failed"
        });
      }
    });
  });
}

function transcribeSpeech(mediaId) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/speech/asr`,
    method: "POST",
    data: { mediaId },
    timeout: 30000
  });
}

function fetchCommunicationDetail(recordId) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications/${recordId}`,
    method: "GET",
    timeout: 15000
  });
}

function synthesizeSpeech(recordId, text) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications/${recordId}/tts`,
    method: "POST",
    data: { text },
    timeout: 30000
  });
}

function updateFavorite(recordId, favorite) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications/${recordId}/favorite`,
    method: "PATCH",
    data: { favorite },
    timeout: 10000
  });
}

function deleteCommunication(recordId) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/communications/${recordId}`,
    method: "DELETE",
    timeout: 10000
  });
}

function wxLogin(code) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/auth/wx/login`,
    method: "POST",
    data: { code },
    timeout: 10000
  });
}

function submitFeedback(payload) {
  const app = getApp();
  return request({
    url: `${app.globalData.apiBaseUrl}/feedback`,
    method: "POST",
    data: payload,
    timeout: 10000
  });
}

module.exports = {
  wxLogin,
  optimizeCommunication,
  fetchHistory,
  uploadMedia,
  uploadMediaFile,
  transcribeSpeech,
  fetchCommunicationDetail,
  synthesizeSpeech,
  updateFavorite,
  deleteCommunication,
  submitFeedback
};
