package au.org.tso.ldap.navigator;

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

    static final int NO_CONNECTION = -1;
    static final int NOT_CONNECTED = 0;
    static final int CONNECTED = 1;

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
            properties.put("key", parts[3] + "@" + parts[5] + ":" + parts[6]);
        } else {
            throw new Exception("Invalid URL");
        }

        return properties;

    }

    LdapConnection connect(String url) throws Exception {
        var logger = LoggerFactory.getLogger(ConnectionManager.class);
        HashMap<String, String> properties = parse(url);
        String key = properties.get("key");

        logger.info("Connecting... " + key);

        if (connections.containsKey(key)) {
            logger.info("Found Connection: " + connections.get(key));

            LdapConnection connection = connections.get(key);

            if (connection.isConnected()) {
                return connection;
            }

        }

        logger.info("Create connection... " + key);

        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost(properties.get("host"));
        config.setLdapPort(Integer.parseInt(properties.get("port")));
        config.setName(properties.get("username"));
        config.setCredentials(properties.get("password"));

        DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);

        factory.setTimeOut(10000);

        LdapConnection connection = factory.newLdapConnection();

        connections.put(key, connection);

        System.out.println(connection.toString());

        return connections.get(key);

    }

    void reconnect(String url) throws Exception {
        HashMap<String, String> properties = parse(url);
        var logger = LoggerFactory.getLogger(ConnectionManager.class);
        String key = properties.get("key");

        logger.info("Reconnecting... " + properties.get("username"));

        if (connections.containsKey(key)) {
            logger.info("Found reconnection: " + connections.get(key).toString());

            LdapConnection connection = connections.get(key);

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

        connections.put(key, factory.newLdapConnection());

        logger.info("Reconnection complete: " + properties.get("key"));

    }

    int status(String url) throws Exception {
        var logger = LoggerFactory.getLogger(ConnectionManager.class);

        HashMap<String, String> properties = parse(url);
        String key = properties.get("key");

        logger.info("Connecting... " + key);

        if (connections.containsKey(key)) {
            logger.info("(Status) Found Connection: " + connections.get(key).toString());

            LdapConnection connection = connections.get(key);

            return connection.isConnected() ? CONNECTED : NOT_CONNECTED;

        }

        return NO_CONNECTION;

    }

}
