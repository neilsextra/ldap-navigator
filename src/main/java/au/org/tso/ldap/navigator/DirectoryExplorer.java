package au.org.tso.ldap.navigator;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DirectoryExplorer {

    interface ResultContainer {

        List<String> getResults();

        String getDn();

        String getCursorPosition();
    };

    final int MAX_RESULTS = 100;

    @Autowired
    SchemaExplorer schemaExplorer;

    public DirectoryExplorer() {
    }

    private String bytesToHex(byte[] bytes) {
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

    SearchResponse search(LdapConnection connection, final String dn) throws Exception {
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        List<String> entries = new ArrayList<>();
        final StringBuffer cursorPosition = new StringBuffer();
        var pageSize = MAX_RESULTS;

        logger.info("Primary Search...");

        try (EntryCursor cursor = connection.search(dn, "(objectclass=*)",
                SearchScope.OBJECT)) {

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
        searchRequest.setScope(SearchScope.ONELEVEL);
        searchRequest.addControl(pagedControl);

        try (SearchCursor cursor = connection.search(searchRequest)) {
   
            while (cursor.next()) {
                Entry entry = cursor.getEntry();

                entries.add(entry.getDn().toString());

            }

            logger.info("Capturing Cursor position");

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();
                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    cursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));    
                }

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        logger.info("Search Completed - Returning Results...");

        return new SearchResponse(entries, dn, cursorPosition.toString());

    }

    SearchResponse next(LdapConnection connection, String dn, String cursorPosition) throws Exception {
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        final List<String> entries = new ArrayList<>();
        final StringBuffer nextCursorPosition = new StringBuffer();

        PagedResultsImpl pageControl = new PagedResultsImpl();
        pageControl.setSize(MAX_RESULTS);
        pageControl.setCookie(Base64.getDecoder().decode(cursorPosition));

        SearchRequestImpl searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(dn));
        searchRequest.setFilter("(objectclass=*)");
        searchRequest.setScope(SearchScope.SUBTREE);
        searchRequest.addControl(pageControl);
    
        try (SearchCursor cursor = connection.search(searchRequest)) {
            System.out.println("Cursor returned: " + cursor.isDone());
            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                entries.add(entry.getDn().toString());

                System.out.println(entry.getDn().toString());
            }

            if (cursor.getSearchResultDone().getLdapResult().getResultCode() != ResultCodeEnum.SIZE_LIMIT_EXCEEDED) {

                Map<String, Control> controls = cursor.getSearchResultDone().getControls();

                PagedResults responseControl = (PagedResults) controls.get(PagedResults.OID);

                if (responseControl != null) {
                    nextCursorPosition.append(Base64.getEncoder().encodeToString(responseControl.getCookie()));
                    logger.info("Next: Captured Cursor position: " +  new String(responseControl.getCookie()) + " : " + nextCursorPosition.toString());
                }

            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return new SearchResponse(entries, dn, nextCursorPosition.toString());

    }

    Vector<Map<String, String>> retrieve(LdapConnection connection, String dn) throws Exception {
        Vector<Map<String, String>> attributes = new Vector<Map<String, String>>();
        var logger = LoggerFactory.getLogger(DirectoryExplorer.class);

        Map<String, AttributeType> schemaAttributes = schemaExplorer.load(connection);

        try {
            connection.setTimeOut(10000);
            Entry entry = connection.lookup(dn);

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

                if (isHumanReadable(attribute.get().getString())) {
                    properties.put("type", "String");
                    properties.put("value", attribute.get().getString());

                } else {
                    properties.put("type", "Binary");
                    properties.put("value", bytesToHex(attribute.get().getBytes()));

                }

                attributes.add(properties);

            }

            return attributes;

        } catch (Exception e) {
            logger.error("Search Error", e);
            return attributes;
        }

    }

}
