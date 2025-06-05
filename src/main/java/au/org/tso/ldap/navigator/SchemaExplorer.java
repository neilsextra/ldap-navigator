package au.org.tso.ldap.navigator;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.directory.api.ldap.model.schema.registries.Schema;

@Component
public class SchemaExplorer {

    public SchemaExplorer() {
    }

    Collection<Schema> load(LdapConnection connection) throws Exception {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);

        try {
            DefaultSchemaLoader schemaLoader = new DefaultSchemaLoader(connection, true);

            Collection<Schema> schemas = schemaLoader.getAllSchemas();

            for (Schema schema : schemas) {

                logger.info("Schema: '" + schema.getSchemaName() + "' - loaded");

            }

            return schemas;

        } catch (

        Exception e) {
            logger.error("Schema Loader Error", e);
        }

        return new ArrayList<Schema>();

    }

}
