package au.org.tso.ldap.navigator;

import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DirectoryExplorer {

    @Autowired
    SchemaExplorer schemaExplorer;

    public DirectoryExplorer() {
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    boolean isHumanReadable(String value) {
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        boolean matchFound = matcher.find();
        
        return (!matchFound);
    
    }

    Vector<String> search(LdapConnection connection, String dn) throws Exception {
        Vector<String> entries = new Vector<String>();

        try (EntryCursor cursor = connection.search(dn, "(objectclass=*)", SearchScope.OBJECT)) {

            for (Entry entry : cursor) {

                entries.add(entry.getDn().toString());

            }

        }

        try (EntryCursor cursor = connection.search(dn, "(objectclass=*)", SearchScope.ONELEVEL)) {

            for (Entry entry : cursor) {

                entries.add(entry.getDn().toString());

            }

        }

        return entries;

    }

    Vector<Map<String, String>> retrieve(LdapConnection connection, String dn) throws Exception {
        Vector<Map<String, String>> attributes = new Vector<Map<String, String>>();
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = schemaExplorer.load(connection);

        try {
            Entry entry = connection.lookup(dn);

            if (entry == null) {
                logger.info("Entry is NULL");

                return attributes;
            }

            for (Attribute attribute : entry.getAttributes()) {
                Map<String, String> properties = new HashMap<>();

                String oid =  schemaAttributes.containsKey(attribute.getId()) ? schemaAttributes.get(attribute.getId()).getOid() :" ";
                String syntaxOid =  schemaAttributes.containsKey(attribute.getId()) ? schemaAttributes.get(attribute.getId()).getSyntaxOid() : " ";

                properties.put("name", attribute.getUpId());
                properties.put("oid", oid == null ? "" : oid);
                properties.put("syntaxOid", syntaxOid == null ? "" : syntaxOid);
                properties.put("type", attribute.get().isHumanReadable() ? "String" : "Binary");

                if (isHumanReadable(attribute.get().getString())) {
                    properties.put("type", "String");
                    properties.put("value", attribute.get().getString());
      
                } else {
                    properties.put("type", "Binary");
                    properties.put("value", bytesToHex(attribute.get().getBytes()));
               
                }
   
                attributes.add(properties);

            }

            return attributes;

        } catch (Exception e) {
            logger.error("Search Error", e);
            return attributes;
        }

    }

}
