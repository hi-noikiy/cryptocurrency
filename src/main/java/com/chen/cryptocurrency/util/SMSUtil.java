package com.chen.cryptocurrency.util;

import org.springframework.util.Base64Utils;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class SMSUtil {
    public static void sendError() {
        String accessKeyId = "LTAIqwRQP26LV9fA";
        String accessSecret = "pt66ozfQDL3lqsW34iCE2vQ6MUFFv6";
        String phone = "18516198920";
        String signName = "Current服务";
        String templateCode = "SMS_133976724";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        // 这里一定要设置GMT时区
        df.setTimeZone(new SimpleTimeZone(8, "GMT"));
        Map<String, String> paras = new HashMap<>();
        // 1. 系统参数
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", UUID.randomUUID().toString());
        paras.put("AccessKeyId", accessKeyId);
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", df.format(new Date()));
        paras.put("Format", "XML");
        // 2. 业务API参数
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", "cn-hangzhou");
        paras.put("PhoneNumbers", phone);
        paras.put("SignName", signName);
        paras.put("TemplateParam", "{}");
        paras.put("TemplateCode", templateCode);

        // 3. 去除签名关键字Key
        if (paras.containsKey("Signature")) {
            paras.remove("Signature");
        }
        // 4. 参数KEY排序
        TreeMap<String, String> sortParas = new TreeMap<>(paras);
        // 5. 构造待签名的字符串
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();

        try {
            while (it.hasNext()) {
                String key = it.next();
                sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
            }

            // 去除第一个多余的&符号
            String sortedQueryString = sortQueryStringTmp.substring(1);

            String stringToSign = "GET" + "&" +
                    specialUrlEncode("/") + "&" +
                    specialUrlEncode(sortedQueryString);
            String sign = sign(accessSecret + "&", stringToSign);

            // 6. 签名最后也要做特殊URL编码
            String signature = specialUrlEncode(sign);

            // 最终打印出合法GET请求的URL
            String url = "http://dysmsapi.aliyuncs.com/?Signature=" + signature + sortQueryStringTmp;
            HttpUtil.getInstance().requestHttpGet(url);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            MailUtil.sendMail("短信发送失败", "短信发送失败，异常原因:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private static String specialUrlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");

    }

    private static String sign(String accessSecret, String stringToSign) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(accessSecret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return Base64Utils.encodeToString(signData);
    }
}