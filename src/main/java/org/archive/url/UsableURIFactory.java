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
		uuri.setPath(uuri.getPath().replace('\\', '/'));
		factory.canonicalize(uuri);
		return factory.validityCheck(uuri);
	}
	
	public static UsableURI getInstance(UsableURI base, String relative) throws URISyntaxException {
		UsableURI rel = factory.parseUrl(relative, true);
		return factory.resolve(base, rel);
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

	public UsableURI resolve(UsableURI base, UsableURI rel) throws URISyntaxException {
		UsableURI resolved = (UsableURI) super.resolve(base, rel);
		resolved.setPath(resolved.getPath().replace('\\', '/'));
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

	/**
	 * @deprecated ignores charset, always UTF-8
	 */
	public static UsableURI getInstance(String urlString, String charset) throws URISyntaxException {
		return getInstance(urlString);
	}

}
