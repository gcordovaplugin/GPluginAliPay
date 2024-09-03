var exec = require("cordova/exec");

exports.alipay = function (orderinfo, success, error) {
  exec(success, error, "GAliPay", "", [orderinfo]);
};
