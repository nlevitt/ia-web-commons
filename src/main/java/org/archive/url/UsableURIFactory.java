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
import java.util.logging.Logger;

/**
 * Factory that returns UsableURIs.
 * <p>
 * TODO: Test logging.
 * 
 * @author stack
 * @author nlevitt
 */
public class UsableURIFactory extends URLParser {

	// XXX can we get rid of these constants?
	 public static final String SLASHDOTDOTSLASH = "^(/\\.\\./)+";
	 public static final String SLASH = "/";
	 public static final String HTTP = "http";
	 public static final String HTTP_PORT = ":80";
	 public static final String HTTPS = "https";
	 public static final String HTTPS_PORT = ":443";
	 public static final String DOT = ".";
	 public static final String EMPTY_STRING = "";
	 public static final String NBSP = "\u00A0";
	 public static final String SPACE = " ";
	 public static final String ESCAPED_SPACE = "%20";
	 public static final String TRAILING_ESCAPED_SPACE = "^(.*)(%20)+$";
	 public static final String PIPE = "|";
	 public static final String PIPE_PATTERN = "\\|";
	 public static final String ESCAPED_PIPE = "%7C";
	 public static final String CIRCUMFLEX = "^";
	 public static final String CIRCUMFLEX_PATTERN = "\\^";
	 public static final String ESCAPED_CIRCUMFLEX = "%5E";
	 public static final String QUOT = "\"";
	 public static final String ESCAPED_QUOT = "%22";
	 public static final String SQUOT = "'";
	 public static final String ESCAPED_SQUOT = "%27";
	 public static final String APOSTROPH = "`";
	 public static final String ESCAPED_APOSTROPH = "%60";
	 public static final String LSQRBRACKET = "[";
	 public static final String LSQRBRACKET_PATTERN = "\\[";
	 public static final String ESCAPED_LSQRBRACKET = "%5B";
	 public static final String RSQRBRACKET = "]";
	 public static final String RSQRBRACKET_PATTERN = "\\]";
	 public static final String ESCAPED_RSQRBRACKET = "%5D";
	 public static final String LCURBRACKET = "{";
	 public static final String LCURBRACKET_PATTERN = "\\{";
	 public static final String ESCAPED_LCURBRACKET = "%7B";
	 public static final String RCURBRACKET = "}";
	 public static final String RCURBRACKET_PATTERN = "\\}";
	 public static final String ESCAPED_RCURBRACKET = "%7D";
	 public static final String BACKSLASH = "\\";
	 public static final String ESCAPED_BACKSLASH = "%5C";
	 public static final String STRAY_SPACING = "[\n\r\t]+";
	 public static final String IMPROPERESC_REPLACE = "%25$1";
	 public static final String IMPROPERESC =
	     "%((?:[^\\p{XDigit}])|(?:.[^\\p{XDigit}])|(?:\\z))";
	 public static final String COMMERCIAL_AT = "@";
	 public static final char PERCENT_SIGN = '%';
	 public static final char COLON = ':';

    /**
     * Logging instance.
     */
    private static Logger logger =
        Logger.getLogger(UsableURIFactory.class.getName());
    
    /**
     * The single instance of this factory.
     */
    private static final UsableURIFactory factory = new UsableURIFactory();
	private static final BasicURLCanonicalizer basic = 
			new BasicURLCanonicalizer();

    @Override
    protected UsableURI parseUrl(String urlString) throws URISyntaxException {
    	return (UsableURI) super.parseUrl(urlString);
    }

    @Override
    protected UsableURI makeOne(String uriScheme, String userName,
    		String userPass, String hostname, int port, String uriPath,
    		String uriQuery, String uriFragment) {
    	return new UsableURI(uriScheme, userName, userPass, hostname, port, uriPath,
    			uriQuery, uriFragment);
    }

	public static UsableURI getInstance(String urlString) throws URISyntaxException {
		UsableURI uuri = factory.parseUrl(urlString);
		uuri.setPath(uuri.getPath().replace('\\', '/'));
		basic.canonicalize(uuri);
		return uuri;
	}

	public static UsableURI getInstance(UsableURI base, String string) throws URISyntaxException {
		throw new RuntimeException("implement me!");
	}

	/**
	 * @deprecated ignores charset, always UTF-8
	 */
	public static UsableURI getInstance(String urlString, String charset) throws URISyntaxException {
		return getInstance(urlString);
	}

}
