package at.ac.tuwien.sepr.groupphase.backend.config;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class Constants {

    //for testing:
    public static final String AUTH_CODE = "123456";


    /*
    public static final String AUTH_CODE = new SecureRandom()
        .ints(6, 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length())
        .mapToObj(i -> String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(i)))
        .collect(Collectors.joining());
     */

}
