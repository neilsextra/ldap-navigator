package au.org.tso.ldap.navigator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AttributeUtils
 * 
 * Helper functions to decode directory attributes
 */

final public class AttributeUtils {
    
    /**
     * Convert a binary value to hex
     * @param bytes the binary to convert 
     * @return a hexadecimal representation of the binary value
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    /**
     * Determine if an attribute value is human readable (not binary)
     * @param value the value
     * @return 'true' human readable, 'false' otherwise
     */
    public static boolean isHumanReadable(String value) {
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();

        return (!matchFound);

    }

}
