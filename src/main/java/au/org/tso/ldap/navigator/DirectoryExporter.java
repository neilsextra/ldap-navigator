package au.gov.sa.euc.ldap.navigator;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.bouncycastle.asn1.*;
import org.springframework.stereotype.Component;

@Component
public class DirectoryExporter {

    String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }

    public byte[] export(LdapConnection connection, String searchBase) throws Exception {

        try (EntryCursor cursor = connection.search(searchBase, "(objectclass=*)", SearchScope.OBJECT)) {

            for (Entry entry : cursor) {
                ASN1EncodableVector attributeVector = new ASN1EncodableVector();

                for (Attribute attribute : entry.getAttributes()) {
                    String attributeName = attribute.getAttributeType().getName();
                    ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(attribute.getAttributeType().getOid());

                    if (attribute.isHumanReadable()) {
                        attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                new DERUTF8String(attributeName), oid, new DERUTF8String(attribute.getString())
                        }));

                    } else {
                        attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                new DERUTF8String(attributeName), oid, new DEROctetString(attribute.getBytes())
                        }));
                    }

                }

                ASN1Sequence attributeSequence = new DERSequence(attributeVector);

                // Output the ASN.1 encoded data
                attributeSequence.getEncoded();

                return attributeSequence.getEncoded();

            }

            throw new Exception("DN - '" + searchBase + "' - not found");

        }

    }

}
