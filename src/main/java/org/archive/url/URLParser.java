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
    
    // see RFC3986
    final public static Pattern URI_AUTHORITY_REGEX = Pattern.compile(
            "^(([^:@]*)(:([^@]*))?@)?(([^:/#?]*)|(\\[[^/#?]*\\]))(:([0-9]+)?)?$");
    //        12       3 4           56          7               8 9
    // 1: user:pass@
    // 2: user
    // 3: :pass
    // 4: pass
    // 5: host
    // 6: ipv4/reg-name
    // 7: ipv6
    // 8: :port
    // 9: port 
    
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
	 * go brewster
	 */
	public final static String WAIS_SCHEME = "wais://";
	
	public static final Pattern SCHEME_PATTERN =
			Pattern.compile("(?s)^([a-zA-Z][a-zA-Z0-9+.-]*):.*");
	
	/**
	 * Pattern for trimming off the same as {@link String#trim()}, and also nbsp
	 * non-breaking space \u00a0
	 */
	public static final Pattern TRIMMING_PATTERN = 
			Pattern.compile("(?s)^[\u0000-\u0020\u00a0]*(.*?)[\u0000-\u0020\u00a0]*$");
	
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
	 * Attempt to find the scheme (http, https, etc) from a given URL.
	 * @param url URL String to parse for a scheme.
	 * @return the scheme if known, null otherwise.
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
		} else if (urlString.startsWith("//")) {
			return "http:" + urlString;
		} else {
			return "http://" + urlString; 
		}
	}
	
    public static HandyURL parse(String urlString) throws URISyntaxException {
    	return SELF.parseUrl(urlString);
    }

    /**
     * @param allowRelative if false, will prepend a default scheme to a url string without one
     */
	public static HandyURL parse(String urlString, boolean allowRelative)
			throws URISyntaxException {
		return SELF.parseUrl(urlString, allowRelative);
	}
    
    protected static final URLParser SELF = new URLParser();
	
    protected HandyURL parseUrl(String urlString) throws URISyntaxException {
    	return parseUrl(urlString, false);
    }
    
    protected HandyURL parseUrl(String urlString, boolean allowRelative) throws URISyntaxException {
    	// first strip leading or trailing spaces:
    	// TODO: this strips too much - stripping non-printables
    	urlString = trim(urlString);
    	
    	// then remove leading, trailing, and internal TAB, CR, LF:
    	urlString = urlString.replaceAll(STRAY_SPACING,"");

    	if (!allowRelative) {
    		// add http:// if no scheme is present..
    		urlString = addDefaultSchemeIfNeeded(urlString);
    	}

        // cross fingers, toes, eyes...
    	Matcher matcher = RFC2396REGEX.matcher(urlString);
    	if(!matcher.matches()) {
			throw new URISyntaxException(urlString,
					"string does not match RFC 2396 regex");
    	}
        String scheme = matcher.group(2);
        String authority = matcher.group(5);
        String path = matcher.group(6);
        String query = matcher.group(8);
        String fragment = matcher.group(10);

        // Split Authority into USER:PASS@HOST:PORT
        String authUser = null;
        String authPass = null;
        String host = null;
        int port = HandyURL.DEFAULT_PORT;

        if (authority != null) {
        	Matcher m2 = URI_AUTHORITY_REGEX.matcher(authority);
        	if (!m2.matches()) {
        		throw new URISyntaxException(urlString,
        				"could not parse authority (" + authority + ")");
        	}
        	host = m2.group(5);
        	authUser = m2.group(2);
        	authPass = m2.group(4);
        	if (m2.group(9) != null) {
        		try {
        			port = Integer.parseInt(m2.group(9));
        		} catch (NumberFormatException e) {
        			throw new URISyntaxException(urlString, "could not parse port "
        					+ m2.group(9) + " - " + e);
        		}
        	}
        }
        
		return makeOne(scheme, authUser, authPass, host, port, path, query,
				fragment);
    }

	protected HandyURL makeOne(String scheme, String authUser, String authPass,
			String host, int port, String path, String query, String fragment) {
		return new HandyURL(scheme, authUser, authPass, host, port, path,
				query, fragment);
	}

	/**
	 * Resolves {@code rel} relative to {@code base} according to IETF Standard
	 * 66 (RFC 3986). The code here follows as exactly as possible the
	 * pseudocode from the standard, except that it does not remove dot segments
	 * from the resulting path. The result should be run through a canonicalizer
	 * to do that.
	 * 
	 * @param base
	 * @param rel
	 * @return new HandyURL of relativeUri resolved relative to baseUri
	 * 
	 * @see <a
	 *      href="http://tools.ietf.org/html/std66#section-5">http://tools.ietf.org/html/std66#section-5</a>
	 */
	public HandyURL resolve(UsableURI base, UsableURI rel) {
		String scheme, authUser, authPass, host, path, query, fragment;
		int port;
		if (rel.getScheme() != null) {
			scheme = rel.getScheme();
			authUser = rel.getAuthUser();
			authPass = rel.getAuthPass();
			host = rel.getHost();
			port = rel.getPort();
			path = rel.getPath();
			query = rel.getQuery();
		} else {
			if (rel.getHost() != null) {
				authUser = rel.getAuthUser();
				authPass = rel.getAuthPass();
				host = rel.getHost();
				port = rel.getPort();
				path = rel.getPath();
				query = rel.getQuery();
			} else {
				if (rel.getPath().equals("")) {
					path = base.getPath();
					if (rel.getQuery() != null) {
						query = rel.getQuery();
					} else {
						query = base.getQuery();
					}
				} else {
					if (rel.getPath().startsWith("/")) {
						path = rel.getPath();
					} else {
						int baseLastSlashIndex = base.getPath().lastIndexOf('/');
						if (baseLastSlashIndex < 0) {
							path = '/' + rel.getPath();
						} else {
							path = base.getPath().substring(0, baseLastSlashIndex + 1) + rel.getPath();
						}
					}
					query = rel.getQuery();
				}
				authUser = base.getAuthUser();
				authPass = base.getAuthPass();
				host = base.getHost();
				port = base.getPort();
			}
			scheme = base.getScheme(); 
		}
		fragment = rel.getFragment();
		
		return makeOne(scheme, authUser, authPass, host, port, path, query,
				fragment);
	}
}
