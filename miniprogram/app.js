const env = require("./config/env");

App({
  globalData: {
    envName: env.currentEnv,
    apiBaseUrl: env.apiBaseUrl,
    token: null,
    userInfo: null
  },

  onLaunch() {
    this.login();
  },

  login() {
    const app = this;
    return new Promise((resolve, reject) => {
      wx.login({
        success(loginRes) {
          if (loginRes.code) {
            wx.request({
              url: `${app.globalData.apiBaseUrl}/auth/wx/login`,
              method: "POST",
              data: { code: loginRes.code },
              timeout: 10000,
              success(res) {
                if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.data) {
                  const data = res.data.data;
                  app.globalData.token = data.token;
                  app.globalData.userInfo = data.userInfo;
                  wx.setStorageSync("token", data.token);
                  resolve(data);
                } else {
                  reject(new Error("登录失败"));
                }
              },
              fail() {
                reject(new Error("登录请求失败"));
              }
            });
          } else {
            reject(new Error("wx.login 失败"));
          }
        },
        fail() {
          reject(new Error("wx.login 调用失败"));
        }
      });
    });
  }
});
