module.exports = function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      timeout: options.timeout || 8000,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
          return;
        }
        reject({
          type: "http",
          statusCode: res.statusCode,
          data: res.data,
          message: `HTTP ${res.statusCode}`
        });
      },
      fail(err) {
        reject({
          type: "network",
          errMsg: err.errMsg || "",
          message: err.errMsg || "network error"
        });
      }
    });
  });
};
