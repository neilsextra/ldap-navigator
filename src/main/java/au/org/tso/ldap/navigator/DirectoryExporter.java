package au.org.tso.ldap.navigator;

import static au.org.tso.ldap.navigator.util.AttributeUtils.isHumanReadable;

import java.util.Iterator;
import java.util.Map;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.bouncycastle.asn1.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DirectoryExporter
 * 
 * Generates an ASN.1 representation of a directory entry
 */
@Component
public class DirectoryExporter {

    final SchemaExplorer schemaExplorer;

    DirectoryExporter(SchemaExplorer schemaExplorer) {
        this.schemaExplorer = schemaExplorer;
    }

    /**
     * Generates an ASN.1 export
     * 
     * @param connection the LDAP connection
     * @param dn         the distinguished
     * @return an ASN.1 file
     * @throws Exception thrown if there is a problem thrown by the directory
     */
    public byte[] export(LdapConnection connection, String dn) throws Exception {
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = schemaExplorer.load(connection);

        Entry entry = connection.lookup(dn);

        if (entry == null) {
            logger.info("Object: '" + dn + "' not Found");

            throw new Exception("Object: '" + dn + "' not Found");
        }

        ASN1EncodableVector attributeVector = new ASN1EncodableVector();

        for (Attribute attribute : entry.getAttributes()) {

            if (schemaAttributes.containsKey(attribute.getId())) {
                String attributeName = attribute.getUpId();

                Iterator<Value> iterator = attribute.iterator();

                while (iterator.hasNext()) {
                    Value value = iterator.next();

                    if (isHumanReadable(value.getString())) {
                        attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                new DERUTF8String(attributeName),
                                new ASN1ObjectIdentifier(schemaAttributes.get(attribute.getId()).getOid()),
                                new DERUTF8String(value.getString())
                        }));

                    } else {
                        attributeVector.add(new DERSequence(new ASN1Encodable[] {
                                new DERUTF8String(attributeName),
                                new ASN1ObjectIdentifier(schemaAttributes.get(attribute.getId()).getOid()),
                                new DEROctetString(value.getBytes())
                        }));
                    }

                }

            }

        }

        ASN1Sequence attributeSequence = new DERSequence(attributeVector);

        logger.info("Export '" + dn + "' - successful");

        return attributeSequence.getEncoded();

    }

}
