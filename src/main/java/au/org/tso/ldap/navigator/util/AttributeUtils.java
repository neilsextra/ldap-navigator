package au.org.tso.ldap.navigator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class AttributeUtils {
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    public static boolean isHumanReadable(String value) {
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();

        return (!matchFound);

    }

}
