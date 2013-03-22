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

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Usable URI.
 * 
 * <p>We used to use {@link java.net.URI} for parsing URIs but ran across
 * quirky behaviors and bugs.  {@link java.net.URI} is not subclassable --
 * its final -- and its unlikely that java.net.URI will change any time soon
 * (See Gordon's considered petition here:
 * <a href="http://developer.java.sun.com/developer/bugParade/bugs/4939847.html">java.net.URI
 * should have loose/tolerant/compatibility option (or allow reuse)</a>).
 *
 * @author gojomo
 * @author stack
 * @author nlevitt
 */
public class UsableURI extends HandyURL
implements CharSequence, Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * The default charset of the protocol.  RFC 2277, 2396
     */
    protected static final String DEFAULT_PROTOCOL_CHARSET = "UTF-8";
    
    public UsableURI(String uriScheme, String userName, String userPass,
			String hostname, int port, String uriPath, String uriQuery,
			String uriFragment) {
		super(uriScheme, userName, userPass, hostname, port, uriPath, uriQuery,
				uriFragment);
	}

	public static String getDefaultProtocolCharset() {
        return DEFAULT_PROTOCOL_CHARSET;
    }

	@Deprecated
	public static boolean hasScheme(String possibleUrl) {
		return URLParser.hasScheme(possibleUrl);
	}

	/**
	 * @param pathOrUri A file path or a URI.
	 * @return Path parsed from passed <code>pathOrUri</code>.
	 * @throws URISyntaxException
	 */
	public static String parseFilename(final String pathOrUri)
			throws URISyntaxException {
		String path = pathOrUri;
		if (URLParser.hasScheme(pathOrUri)) {
			HandyURL url = URLParser.parse(pathOrUri);
			path = url.getPath();
		}
		return (new File(path)).getName();
	}

	private String cachedUrlString;

	@Override
	public String getURLString() {
		if (cachedUrlString == null) {
			cachedUrlString = super.getURLString();
		}
		return cachedUrlString;
	}

	@Override
	public int length() {
		return getURLString().length();
	}

	@Override
	public char charAt(int index) {
		return getURLString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return getURLString().subSequence(start, end);
	}

	@Override
	public void setAuthPass(String authPass) {
		cachedUrlString = null;
		super.setAuthPass(authPass);
	}

	@Override
	public void setFragment(String fragment) {
		cachedUrlString = null;
		super.setFragment(fragment);
	}

	@Override
	public void setAuthUser(String authUser) {
		cachedUrlString = null;
		super.setAuthUser(authUser);
	}

	@Override
	@Deprecated
	public void setHash(String hash) {
		cachedUrlString = null;
		super.setHash(hash);
	}

	@Override
	public void setHost(String host) {
		cachedUrlString = null;
		super.setHost(host);
	}

	@Override
	public void setOpaque(String opaque) {
		cachedUrlString = null;
		super.setOpaque(opaque);
	}

	@Override
	public void setPath(String path) {
		cachedUrlString = null;
		super.setPath(path);
	}

	@Override
	public void setPort(int port) {
		cachedUrlString = null;
		super.setPort(port);
	}

	@Override
	public void setQuery(String query) {
		cachedUrlString = null;
		super.setQuery(query);
	}

	@Override
	public void setScheme(String scheme) {
		cachedUrlString = null;
		super.setScheme(scheme);
	}
	
	@Override
	public String getScheme() {
		// XXX should this go in HandyURL?
		if (getOpaque() != null && getOpaque().startsWith("dns:")) {
			return "dns";
		} else {
			return super.getScheme();
		}
	}
	
	 /**
	  * Return the referenced host in the UURI, if any, also extracting the 
	  * host of a DNS-lookup URI where necessary. 
	  * 
	  * @return the target or topic host of the URI
	  */
	public String getReferencedHost() {
		if (getOpaque() != null && getOpaque().startsWith("dns:")) {
			return getOpaque().substring(4); 
		} else {
			return this.getHost();
		}
	}
	
	public String getAuthorityMinusUserinfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(getHost());

		if (getPort() != DEFAULT_PORT) {
			sb.append(":").append(getPort());
		}

		return sb.toString();
	}

	@Deprecated
	public String getURI() {
		return getURLString();
	}
	
	@Deprecated
	public String getEscapedURI() {
		return getURLString();
	}
	
	@Deprecated
	public String toCustomString() {
		return getURLString();
	}

	@Deprecated
	public String getEscapedPathQuery() {
		return getPathQuery();
	}

	public String getSurtForm() {
		// XXX cache? old one did
		return getSURTString(true);
	}

	public void resolve(String relative) {
		throw new RuntimeException("implement me!");
	}
	
	@Override
	public String toString() {
		return getURLString();
	}
}
