package au.org.tso.ldap.navigator;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemaExplorer {
    public SchemaExplorer() {

    }

    void load(LdapConnection connection) throws Exception {
        var logger = LoggerFactory.getLogger(SchemaExplorer.class);

        try {
            connection.loadSchemaRelaxed();

            SchemaManager schemaManager = connection.getSchemaManager();

            schemaManager.setRelaxed();

            logger.info("Schemas Loaderd: " + schemaManager.getAllSchemas().size());

            schemaManager.loadAllEnabled();

            logger.info("Schema Manager PROCESSOL: " + (schemaManager == null));

        } catch (

        Exception e) {
            logger.error("Schema Error", e);
        }

    }

}
