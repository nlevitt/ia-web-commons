package org.archive.url;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLParser {
    /**
     * RFC 2396-inspired regex.
     *
     * From the RFC Appendix B:
     * <pre>
     * URI Generic Syntax                August 1998
     *
     * B. Parsing a URI Reference with a Regular Expression
     *
     * As described in Section 4.3, the generic URI syntax is not sufficient
     * to disambiguate the components of some forms of URI.  Since the
     * "greedy algorithm" described in that section is identical to the
     * disambiguation method used by POSIX regular expressions, it is
     * natural and commonplace to use a regular expression for parsing the
     * potential four components and fragment identifier of a URI reference.
     *
     * The following line is the regular expression for breaking-down a URI
     * reference into its components.
     *
     * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
     *  12            3  4          5       6  7        8 9
     *
     * The numbers in the second line above are only to assist readability;
     * they indicate the reference points for each subexpression (i.e., each
     * paired parenthesis).  We refer to the value matched for subexpression
     * <n> as $<n>.  For example, matching the above expression to
     *
     * http://www.ics.uci.edu/pub/ietf/uri/#Related
     *
     * results in the following subexpression matches:
     *
     * $1 = http:
     * $2 = http
     * $3 = //www.ics.uci.edu
     * $4 = www.ics.uci.edu
     * $5 = /pub/ietf/uri/
     * $6 = <undefined>
     * $7 = <undefined>
     * $8 = #Related
     * $9 = Related
     *
     * where <undefined> indicates that the component is not present, as is
     * the case for the query component in the above example.  Therefore, we
     * can determine the value of the four components and fragment as
     *
     * scheme    = $2
     * authority = $4
     * path      = $5
     * query     = $7
     * fragment  = $9
     * </pre>
     *
     * -- 
     * <p>Below differs from the rfc regex in that... 
     * (1) it has java escaping of regex characters 
     * (2) we allow a URI made of a fragment only (Added extra
     * group so indexing is off by one after scheme).
     * (3) scheme is limited to legal scheme characters 
     */
    final public static Pattern RFC2396REGEX = Pattern.compile(
            "^(([a-zA-Z][a-zA-Z0-9\\+\\-\\.]*):)?((//([^/?#]*))?([^?#]*)(\\?([^#]*))?)?(#(.*))?");
    //        12                                 34  5          6       7   8          9 A
    //                                       2 1             54        6          87 3      A9
    // 1: scheme
    // 2: scheme:
    // 3: //authority/path
    // 4: //authority
    // 5: authority
    // 6: path
    // 7: ?query
    // 8: query 
    // 9: #fragment
    // A: fragment
    
    public static final String STRAY_SPACING = "[\n\r\t\\p{Zl}\\p{Zp}\u0085]+";
    
    /**
     * Pattern that looks for case of three or more slashes after the 
     * scheme.  If found, we replace them with two only as mozilla does.
     */
    final static Pattern HTTP_SCHEME_SLASHES =
        Pattern.compile("^(?i)(https?://)/+(.*)");

	/**
	 * ARC/WARC specific DNS resolution record.
	 */
	public final static String DNS_SCHEME = "dns:";
	/**
	 * ARC header record.
	 */
	public final static String FILEDESC_SCHEME = "filedesc:";
	/**
	 * WARC header record.
	 */
	public final static String WARCINFO_SCHEME = "warcinfo:";
	
	/**
	 * Default scheme to assume if unspecified. No context implied...
	 */
	public final static String DEFAULT_SCHEME = "http://";	
	
	/**
	 * go brewster
	 */
	public final static String WAIS_SCHEME = "wais://";
	
	public static final Pattern SCHEME_PATTERN =
			Pattern.compile("(?s)^([a-zA-Z0-9+.-]+):.*");
	
	/**
	 * Pattern for trimming off the same as {@link String#trim()}, and also nbsp
	 * non-breaking space \u0080
	 */
	public static final Pattern TRIMMING_PATTERN = 
			Pattern.compile("^[\u0000-\u0020\u0080]*(.*?)[\u0000-\u0020\u0080]*$");
	
	public static String trim(String s) {
		int left = s.length(), right = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > ' ' && c != '\u00a0') {
				left = i; 
				break;
			}
		}
		for (int i = s.length() - 1; i >= left; i--) {
			char c = s.charAt(i);
			if (c > ' ' && c != '\u00a0') {
				right = i + 1;
				break;
			}
		}

		if (left >= right) {
			return "";
		} else if (left > 0 || right < s.length() - 1) {
			return s.substring(left, right);
		} else {
			return s;
		}
	}
	
	/**
	 * Attempt to find the scheme (http://, https://, etc) from a given URL.
	 * @param url URL String to parse for a scheme.
	 * @return the scheme, including trailing "://" if known, null otherwise.
	 */
	public static String urlToScheme(final String url) {
		Matcher matcher = SCHEME_PATTERN.matcher(url);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
	
	public static boolean hasScheme(String urlString) {
		return SCHEME_PATTERN.matcher(urlString).matches();
	}
    
	public static String addDefaultSchemeIfNeeded(String urlString) {
		if (urlString == null) {
			return null;
		} else if (hasScheme(urlString)) {
			return urlString;
		} else {
			// add http:// if no scheme is present:
			return DEFAULT_SCHEME + urlString;
		}
	}
	
    public static HandyURL parse(String urlString) throws URISyntaxException {
    	return SELF.parseUrl(urlString);
    }
    
    protected static final URLParser SELF = new URLParser();
	
    protected HandyURL parseUrl(String urlString) throws URISyntaxException {
    	// first strip leading or trailing spaces:
    	// TODO: this strips too much - stripping non-printables
    	urlString = trim(urlString);
    	
    	// then remove leading, trailing, and internal TAB, CR, LF:
    	urlString = urlString.replaceAll(STRAY_SPACING,"");

    	// check for non-standard URLs:
    	if(urlString.startsWith(DNS_SCHEME)
    			|| urlString.startsWith(FILEDESC_SCHEME)
    			|| urlString.startsWith(WARCINFO_SCHEME)) {
    		HandyURL h = new HandyURL();
    		// TODO: we could set the authority - to allow SURT stuff to work..
    		h.setOpaque(urlString);
    		return h;
    	}
    	
    	// replace leading http:/// with http://
        Matcher m1 = HTTP_SCHEME_SLASHES.matcher(urlString);
        if (m1.matches()) {
        	urlString = m1.group(1) + m1.group(2);
        }

        // cross fingers, toes, eyes...
    	Matcher matcher = RFC2396REGEX.matcher(urlString);
    	if(!matcher.matches()) {
			throw new URISyntaxException(urlString,
					"string does not match RFC 2396 regex");
    	}
        String uriScheme = matcher.group(2);
        String uriAuthority = matcher.group(5);
        String uriPath = matcher.group(6);
        String uriQuery = matcher.group(8);
        String uriFragment = matcher.group(10);

        // Split Authority into USER:PASS@HOST:PORT
        String userName = null;
        String userPass = null;
        String hostname = null;
        int port = HandyURL.DEFAULT_PORT;

        if (uriAuthority != null) {
        	String userInfo = null;
        	String colonPort = null;

        	int atIndex = uriAuthority.indexOf('@');
        	int portColonIndex = uriAuthority.indexOf(':',(atIndex<0)?0:atIndex);


        	if(atIndex<0 && portColonIndex<0) {
        		// most common case: neither userinfo nor port
        		hostname = uriAuthority;
        	} else if (atIndex<0 && portColonIndex>-1) {
        		// next most common: port but no userinfo
        		hostname = uriAuthority.substring(0,portColonIndex);
        		colonPort = uriAuthority.substring(portColonIndex);
        	} else if (atIndex>-1 && portColonIndex<0) {
        		// uncommon: userinfo, no port
        		userInfo = uriAuthority.substring(0,atIndex);
        		hostname = uriAuthority.substring(atIndex+1);
        	} else {
        		// uncommon: userinfo, port
        		userInfo = uriAuthority.substring(0,atIndex);
        		hostname = uriAuthority.substring(atIndex+1,portColonIndex);
        		colonPort = uriAuthority.substring(portColonIndex);
        	}
        	if(colonPort != null) {
        		if(colonPort.startsWith(":")) {
        			try {
        				port = Integer.parseInt(colonPort.substring(1));
        			} catch(NumberFormatException e) {
        				throw new URISyntaxException(urlString, "bad port "
        						+ colonPort.substring(1));
        			}
        		} else {
        			// uncommon: userinfo, port
        			userInfo = uriAuthority.substring(0,atIndex+1);
        			hostname = uriAuthority.substring(atIndex+1,portColonIndex);
        			colonPort = uriAuthority.substring(portColonIndex);
        		}
        	}
        	if(userInfo != null) {
        		int passColonIndex = userInfo.indexOf(':');
        		if(passColonIndex == -1) {
        			// no password:
        			userName = userInfo;
        		} else {
        			userName = userInfo.substring(0, passColonIndex);
        			userPass = userInfo.substring(passColonIndex + 1);
        		}
        	}
        }
        return makeOne(uriScheme,userName,userPass,hostname,
        		port,uriPath,uriQuery,uriFragment);
    }

	protected HandyURL makeOne(String uriScheme, String userName,
			String userPass, String hostname, int port, String uriPath,
			String uriQuery, String uriFragment) {
		return new HandyURL(uriScheme,userName,userPass,hostname,
				port,uriPath,uriQuery,uriFragment);
	}
}
