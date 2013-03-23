package org.archive.url;

import java.net.URISyntaxException;

public abstract class URLCanonicalizer {
	public abstract void canonicalize(HandyURL url);
	
	/**
	 * @param urlString presumed to be absolute
	 * @return
	 * @throws URISyntaxException
	 */
	public String canonicalize(String urlString) throws URISyntaxException {
		HandyURL handyUrl = URLParser.parse(urlString, false);
		canonicalize(handyUrl);
		return handyUrl.getURLString();
	}
}
