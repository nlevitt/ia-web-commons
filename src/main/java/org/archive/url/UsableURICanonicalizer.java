package org.archive.url;

public class UsableURICanonicalizer extends RulesBasedURLCanonicalizer {

	@Override
	protected CanonicalizeRules buildRules() {
		CanonicalizeRules rules = new CanonicalizeRules();

		rules.setRule(SCHEME_SETTINGS, SCHEME_ORIGINAL);
		rules.setRule(AUTH_SETTINGS, AUTH_MINIMAL_ESCAPE);
		rules.setRule(HOST_SETTINGS, HOST_MINIMAL_ESCAPE | HOST_IDN_TO_ASCII
				| HOST_REMOVE_EXTRA_DOTS | HOST_CANONICALIZE_IPV4
				| HOST_LOWERCASE | HOST_TRIM_ENCODED_WHITESPACE);
		rules.setRule(PORT_SETTINGS, PORT_STRIP_DEFAULT);
		rules.setRule(PATH_SETTINGS, PATH_MINIMAL_ESCAPE
				| PATH_NORMALIZE_DOT_SEGMENTS | PATH_BACKSLASH_TO_SLASH);
		rules.setRule(QUERY_SETTINGS, QUERY_MINIMAL_ESCAPE);
		rules.setRule(FRAGMENT_SETTINGS, FRAGMENT_STRIP);

		return rules;
	}
	
}
