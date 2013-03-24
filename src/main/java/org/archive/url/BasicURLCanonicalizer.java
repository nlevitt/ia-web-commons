package org.archive.url;

/**
 * Canonicalizer that does more or less basic fixup. Based initially on rules
 * specified at <a href=
 * "https://developers.google.com/safe-browsing/developers_guide_v2#Canonicalization"
 * >https://developers.google.com/safe-browsing/developers_guide_v2#
 * Canonicalization</a>. These rules are designed for clients of google's
 * "experimental" Safe Browsing API to "check URLs against Google's
 * constantly-updated blacklists of suspected phishing and malware pages".
 * 
 * <p>
 * This class differs from google in treatment of non-ascii input. Google's
 * rules don't really address this except with one example test case, which
 * seems to suggest taking raw input bytes and pct-encoding them byte for byte.
 * Since the input to this class consists of java strings, not raw bytes, that
 * wouldn't be possible, even if deemed preferable. Instead
 * BasicURLCanonicalizer expresses non-ascii characters pct-encoded UTF-8.
 */
public class BasicURLCanonicalizer extends RulesBasedURLCanonicalizer implements
		CanonicalizerConstants {

	@Override
	protected CanonicalizeRules buildRules() {
		CanonicalizeRules rules = new CanonicalizeRules();

		rules.setRule(SCHEME_SETTINGS, SCHEME_ORIGINAL);
		rules.setRule(AUTH_SETTINGS, AUTH_MINIMAL_ESCAPE);
		rules.setRule(HOST_SETTINGS, HOST_MINIMAL_ESCAPE | HOST_IDN_TO_ASCII
				| HOST_REMOVE_EXTRA_DOTS | HOST_CANONICALIZE_IPV4
				| HOST_LOWERCASE);
		rules.setRule(PORT_SETTINGS, PORT_ORIGINAL);
		rules.setRule(PATH_SETTINGS, PATH_MINIMAL_ESCAPE
				| PATH_COLLAPSE_MULTIPLE_SLASHES | PATH_NORMALIZE_DOT_SEGMENTS);
		rules.setRule(QUERY_SETTINGS, QUERY_MINIMAL_ESCAPE);
		rules.setRule(FRAGMENT_SETTINGS, FRAGMENT_STRIP);

		return rules;
	}

}
