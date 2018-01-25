package com.chen.cryptocurrency.util;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class MailUtil {
    public static void main(String[] args) {
        Email email = new EmailBuilder()
                .from("CoinServer", "81773322@qq.com")
                .to("chen", "xiao1tt@163.com")
                .subject("My Bakery is finally open!")
                .text("Mom, Dad. We did the opening ceremony of our bakery!!!")
                .build();


        new Mailer("smtp.qq.com", 587, "81773322@qq.com", "jnheedeochtscbai").sendMail(email);
    }
}