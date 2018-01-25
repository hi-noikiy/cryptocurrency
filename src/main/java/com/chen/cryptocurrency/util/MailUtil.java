package com.chen.cryptocurrency.util;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class MailUtil {
    public static void sendMail(String subject,String text) {
        Email email = new EmailBuilder()
                .from("CoinServer", "81773322@qq.com")
                .to("chen", "xiao1tt@hotmail.com")
                .subject(subject)
                .text(text)
                .build();


        new Mailer("smtp.qq.com", 587, "81773322@qq.com", "jnheedeochtscbai").sendMail(email);
    }
}