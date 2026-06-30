package au.org.tso.ldap.navigator;

import static au.org.tso.ldap.navigator.util.AttributeUtils.bytesToHex;
import static au.org.tso.ldap.navigator.util.AttributeUtils.isHumanReadable;

import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DirectoryExplorer
 * 
 * Manages the directory search capability
 */
@Component
public class DirectoryExplorer {

    interface ResultContainer {

        Set<String> getResults();

        String getBase();

        String getFilter();

        SearchScope getScope();

    };

    final SchemaExplorer schemaExplorer;

    /**
     * Directory Explorer
     * 
     * @param schemaExplorer the schema explorer
     */
    public DirectoryExplorer(SchemaExplorer schemaExplorer) {
        this.schemaExplorer = schemaExplorer;
    }

    /**
     * Get the next set of entries
     * 
     * @param connection the LDAP connection
     * @param dn         the base name
     * @param limit      maximum number of entries
     * @param scope      the search scope
     * @return the next cursor position
     * @throws Exception hrown if there is a problem
     */
    SearchResponse search(LdapConnection connection, String base, String filter, SearchScope scope, int limit)
            throws Exception {
        Set<String> entries = new LinkedHashSet<>();

        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        logger.info("[search] (INITIATED) '{}' - '{}' - '{}' - '{}' ...", base, filter, scope.toString(), limit);

        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(base));
        searchRequest.setTimeLimit(10000);

        if (!filter.trim().isEmpty()) {
            searchRequest.setFilter(filter);
        }

        searchRequest.setScope(scope);
        searchRequest.setSizeLimit(limit);

        try (SearchCursor cursor = connection.search(searchRequest)) {

            while (cursor.next()) {
                Entry entry = cursor.getEntry();

                if (!entries.contains(entry.getDn().toString())) {
                    entries.add(entry.getDn().toString());
                }

            }

            cursor.close();

            logger.info("[search] (Completed) '{}' - '{}' - '{}' - '{}' ...", base, filter, scope.toString(),
                    entries.size());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return new SearchResponse(entries, base, filter, scope);

    }

    /**
     * Get a directory entry attributes as vector
     * 
     * @param connection the LDAP connection
     * @param dn         the base name
     * @return the attributes as a vector
     * @throws Exception hrown if there is a problem
     */
    Vector<Map<String, String>> retrieve(LdapConnection connection, String dn) throws Exception {
        Vector<Map<String, String>> attributes = new Vector<Map<String, String>>();
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = schemaExplorer.load(connection);

        try {
            Entry entry = connection.lookup(dn, "*", "+");

            if (entry == null) {
                logger.info("Entry is NULL");

                return attributes;
            }

            for (Attribute attribute : entry.getAttributes()) {
                Map<String, String> properties = new HashMap<String, String>();

                String oid = schemaAttributes.containsKey(attribute.getId())
                        ? schemaAttributes.get(attribute.getId()).getOid()
                        : " ";
                String syntaxOid = schemaAttributes.containsKey(attribute.getId())
                        ? schemaAttributes.get(attribute.getId()).getSyntaxOid()
                        : " ";

                properties.put("name", attribute.getUpId());
                properties.put("oid", oid == null ? "" : oid);
                properties.put("SyntaxOid", syntaxOid == null ? "" : syntaxOid == null ? "" : syntaxOid);
                properties.put("type", isHumanReadable(attribute.get().getString()) ? "String" : "Binary");

                Iterator<Value> iterator = attribute.iterator();

                while (iterator.hasNext()) {
                    Map<String, String> values = new HashMap<String, String>(properties);
                    Value value = iterator.next();

                    if (isHumanReadable(value.getString())) {
                        values.put("type", "String");
                        values.put("value", value.getString());

                    } else {
                        values.put("type", "Binary");
                        values.put("value", bytesToHex(value.getBytes()));

                    }

                    attributes.add(values);

                }

            }

            return attributes;

        } catch (Exception e) {
            logger.error("Search Error", e);
            return attributes;
        }

    }

}
