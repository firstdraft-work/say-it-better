const ENVIRONMENTS = {
  local: {
    name: "local",
    apiBaseUrl: "http://127.0.0.1:8080/api/v1"
  },
  lan: {
    name: "lan",
    apiBaseUrl: "http://192.168.31.17:8080/api/v1"
  },
  prod: {
    name: "prod",
    apiBaseUrl: "https://api.mizio.cn/api/v1"
  }
};

// 开发工具模拟器默认用 local；真机联调切 lan；正式联调/体验版切 prod。
const CURRENT_ENV = "prod";

module.exports = {
  currentEnv: ENVIRONMENTS[CURRENT_ENV].name,
  apiBaseUrl: ENVIRONMENTS[CURRENT_ENV].apiBaseUrl,
  environments: ENVIRONMENTS
};
