package app.dearobjet.backend.global.sms.util;

import java.security.SecureRandom;

public class VerificationCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        int number = random.nextInt(900000) + 100000; // 6자리
        return String.valueOf(number);
    }
}

