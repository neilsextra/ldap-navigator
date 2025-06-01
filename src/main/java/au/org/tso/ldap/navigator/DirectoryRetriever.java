package au.org.tso.ldap.navigator;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DirectoryRetriever {

    @Autowired
    DirectoryConnector connector;

    @Autowired
    SchemaBrowser schemaBrowser;

    String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    public Vector<Vector<String>> retrieve(LDAPConnection connection, String searchBase)
            throws Exception {
        LDAPSchema schema = schemaManager.getSchema(connection);
        String dn;

        Vector<Vector<String>> entries = new Vector<Vector<String>>();

        LDAPSearchResults searchResults = connection.search(searchBase, LDAPConnection.SCOPE_BASE,
                "(objectClass=*)", null, false);

        if (searchResults.hasMore()) {
            LDAPEntry nextEntry = searchResults.next();

            dn = nextEntry.getDN();

            LDAPAttributeSet attributeSet = nextEntry.getAttributeSet();
            Iterator allAttributes = attributeSet.iterator();

            while (allAttributes.hasNext()) {
                LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();
                String attributeName = attribute.getName();

                System.out.println("attributeName: " + attributeName);

                Properties properties = schemaManager.getAttributeProperties(schema, attributeName);

                String oidValue = "";

                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(properties.getProperty("id"));

                oidValue = oid.toString();

                String syntaxString = properties.getProperty("syntaxString");

                Enumeration allValues = attribute.getStringValues();

                if (allValues != null) {

                    String type = "String";
                    String attributeValue = "";

                    while (allValues.hasMoreElements()) {
                        String value = (String) allValues.nextElement();

                        if (Base64.isLDIFSafe(value)) {
                            type = "String";
                            attributeValue = value;

                        } else {
                            type = "Hex";
                            attributeValue = bytesToHex(value.getBytes());
                        }

                        entries.add(new Vector<String>(Arrays.asList(attributeName, oidValue, syntaxString, type, attributeValue)));

                    }

                }

            }

        }

        return entries;

    }

}
