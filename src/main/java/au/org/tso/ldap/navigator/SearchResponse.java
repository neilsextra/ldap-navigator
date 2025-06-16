package au.gov.sa.euc.ldap.navigator;

import java.util.List;

class SearchResponse implements DirectoryExplorer.ResultContainer {
    List<String> results;
    String dn;
    String cursorPosition;
    
    SearchResponse(List<String> results, String dn, String cursorPosition) {

        this.results = results;
        this.dn = dn;
        this.cursorPosition = cursorPosition;

    }

	@Override
	public List<String> getResults() {

        return results;

	}

	@Override
	public String getDn() {

        return dn;

    }

	@Override
	public String getCursorPosition() {

        return cursorPosition;

    }
    
}
