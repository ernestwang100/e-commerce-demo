package com.superdupermart.shopping;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.junit.jupiter.api.Test;
import java.io.FileWriter;

public class JasyptEncryptionTest {

    @Test
    public void generateEncryptedValues() throws Exception {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setPassword("commerce");
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        encryptor.setPoolSize(1);

        String dbPassword = "shoppass";
        String jwtSecret = "super-secret-jwt-key-for-online-shopping-app-2026";

        String encDb = encryptor.encrypt(dbPassword);
        String encJwt = encryptor.encrypt(jwtSecret);

        // Write to file to avoid console truncation
        try (FileWriter writer = new FileWriter("target/encrypted_values.txt")) {
            writer.write("DB_PASSWORD_ENC=" + encDb + "\n");
            writer.write("JWT_SECRET_ENC=" + encJwt + "\n");
        }

        System.out.println("Written to target/encrypted_values.txt");
    }
}
