module.exports = function request(options) {
  const app = getApp();

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
                  reject(classifyError(retryRes.statusCode, retryRes.data));
                }
              },
              fail(err) {
                reject(classifyNetworkError(err));
              }
            });
          }).catch(() => {
            reject({ type: "auth", message: "登录已过期，请重新打开小程序" });
          });
          return;
        }

        reject(classifyError(res.statusCode, res.data));
      },
      fail(err) {
        reject(classifyNetworkError(err));
      }
    });
  });
};

function classifyError(statusCode, data) {
  const serverMessage = data && data.message ? data.message : "";

  if (statusCode === 429) {
    return { type: "rate_limit", statusCode, message: "操作太频繁，请稍后再试", data };
  }
  if (statusCode >= 500) {
    return { type: "server", statusCode, message: "服务开小差了，请稍后重试", data };
  }
  if (statusCode === 422 && serverMessage) {
    return { type: "validation", statusCode, message: serverMessage, data };
  }
  return { type: "http", statusCode, message: `请求失败（${statusCode}）`, data };
}

function classifyNetworkError(err) {
  const msg = (err && (err.errMsg || "")) || "";

  if (msg.includes("timeout")) {
    return { type: "timeout", message: "请求超时，请稍后重试" };
  }
  if (msg.includes("fail to connect") || msg.includes("ECONNREFUSED")) {
    return { type: "network", message: "连不上服务器，请检查网络设置" };
  }
  return { type: "network", errMsg: msg, message: "网络不给力，请检查网络设置" };
}
