package com.example.puriqtours.helper;

import java.security.SecureRandom;
import java.util.UUID;

public class TokenUtil {

    public static String generarToken() {
        SecureRandom random = new SecureRandom();

        // UUID base
        String uuid = UUID.randomUUID().toString();

        // Número aleatorio extra
        int randomNumber = random.nextInt(999999);

        return uuid + "-" + randomNumber;
    }
}

