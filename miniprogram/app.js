const env = require("./config/env");

App({
  globalData: {
    envName: env.currentEnv,
    apiBaseUrl: env.apiBaseUrl
  }
});
