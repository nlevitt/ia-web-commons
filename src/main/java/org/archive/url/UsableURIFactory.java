/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.url;

import java.net.URISyntaxException;

/**
 * Factory that returns UsableURIs.
 * <p>
 * TODO: Test logging.
 * 
 * @author stack
 * @author nlevitt
 */
public class UsableURIFactory extends URLParser {

    private static final UsableURIFactory factory = new UsableURIFactory();
    
	public static UsableURI getInstance(String urlString) throws URISyntaxException {
		UsableURI uuri = factory.parseUrl(urlString);
		factory.canonicalize(uuri);
		return factory.validityCheck(uuri);
	}
	
	public static UsableURI getInstance(UsableURI base, String relative) throws URISyntaxException {
		UsableURI rel = factory.parseUrl(relative, true);
		return factory.resolve(base, rel);
	}

	/**
	 * @deprecated ignores charset, always UTF-8
	 */
	public static UsableURI getInstance(String urlString, String charset) throws URISyntaxException {
		UsableURI uuri = factory.parseUrl(urlString);
		factory.canonicalize(uuri, charset);
		return factory.validityCheck(uuri);
	}

	protected final URLCanonicalizer canon = new UsableURICanonicalizer();

    @Override
    protected UsableURI parseUrl(String urlString) throws URISyntaxException {
    	return (UsableURI) super.parseUrl(urlString);
    }
    
    @Override
    protected UsableURI parseUrl(String urlString, boolean allowRelative)
    		throws URISyntaxException {
    	return (UsableURI) super.parseUrl(urlString, allowRelative);
    }

    @Override
	protected HandyURL makeOne(String scheme, String authUser, String authPass,
			String host, int port, String path, String query, String fragment) {
		return new UsableURI(scheme, authUser, authPass, host, port, path,
				query, fragment);
	}
    
	protected void canonicalize(UsableURI uuri) {
		canon.canonicalize(uuri);
	}

	protected void canonicalize(UsableURI uuri, String charset) {
		canon.canonicalize(uuri, charset);
	}

	/**
	 * Resolves {@code rel} relative to {@code base}. Mostly follows IETF
	 * Standard 66 (RFC 3986) but differs in that a url with scheme and without
	 * authority is considered relative.
	 * 
	 * @param base
	 * @param rel
	 * @return new HandyURL of rel resolved relative to base
	 * @throws URISyntaxException
	 * 
	 * @see <a
	 *      href="http://tools.ietf.org/html/std66#section-5">http://tools.ietf.org/html/std66#section-5</a>
	 */
	public UsableURI resolve(UsableURI base, UsableURI rel) throws URISyntaxException {
		String scheme = rel.getScheme();
		String authUser = rel.getAuthUser();
		String authPass = rel.getAuthPass();
		String host = rel.getHost();
		String query = rel.getQuery();
		String path = rel.getPath();
		String fragment = rel.getFragment();
		int port = rel.getPort();
	
		if (host == null
				&& (scheme == null || scheme.equals(base.getScheme()))) {
			host = base.getHost();
			port = base.getPort();
			authUser = base.getAuthUser();
			authPass = base.getAuthPass();
			if (path.equals("")) {
				path = base.getPath();
				if (query == null) {
					query = base.getQuery();
				}
			} else if (!path.startsWith("/")) {
				int baseLastSlashIndex = base.getPath().lastIndexOf('/');
				if (baseLastSlashIndex < 0) {
					path = '/' + rel.getPath();
				} else {
					path = base.getPath().substring(0, baseLastSlashIndex + 1) + rel.getPath();
				}
			}
		}
		if (scheme == null) {
			scheme = base.getScheme();
		}

		UsableURI resolved = (UsableURI) makeOne(scheme, authUser, authPass,
				host, port, path, query, fragment);
		
		canon.canonicalize(resolved);
		return validityCheck(resolved);
	}

	protected UsableURI validityCheck(UsableURI uuri) throws URISyntaxException {
		if (uuri.length() > UsableURI.MAX_URL_LENGTH) {
			throw new URISyntaxException(uuri.getURLString(), "Created (escaped) uuri > " +
					UsableURI.MAX_URL_LENGTH);
		}
		return uuri;
	}

}
