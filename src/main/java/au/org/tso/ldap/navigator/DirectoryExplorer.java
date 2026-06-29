package au.org.tso.ldap.navigator;

import static au.org.tso.ldap.navigator.util.AttributeUtils.bytesToHex;
import static au.org.tso.ldap.navigator.util.AttributeUtils.isHumanReadable;

import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
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

        String getDn();

    };

    final int PAGE_SIZE = 100;

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
     * @param entries    the entry array
     * @param scope      the search scope
     * @return the next cursor position
     * @throws Exception hrown if there is a problem
     */
    void search(LdapConnection connection, final String dn, int limit, Set<String> entries, SearchScope scope)
            throws Exception {
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        final StringBuffer cursorPosition = new StringBuffer();
        var pageSize = PAGE_SIZE;

        logger.info("[Search] '{}' - {} ...", dn, limit);

        try (EntryCursor cursor = connection.search(dn, "(objectclass=*)", scope)) {

            for (Entry entry : cursor) {

                entries.add(entry.getDn().toString());
                pageSize = pageSize - 1;

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        PagedResults pagedControl = new PagedResultsImpl();

        pagedControl.setSize(pageSize);

        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setTimeLimit(10000);
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(scope);
        searchRequest.addControl(pagedControl);
        searchRequest.setSizeLimit(limit);

        int counter = 0;

        try (SearchCursor cursor = connection.search(searchRequest)) {

            while (cursor.next() && counter <= limit) {
                Entry entry = cursor.getEntry();

                if (!entries.contains(entry.getDn().toString())) {
                    entries.add(entry.getDn().toString());
                    System.out.println(entry.getDn().toString());
                    counter += 1;
                }

            }

            logger.info("Capturing Cursor position: " + counter + ":" + limit);

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();
                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    cursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));
                }

            }

            cursor.close();

            logger.info("Entries Size: " + entries.size() + ":" + PAGE_SIZE + " : " + (entries.size() % pageSize == 0));

            if (entries.size() == PAGE_SIZE && entries.size() < limit)
                while (entries.size() <= limit && entries.size() % pageSize == 0) {
                    cursorPosition.setLength(0);
                    
                    cursorPosition.append(next(connection, dn, cursorPosition.toString(), entries, limit, scope));

                    logger.info("NEXT");

                }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        logger.info("Search Completed - Returning Results...");

    }

    /**
     * Get the next set of entries
     * 
     * @param connection     the LDAP connection
     * @param dn             the base name
     * @param cursorPosition the current cursor position
     * @param entries        the entry array
     * @param limit        the maximum nimber of entries returned
     * @param scope        the scope
     * @return the next cursor position
     * @throws Exception hrown if there is a problem
     */
    private String next(LdapConnection connection, String dn, String cursorPosition, Set<String> entries,
            int limit, SearchScope scope) throws Exception {
        final StringBuffer nextCursorPosition = new StringBuffer();

        PagedResultsImpl pageControl = new PagedResultsImpl();
        pageControl.setSize(PAGE_SIZE);
        pageControl.setCookie(Base64.getDecoder().decode(cursorPosition));

        SearchRequestImpl searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(scope);
        searchRequest.addControl(pageControl);
        searchRequest.setSizeLimit(limit);


        try (SearchCursor cursor = connection.search(searchRequest)) {
            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                entries.add(entry.getDn().toString());

                System.out.println("NEXT: " + entry.getDn().toString());
            }

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();

                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    nextCursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));
                }

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return nextCursorPosition.toString();

    }

    /**
     * Search the LDAP directory
     * 
     * @param connection the LDAP connection
     * @param dn         the base name
     * @param limit      the number of results to return
     * @return the search result as a list of directory entries
     * @throws Exception hrown if there is a problem
     */
    SearchResponse search(LdapConnection connection, final String dn, int limit) throws Exception {
        Set<String> entries = new LinkedHashSet<>();

        search(connection, dn, limit, entries, SearchScope.OBJECT);

        if (entries.size() < limit) {
            search(connection, dn, limit, entries, SearchScope.ONELEVEL);
        }

        if (entries.size() < limit) {
            search(connection, dn, limit, entries, SearchScope.SUBTREE);
        }

        return new SearchResponse(entries, dn);

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

                System.out.println("Name: " + attribute.getUpId());

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
