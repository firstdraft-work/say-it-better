module.exports = function request(options) {
  const app = getApp();

  // Read token from globalData or fallback to storage
  const token = app.globalData.token || wx.getStorageSync("token");
  if (token) {
    options.header = options.header || {};
    options.header["Authorization"] = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      timeout: options.timeout || 8000,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
          return;
        }

        if (res.statusCode === 401) {
          // Token expired or invalid, re-login and retry once
          app.login().then(() => {
            const newToken = app.globalData.token;
            options.header = options.header || {};
            options.header["Authorization"] = `Bearer ${newToken}`;
            wx.request({
              ...options,
              timeout: options.timeout || 8000,
              success(retryRes) {
                if (retryRes.statusCode >= 200 && retryRes.statusCode < 300) {
                  resolve(retryRes.data);
                } else {
                  reject({
                    type: "http",
                    statusCode: retryRes.statusCode,
                    data: retryRes.data,
                    message: `HTTP ${retryRes.statusCode}`
                  });
                }
              },
              fail(err) {
                reject({
                  type: "network",
                  errMsg: err.errMsg || "",
                  message: err.errMsg || "network error"
                });
              }
            });
          }).catch(() => {
            reject({
              type: "auth",
              message: "登录已过期，请重新打开小程序"
            });
          });
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
