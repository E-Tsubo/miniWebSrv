package minwebsrv.servletimpl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

class SessionIdGenerator {
    private SecureRandom random;

    public String generateSessionId() {
        byte[] bytes = new byte[16];
        this.random.nextBytes(bytes);
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            buffer.append(Integer.toHexString(bytes[i] & 0xff).toUpperCase());
        }
        return buffer.toString();
    }

    SessionIdGenerator() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
