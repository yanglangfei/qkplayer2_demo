package com.qukan.playsdk.sample.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.qukan.playsdk.sample.R;
import com.qukan.playsdk.sample.utils.PayResult;
import com.qukan.playsdk.sample.utils.SignUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by jucaipen on 16/6/22.
 */
public class PayActivity extends Activity {
    // 商户PID
    public static final String PARTNER = "2088021811620333";
    // 商户收款账号
    public static final String SELLER = "pay@jucaipen.com";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKhuOcmXuRCWs8kN"
            + "pIDAM89V4GuPA+9CUJ/pb3TOtD+0NXGd5Gha/9aRoQJZWpV/bksZDoEPSP2FohLR"
            + "eFZg9OGVNPDuQ/1UMRBTWtILwc64DmrkDHQvSxk3AOaHnC9GdC6WUDERdivu2IeJ"
            + "Br4kCFXEDGtJUJdLgYtyyFfd7C/NAgMBAAECgYBCiEgLegOAqgcaxGjW8xWEMUgV"
            + "NMk5Gdj/UhbU7VIGWAlEeDphfpDREXWol+NQaqYGxD8WfBM5EjCTfmsqs+IHFcb0"
            + "F6oKzUd33y7xVL9EuWzmBCJqr+nTX7+xFSp0bA7KPgStlL30L9mKIRkxc1o8Wwl9"
            + "U5enPzZ57yfQyqoXgQJBANpkLNDMzLfTY+5qnkm44TGESpSYOL/K+cwWTc11q8lF"
            + "kE44h1c90GMXPFvFVlSIu6ihH9a86DSOhbgqMPM7Ec8CQQDFb4V5QGve9D95H4E/"
            + "0FmA/F3ZBaXlA2mvtjews40TdZjGhVpdtKFXuSAxpx2jttLbH722Fmy6QUA29h90"
            + "tdejAkEA0N/IwTdvgU3MISaWF9qcf3iQwEhK1p0yhxC5/w1RbSJ8X2PPhhBX874w"
            + "e3FlHVrdaO4Z/o8U49eAa2DnCmtvnwJAQC4YfWnrpvZPC9ip80yPS9a9yyaC3wdU"
            + "+8GIKPC1E3ZOy3nd/kgRiqqrRHjGsclkkcanrOFtRr6fWT54yz+xcQJAE3BXSr3F"
            + "0mrRFZBRj68PsHSbkAEudeKwY3Eq2RqJ2Qi8PLIfw75kDUvhAnRSLsH6+6E0KtuB"
            + "tiF8wa4Hj1qxtw==";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCobjnJl7kQlrPJDaSAwDPPVeBr"
            + "jwPvQlCf6W90zrQ/tDVxneRoWv/WkaECWVqVf25LGQ6BD0j9haIS0XhWYPThlTTw"
            + "7kP9VDEQU1rSC8HOuA5q5Ax0L0sZNwDmh5wvRnQullAxEXYr7tiHiQa+JAhVxAxr"
            + "SVCXS4GLcshX3ewvzQIDAQAB";
    private static final int SDK_PAY_FLAG = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(PayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(PayActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(PayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_pay);
        initView();

    }

    private void initView() {
        findViewById(R.id.aplay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //支付宝支付
                pay();
            }
        });
        findViewById(R.id.bankPay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //银行卡支付


            }
        });

    }




        /*
        调用SDK支付
        */
        public void pay() {
            if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
                new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                //
                                finish();
                            }
                        }).show();
                return;
            }
            String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", "0.01");

            /**
             * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
             */
            String sign = sign(orderInfo);
            try {
                /**
                 * 仅需对sign 做URL编码
                 */
                sign = URLEncoder.encode(sign, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            /**
             * 完整的符合支付宝参数规范的订单信息
             */
            final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    // 构造PayTask 对象
                    PayTask alipay = new PayTask(PayActivity.this);
                    // 调用支付接口，获取支付结果
                    String result = alipay.pay(payInfo, true);

                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            };

            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
        }

    /**
     * create the order info. 创建订单信息
     *
     */
    private String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm" + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     *
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content
     *            待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }





}
