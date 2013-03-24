package org.archive.url;

public class OrdinaryIAURLCanonicalizer extends UsableURICanonicalizer {
	
	@Override
	protected CanonicalizeRules buildRules() {
		CanonicalizeRules rules = super.buildRules();
		
		rules.addRule(SCHEME_SETTINGS, SCHEME_LOWERCASE);
		rules.addRule(HOST_SETTINGS, HOST_LOWERCASE);
		rules.addRule(PORT_SETTINGS, PORT_STRIP_DEFAULT);
		rules.addRule(QUERY_SETTINGS, QUERY_STRIP_EMPTY);
		rules.addRule(FRAGMENT_SETTINGS, FRAGMENT_STRIP);
		
		return rules;
	}
}
