package org.archive.url;

public class UsableURICanonicalizer extends URLCanonicalizer {

	protected IAURLCanonicalizer uuriRules;
	protected BasicURLCanonicalizer uuriBasic;

	public UsableURICanonicalizer() {
		super();
		
		uuriBasic = new BasicURLCanonicalizer() {
			@Override
			public String normalizePath(String path) {
				return super.normalizePath(URLParser.trim(path), false);
			}
		};
		
		CanonicalizeRules rules = new CanonicalizeRules();
		rules.setRule(IAURLCanonicalizer.PORT_SETTINGS, IAURLCanonicalizer.PORT_STRIP_DEFAULT);
		uuriRules = new IAURLCanonicalizer(rules);
	}
	
	
	@Override
	public void canonicalize(HandyURL url) {
		uuriBasic.canonicalize(url);
		uuriRules.canonicalize(url);
	}
	
}
