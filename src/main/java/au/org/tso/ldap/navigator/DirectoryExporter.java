package au.org.tso.ldap.navigator;

import java.util.*;


import org.bouncycastle.asn1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DirectoryExporter {

    @Autowired
    Schema schemaManager;

    String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    public byte[] export(LDAPConnection connection, String searchBase) throws Exception {
        String dn;

        LDAPSchema schema = schemaManager.getSchema(connection);
        LDAPSearchResults searchResults = connection.search(searchBase, LDAPConnection.SCOPE_BASE,
                "(objectClass=*)", null, false);

        if (searchResults.hasMore()) {
            LDAPEntry nextEntry = searchResults.next();

            dn = nextEntry.getDN();

            ASN1EncodableVector attributeVector = new ASN1EncodableVector();

            LDAPAttributeSet attributeSet = nextEntry.getAttributeSet();
            Iterator allAttributes = attributeSet.iterator();

            while (allAttributes.hasNext()) {
                LDAPAttribute attribute = (LDAPAttribute) allAttributes.next();
                String attributeName = attribute.getName();

                Properties properties = schemaManager.getAttributeProperties(schema, attributeName);
                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(properties.get("id").toString());

                Enumeration allValues = attribute.getStringValues();

                if (allValues != null) {
                    while (allValues.hasMoreElements()) {
                        String value = (String) allValues.nextElement();
                        if (Base64.isLDIFSafe(value)) {

                            attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                    new DERUTF8String(attributeName), oid, new DERUTF8String(value)
                            }));

                        } else {
                            attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                    new DERUTF8String(attributeName), oid, new DEROctetString(value.getBytes())
                            }));
                        }
                    }
                }

            }

            ASN1Sequence attributeSequence = new DERSequence(attributeVector);

            // Output the ASN.1 encoded data
            attributeSequence.getEncoded();

            return attributeSequence.getEncoded();
        }

        return new byte[0];

    }

}
