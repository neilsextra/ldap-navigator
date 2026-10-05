package au.org.tso.ldap.navigator;

import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.X509TrustManager;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ConnectionManager
 * 
 * Manages the directory connections
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConnectionManager {

    /**
     * Insecure Trust Manager
     */
    final public class InsecureTrustManager implements X509TrustManager {
        /**
         * Default constructor
         */
        InsecureTrustManager() {
        }

        /**
         * Check the SSL client certificate
         * 
         * @param certs    the certificate array
         * @param authType the authentication type
         */
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // Accept all client certificates
        }

        /**
         * Check the server SSL client certificate
         * 
         * @param certs    the certificate array
         * @param authType the authentication type
         */
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // Accept all server certificates
        }

        /**
         * Return the accepted issues an empty array means all server certficates are
         * acceptable
         * 
         * @return an array of acceptable certificates
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]; // Return empty array
        }
        
    }

    /**
     * Connection Manager constructor
     */
    public ConnectionManager() {
    }

    /**
     * Parse the directory URL
     * 
     * @param url the directory url
     * @return the directory url parts
     * @throws Exception the url does meet the protocol
     */
    HashMap<String, String> parse(String url) throws Exception {
        HashMap<String, String> properties = new HashMap<>();

        String[] parts = url.split("/|:|@");

        if (parts.length == 6) {
            properties.put("protocol", parts[0]);
            properties.put("username", parts[3]);
            properties.put("host", parts[4]);
            properties.put("port", parts[5]);
            properties.put("key", parts[3] + "@" + parts[4] + ":" + parts[5]);
        } else {
            throw new Exception("Invalid URL");
        }

        return properties;

    }

    /**
     * LDAP connection
     * 
     * @param url      the LDAP URL (with host name and port)
     * @param password the users password
     * @return an LDAP connection
     * @throws Exception thrown if there was a problem with the LDAP connection
     */
    LdapConnection connect(String url, String password) throws Exception {
        var logger = LoggerFactory.getLogger(ConnectionManager.class);
        HashMap<String, String> properties = parse(url);
        String key = properties.get("key");

        logger.info("[connection] '{}' creating... ", key);

        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost(properties.get("host"));
        config.setLdapPort(Integer.parseInt(properties.get("port")));
        config.setName(properties.get("username"));
        config.setCredentials(password);

        if (properties.get("protocol").equals("ldaps")) {
             config.setTrustManagers(new InsecureTrustManager());
             config.setUseSsl(true);     
        }

        DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

        factory.setTimeOut(10000);

        return factory.newLdapConnection();

    }

}
