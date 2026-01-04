package com.superdupermart.shopping.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptUtil {
    public static void main(String[] args) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("commerce");
        encryptor.setAlgorithm("PBEWithMD5AndDES");

        String dbPassword = "StrongPass123!";
        String jwtSecret = "super-secret-jwt-key-for-online-shopping-app-2026-antigravity";

        System.out.println("New DB Password ENC: " + encryptor.encrypt(dbPassword));
        System.out.println("New JWT Secret ENC: " + encryptor.encrypt(jwtSecret));
    }
}
