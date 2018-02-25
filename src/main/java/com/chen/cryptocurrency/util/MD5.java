package com.chen.cryptocurrency.util;

import java.security.MessageDigest;

public class MD5 {
    public static String encrypt(String source) {

        StringBuilder sb = new StringBuilder(32);

        try {
            MessageDigest md    = MessageDigest.getInstance("MD5");
            byte[] array        = md.digest(source.getBytes("utf-8"));

            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).toUpperCase().substring(1, 3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
