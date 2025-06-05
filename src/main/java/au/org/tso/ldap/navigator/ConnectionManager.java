package au.org.tso.ldap.navigator;

import java.util.HashMap;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.springframework.stereotype.Component;

@Component
public class ConnectionManager {
    
    public ConnectionManager() throws Exception {	
    }

    HashMap<String, String> parse(String url) throws Exception{
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
         HashMap<String, String> properties = parse(url);

        LdapConnectionConfig config = new LdapConnectionConfig();

        config.setLdapHost(properties.get("host"));
        config.setLdapPort( Integer.parseInt(properties.get("port" )));
        config.setName(properties.get("username" ));
        config.setCredentials(properties.get("password") );

         DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory( config );
        
         factory.setTimeOut( 10000 );

        return factory.newLdapConnection();

    }

}
