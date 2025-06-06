package au.org.tso.ldap.navigator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.api.ldap.model.schema.registries.Schema;

@Component
public class SchemaExplorer {

    public SchemaExplorer() {
    }

    Map<String, AttributeType> load(LdapConnection connection) throws Exception {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);

         Map<String, AttributeType> attributes = new HashMap<String, AttributeType>();

        try {
            DefaultSchemaLoader schemaLoader = new DefaultSchemaLoader(connection, true);

            Collection<Schema> schemas = schemaLoader.getAllSchemas();

            for (Schema schema : schemas) {

                logger.info("Schema: '" + schema.getSchemaName() + "' - loaded");

                Set<SchemaObjectWrapper> content = schema.getContent();

                for (var attribute : content) {

                    if (attribute.get() instanceof AttributeType attributeType) {
                
                        attributes.put(attributeType.getName().toLowerCase(), attributeType);
                        
                    }

                }

            }

            return attributes;

        } catch (

        Exception e) {
            logger.error("Schema Loader Error", e);
        }

        return attributes;

    }

}
