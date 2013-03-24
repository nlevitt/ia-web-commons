package org.archive.url;

public class NonMassagingIAURLCanonicalizer extends AggressiveIAURLCanonicalizer {
	@Override
	protected CanonicalizeRules buildRules() {
		CanonicalizeRules rules = super.buildRules();
		rules.removeRule(HOST_SETTINGS, HOST_REMOVE_WWWN);
		return rules;
	}
}
