package cn.yingzhichu.cordova.galipay;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alipay.sdk.app.PayTask;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class GAliPay extends CordovaPlugin {

    private Handler handler;
    private CallbackContext callback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(callback == null){
                    return true;
                }
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                /**
                 * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    PluginResult rs = new PluginResult(PluginResult.Status.OK);
                    rs.setKeepCallback(true);
                    callback.sendPluginResult(rs);
                } else {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    PluginResult rs = new PluginResult(PluginResult.Status.ERROR);
                    rs.setKeepCallback(true);
                    callback.sendPluginResult(rs);
                }
                return true;
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        String orderinfo = args.getString(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(cordova.getActivity());
                Map<String, String> result = alipay.payV2(orderinfo, true);
                Message msg = new Message();
                msg.what = 0;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        }).start();
        return true;
    }

}
