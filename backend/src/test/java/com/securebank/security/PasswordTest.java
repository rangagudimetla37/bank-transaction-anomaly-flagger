package com.securebank.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {

    @Test
    public void testHashMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String storedHashSB1 = "$2a$12$Z0neupNIKD2lSOxql04JxOABjZv1CHlCdwx34xzr7WWVmCEkAGCWm";
        String storedHashREV = "$2a$12$DwUMWulIkMEMucmRZEQMdODXtcsuZwuUD2RUc0alvc0bB7an1OX76";

        boolean matchDemo = encoder.matches("Demo@12345", storedHashSB1);
        System.out.println(">>> SB100001 with Demo@12345 match: " + matchDemo);

        boolean matchRev = encoder.matches("Review@12345", storedHashREV);
        System.out.println(">>> REV100001 with Review@12345 match: " + matchRev);

        assertTrue(matchDemo, "Demo@12345 must match SB100001 hash");
        assertTrue(matchRev, "Review@12345 must match REV100001 hash");
    }
}
