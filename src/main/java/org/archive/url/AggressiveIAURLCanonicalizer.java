package org.archive.url;

public class AggressiveIAURLCanonicalizer extends UsableURICanonicalizer {

	@Override
	protected CanonicalizeRules buildRules() {
		CanonicalizeRules rules = super.buildRules();

		rules.addRule(SCHEME_SETTINGS, SCHEME_LOWERCASE);
		rules.addRule(HOST_SETTINGS, HOST_LOWERCASE | HOST_REMOVE_WWWN);

		rules.addRule(PORT_SETTINGS, PORT_STRIP_DEFAULT);

		rules.addRule(PATH_SETTINGS, PATH_LOWERCASE | PATH_STRIP_SESSION_ID
				| PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY);

		rules.addRule(QUERY_SETTINGS, QUERY_LOWERCASE | QUERY_STRIP_SESSION_ID
				| QUERY_STRIP_EMPTY | QUERY_ALPHA_REORDER);

		rules.addRule(FRAGMENT_SETTINGS, FRAGMENT_STRIP);

		rules.addRule(AUTH_SETTINGS, AUTH_STRIP_PASS | AUTH_STRIP_AUTH);

		return rules;
	}
}
