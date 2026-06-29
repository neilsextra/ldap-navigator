package au.org.tso.ldap.navigator;

import java.util.Set;

class SearchResponse implements DirectoryExplorer.ResultContainer {
    Set<String> results;
    String dn;
    
    SearchResponse(Set<String> results, String dn) {
        this.results = results;
        this.dn = dn;
    }

	@Override
	public Set<String> getResults() {

        return results;

	}

	@Override
	public String getDn() {

        return dn;

    }
    
}
