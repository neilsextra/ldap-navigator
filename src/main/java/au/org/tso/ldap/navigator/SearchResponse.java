package au.org.tso.ldap.navigator;

import java.util.Set;

import org.apache.directory.api.ldap.model.message.SearchScope;

/**
 * Search Response
 * 
 * container of a list of distinguished names as a result of a search
 * 
 */
class SearchResponse implements DirectoryExplorer.ResultContainer {
    Set<String> results;
    String base;
    String filter;
    SearchScope scope;

    /**
     * Search response constructor
     * @param results the distinguised names
     * @param base the 
     * @param filter
     * @param scope
     */
    SearchResponse(Set<String> results, String base, String filter, SearchScope scope) {
        this.results = results;
        this.base = base;
        this.filter = filter;
        this.scope = scope;

    }

    @Override
    public Set<String> getResults() {

        return results;

    }

    @Override
    public String getBase() {

        return base;

    }

    @Override
    public String getFilter() {

        return filter;

    }

    @Override
    public SearchScope getScope() {

        return scope;

    }

}
