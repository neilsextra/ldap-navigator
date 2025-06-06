package au.org.tso.ldap.navigator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.bouncycastle.asn1.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DirectoryExporter {

    @Autowired
    SchemaExplorer schemaExplorer;

    String bytesToHex(byte[] bytes) {
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

    public byte[] export(LdapConnection connection, String dn) throws Exception {
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = schemaExplorer.load(connection);

        Entry entry = connection.lookup(dn);

        if (entry == null) {
            logger.info("Object: '" + dn + "' not Found);

            throw new Exception("Object: '" + dn + "' not Found");
        }

        ASN1EncodableVector attributeVector = new ASN1EncodableVector();

        for (Attribute attribute : entry.getAttributes()) {

            if (schemaAttributes.containsKey(attribute.getId())) {
                String attributeName = attribute.getUpId();

                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(schemaAttributes.get(attribute.getId()).getOid());

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

        }

        ASN1Sequence attributeSequence = new DERSequence(attributeVector);

        logger.info("Export '" + dn + "' - successful");
        
        return attributeSequence.getEncoded();

    }

}
