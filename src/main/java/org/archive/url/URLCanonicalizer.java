package org.archive.url;

import java.net.URISyntaxException;

public abstract class URLCanonicalizer {
	public abstract void canonicalize(HandyURL url);
	
	/**
	 * 
	 * @param urlString
	 * @return
	 * @throws URISyntaxException
	 */
	public String canonicalize(String urlString) throws URISyntaxException {
		urlString = URLParser.addDefaultSchemeIfNeeded(urlString);
		HandyURL handyUrl = URLParser.parse(urlString);
		canonicalize(handyUrl);
		return handyUrl.getURLString();
	}
}
