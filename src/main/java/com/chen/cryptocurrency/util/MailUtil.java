package com.chen.cryptocurrency.util;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class MailUtil {
    public static void sendMail(String subject, String text) {
        String username = "xiao1tt";
        String mailSuf = "@126.com";
        String password = "W0SILENT";

        Email email = new EmailBuilder()
                .from("CoinServer", username + mailSuf)
                .to("81773322@qq.com", "xiao1tt@hotmail.com")
                .cc("xiao1tt@126.com")
                .subject(subject)
                .text(text)
                .build();

        Mailer mailer = new Mailer("smtp.126.com", 25, username + mailSuf, password);
        mailer.trustAllSSLHosts(true);
        mailer.sendMail(email);
    }
}