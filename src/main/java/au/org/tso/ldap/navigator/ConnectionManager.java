package au.gov.sa.euc.ldap.navigator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConnectionManager {

    static final Map<String, LdapConnection> connections = new LinkedHashMap<>();

    public ConnectionManager() throws Exception {
    }

    HashMap<String, String> parse(String url) throws Exception {
        HashMap<String, String> properties = new HashMap<>();

        String[] parts = url.split("/|:|@");

        if (parts.length == 7) {
            properties.put("protocol", parts[0]);
            properties.put("username", parts[3]);
            properties.put("password", parts[4]);
            properties.put("host", parts[5]);
            properties.put("port", parts[6]);
        } else {
            throw new Exception("Invalid URL");
        }

        return properties;

    }

    LdapConnection connect(String url) throws Exception {
        var logger = LoggerFactory.getLogger(ConnectionManager.class);
        HashMap<String, String> properties = parse(url);
        String username = properties.get("username");

        logger.info("Connecting... " + username);

        if (connections.containsKey(username)) {
            logger.info("Found Connection: " + connections.get(username));

            LdapConnection connection = connections.get(username);

            if (connection.isConnected()) {
                return connection;
            }

        }

        logger.info("Create connection... " + username);

        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost(properties.get("host"));
        config.setLdapPort(Integer.parseInt(properties.get("port")));
        config.setName(properties.get("username"));
        config.setCredentials(properties.get("password"));

        DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

        factory.setTimeOut(10000);

        LdapConnection connection = factory.newLdapConnection();

        connections.put(username, connection);

        System.out.println(connection.toString());

        return connections.get(username);

    }

    void reconnect(String url) throws Exception {
        HashMap<String, String> properties = parse(url);
        var logger = LoggerFactory.getLogger(ConnectionManager.class);
        String username = properties.get("username");

        logger.info("Reconnecting... " + properties.get("username"));

        if (connections.containsKey(username)) {
            logger.info("Found reconnection: " + connections.get(username).toString());

            LdapConnection connection = connections.get(username);

            logger.info("Found Reconnection: " + connection);

            if (connection.isConnected()) {

                connection.close();
            }

        }

        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost(properties.get("host"));
        config.setLdapPort(Integer.parseInt(properties.get("port")));
        config.setName(properties.get("username"));
        config.setCredentials(properties.get("password"));

        DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

        factory.setTimeOut(10000);

        connections.put(username, factory.newLdapConnection());

        logger.info("Reconnection complete: " + properties.get("username"));

    }

}
