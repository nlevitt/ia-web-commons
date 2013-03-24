package org.archive.url;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.util.StringFieldExtractor;
import org.archive.util.StringFieldExtractor.StringTuple;

import com.google.common.net.InetAddresses;

abstract public class RulesBasedURLCanonicalizer extends URLCanonicalizer implements CanonicalizerConstants {
	
	private static final Pattern MULTI_SLASH_PATTERN = Pattern.compile("/+");
	private static final Pattern SINGLE_SLASH_PATTERN = Pattern.compile("/");
	private static final Pattern OCTAL_IP = Pattern.compile("^(0[0-7]*)(\\.[0-7]+)?(\\.[0-7]+)?(\\.[0-7]+)?$");
	private static final Pattern DECIMAL_IP = Pattern.compile("^([1-9][0-9]*)(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?$");

	/*
	 * http://tools.ietf.org/html/std66#section-2.2
	 * 
	 * gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@" sub-delims = "!" /
	 * "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
	 * 
	 * The purpose of reserved characters is to provide a set of delimiting
	 * characters that are distinguishable from other data within a URI. URIs
	 * that differ in the replacement of a reserved character with its
	 * corresponding percent-encoded octet are not equivalent. Percent- encoding
	 * a reserved character, or decoding a percent-encoded octet that
	 * corresponds to a reserved character, will change how the URI is
	 * interpreted by most applications. Thus, characters in the reserved set
	 * are protected from normalization and are therefore safe to be used by
	 * scheme-specific and producer-specific algorithms for delimiting data
	 * subcomponents within a URI.
	 */
	/*
	 * These ascii characters (thus bytes) shouldn't be touched. If they occur
	 * encoded in the input, they should end up encoded in the output, and if
	 * they occur unencoded in the input, they should end up unencoded in the
	 * output. See BasicURLCanonicalizerTest#testEscapedReserved()
	 * 
	 * We add '%' to the list to match browser behavior and established UURI
	 * behavior.
	 */
	private static final String ESCAPING_DONT_TOUCH_CHARS = ":/?#[]@!$&'()*+,;=%";

	private final CanonicalizeRules rules = buildRules();
	
	abstract protected CanonicalizeRules buildRules(); 

	public void canonicalize(HandyURL url) {
		applySchemeRules(url);
		applyAuthRules(url);
		applyHostRules(url);
		applyPortRules(url);
		applyPathRules(url);
		applyQueryRules(url);
		applyFragmentRules(url);
	}

	protected void applyFragmentRules(HandyURL url) {
		if (rules.isSet(FRAGMENT_SETTINGS, FRAGMENT_STRIP)) {
			url.setFragment(null);
		}
	}

	protected void applyQueryRules(HandyURL url) {
		String query = url.getQuery();
		if (query != null) {
			if (query.equals("")) {
				if (rules.isSet(QUERY_SETTINGS, QUERY_STRIP_EMPTY)) {
					query = null;
				}
			} else {
				// we have a query... what to do with it?

				if (rules.isSet(QUERY_SETTINGS, QUERY_MINIMAL_ESCAPE)) {
					query = escape(unescape(query));
				}
				if (rules.isSet(QUERY_SETTINGS, QUERY_STRIP_SESSION_ID)) {
					query = URLRegexTransformer.stripQuerySessionID(query);
				}
				if (rules.isSet(QUERY_SETTINGS, QUERY_LOWERCASE)) {
					query = query.toLowerCase();
				}
				// re-order?
				if (rules.isSet(QUERY_SETTINGS, QUERY_ALPHA_REORDER)) {
					query = alphaReorderQuery(query);
				}
			}
			url.setQuery(query);
		}
	}

	protected void applyPathRules(HandyURL url) {
		String path = url.getPath();
		
		/*
		 * Only handle escaping if it's not a slash-delimited directory path. (A
		 * url like "dns:foo.org" has path "foo.org" and no authority.) If url
		 * has an authority the path is assumed to be slash-delimited a
		 * directory path because it must begin with "/" by definition unless
		 * it's an empty string.
		 */
		if (url.getHost() == null && !path.startsWith("/")) {
			if (rules.isSet(PATH_SETTINGS, PATH_MINIMAL_ESCAPE)) {
				path = unescape(path);
				path = escape(path);
			}
		} else {
			if (rules.isSet(PATH_SETTINGS, PATH_BACKSLASH_TO_SLASH)) {
				path = path.replace('\\', '/');
			}
			if (rules.isSet(PATH_SETTINGS, PATH_COLLAPSE_MULTIPLE_SLASHES)) {
				path = MULTI_SLASH_PATTERN.matcher(path).replaceAll("/");
			}
			if (rules.isSet(PATH_SETTINGS, PATH_MINIMAL_ESCAPE)) {
				path = unescape(path);
			}
			if (rules.isSet(PATH_SETTINGS, PATH_NORMALIZE_DOT_SEGMENTS)) {
				path = normalizePath(path);
			}
			if (rules.isSet(PATH_SETTINGS, PATH_MINIMAL_ESCAPE)) {
				path = escape(path);
			}
			if (rules.isSet(PATH_SETTINGS, PATH_LOWERCASE)) {
				path = path.toLowerCase();
			}
			if (rules.isSet(PATH_SETTINGS, PATH_STRIP_SESSION_ID)) {
				path = URLRegexTransformer.stripPathSessionID(path);
			}
			if (rules.isSet(PATH_SETTINGS,PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY)) {
				if (path.endsWith("/") && (path.length() > 1)) {
					path = path.substring(0, path.length() - 1);
				}
			}
		}
		url.setPath(path);
	}

	protected void applyPortRules(HandyURL url) {
		if (rules.isSet(PORT_SETTINGS, PORT_STRIP_DEFAULT)) {
			int defaultPort = getDefaultPort(url.getScheme());
			if (defaultPort == url.getPort()) {
				url.setPort(HandyURL.DEFAULT_PORT);
			}
		}
	}

	protected void applyAuthRules(HandyURL url) {
		if (rules.isSet(AUTH_SETTINGS, AUTH_STRIP_AUTH)) {
			url.setAuthUser(null);
			url.setAuthPass(null);
		}
		if (rules.isSet(AUTH_SETTINGS, AUTH_STRIP_PASS)) {
			url.setAuthPass(null);
		}
		if (rules.isSet(AUTH_SETTINGS, AUTH_MINIMAL_ESCAPE)) {
			url.setAuthUser(escape(unescape(url.getAuthUser())));
			url.setAuthPass(escape(unescape(url.getAuthPass())));
		}
	}

	protected void applyHostRules(HandyURL url) {
		String host = url.getHost();
		if (host != null) {
			if (rules.isSet(HOST_SETTINGS, HOST_MINIMAL_ESCAPE)) {
				host = unescape(host);
			}
			if (rules.isSet(HOST_SETTINGS, HOST_IDN_TO_ASCII)) {
				host = IDN.toASCII(host);
			}
			if (rules.isSet(HOST_SETTINGS, HOST_REMOVE_EXTRA_DOTS)) {
				host = host.replaceAll("^\\.+", "").replaceAll("\\.\\.+", ".").replaceAll("\\.$", "");
			}
			if (rules.isSet(HOST_SETTINGS, HOST_CANONICALIZE_IPV4)) {
				String ipv4 = attemptIPv4Formats(host);
				if (ipv4 != null) {
					host = ipv4;
				}
			}
			if (rules.isSet(HOST_SETTINGS, HOST_TRIM_ENCODED_WHITESPACE)) {
				host = URLParser.trim(host);
			}
			if (rules.isSet(HOST_SETTINGS, HOST_MINIMAL_ESCAPE)) {
				host = escape(host);
			}
			if (rules.isSet(HOST_SETTINGS, HOST_LOWERCASE)) {
				host = host.toLowerCase();
			}
			if (rules.isSet(HOST_SETTINGS, HOST_REMOVE_WWWN)) {
				host = massageHost(host);
			}
			url.setHost(host);
		}
	}

	protected void applySchemeRules(HandyURL url) {
		if (rules.isSet(SCHEME_SETTINGS, SCHEME_LOWERCASE)) {
			if (url.getScheme() != null) {
				url.setScheme(url.getScheme().toLowerCase());
			}
		}
	}
	
	public static String alphaReorderQuery(String orig) {
		if(orig == null) {
			return null;
		}
		if(orig.length() <= 1) {
			return orig;
		}
		String args[] = orig.split("&",-1);
		StringTuple qas[] = new StringTuple[args.length];
		StringFieldExtractor sfe = new StringFieldExtractor('=', 1);
		for(int i = 0; i < args.length; i++) {
			qas[i] = sfe.split(args[i]);
		}
		Arrays.sort(qas,new Comparator<StringTuple>() {

			public int compare(StringTuple o1, StringTuple o2) {
				int cmp = o1.first.compareTo(o2.first);
				if(cmp != 0) {
					return cmp;
				}
				if(o1.second == null) {
					if(o2.second == null) {
						// both null - same
						return 0;
					}
					// first null, second non-null, so first is smaller
					return -1;
				} else if(o2.second == null) {
					// first non-null, second null, second is smaller
					return 1;
				}
				// neither null, compare them:
				return o1.second.compareTo(o2.second);
			}
		});
		StringBuilder sb = new StringBuilder(orig.length());
		int max = qas.length - 1;
		for(int i = 0; i < max; i++) {
			if(qas[i].second == null) {
				sb.append(qas[i].first).append('&');
				
			} else {
				sb.append(qas[i].first).append('=').append(qas[i].second).append('&');
			}
		}
		if(qas[max].second == null) {
			sb.append(qas[max].first);
		
		} else {
			sb.append(qas[max].first).append('=').append(qas[max].second);
		}
		
		return sb.toString();
	}
	
	
	public static final Pattern WWWN_PATTERN = Pattern.compile("^www\\d*\\.");
	public static String massageHost(String host) {
		while(true) {
			Matcher m = WWWN_PATTERN.matcher(host);
			if(m.find()) {
				host = host.substring(m.group(0).length());
			} else {
				break;
			}
		}
		return host;
	}
	public static int getDefaultPort(String scheme) {
		String lcScheme = scheme.toLowerCase();
		if(lcScheme.equals("http")) {
			return 80;
		} else if(lcScheme.equals("https")) {
			return 443;
		}
		return 0;
	}
	
	protected static Charset _UTF8 = null;

	protected static Charset UTF8() {
		if (_UTF8 == null) {
			_UTF8 = Charset.forName("UTF-8");
		}
		return _UTF8;
	}

	public String escape(String input) {
		if (input == null) {
			return null;
		}

		byte[] utf8bytes = input.getBytes(UTF8());
		StringBuilder sb = null;

		for (int i = 0; i < utf8bytes.length; i++) {
			int b = utf8bytes[i] & 0xff;
			/*
			 * Google says "percent-escape all characters in the URL which are
			 * <= ASCII 32, >= 127, "#", or "%". The escapes should use
			 * uppercase hex characters". However, '#' is a reserved character,
			 * so we shouldn't touch it. And it turns out that browsers don't
			 * encode '%', and UURI has always left '%' alone as well, so we
			 * don't touch that either.
			 */
			if (b <= 0x20 || b >= 0x7f) {
				if (sb == null) {
					/*
					 * everything up to this point has been an ascii character
					 * not needing escaping
					 */
					sb = new StringBuilder(input.substring(0, i));
				}
				sb.append("%");
				String hex = Integer.toHexString(b).toUpperCase();
				if (hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			} else {
				if (sb != null) {
					sb.append((char) b);
				}
			}
		}
		if (sb == null) {
			return input;
		}
		return sb.toString();
	}

	public String unescape(String input) {
		if (input == null) {
			return null;
		}
		StringBuilder sb = null;
		int pctUtf8SeqStart = -1;
		ByteBuffer bbuf = null;
		CharsetDecoder utf8decoder = null;
		int i = 0;
		int h1, h2;
		while (i < input.length()) {
			char c = input.charAt(i);
			if (i <= input.length() - 3 && c == '%'
					&& (h1 = getHex(input.charAt(i + 1))) >= 0
					&& (h2 = getHex(input.charAt(i + 2))) >= 0) {
				char b = (char) (((h1 << 4) + h2) & 0xff);
				if (ESCAPING_DONT_TOUCH_CHARS.indexOf(b) < 0) {
					if (sb == null) {
						sb = new StringBuilder(input.length());
						if (i > 0) {
							sb.append(input.substring(0, i));
						}
					}
					if (pctUtf8SeqStart < 0 && b < 0x80) { // plain ascii
						sb.append((char) b);
					} else {
						if (pctUtf8SeqStart < 0) {
							pctUtf8SeqStart = i;
							if (bbuf == null) {
								bbuf = ByteBuffer.allocate((input.length() - i) / 3);
							}
						}
						bbuf.put((byte) b);
					}
					i += 3;
					continue;
				}
			}
			if (pctUtf8SeqStart >= 0) {
				if (utf8decoder == null) {
					utf8decoder = UTF8().newDecoder();
				}
				appendDecodedPctUtf8(sb, bbuf, input, pctUtf8SeqStart, i,
						utf8decoder);
				pctUtf8SeqStart = -1;
				bbuf.clear();
			}
			if (sb != null) {
				sb.append(c);
			}
			i++;
		}
		if (pctUtf8SeqStart >= 0) {
			if (utf8decoder == null) {
				utf8decoder = UTF8().newDecoder();
			}
			appendDecodedPctUtf8(sb, bbuf, input, pctUtf8SeqStart, i,
					utf8decoder);
		}

		if (sb != null) {
			return sb.toString();
		} else {
			return input;
		}
	}

	/**
	 * Decodes bytes in bbuf as utf-8 and appends decoded characters to sb. If
	 * decoding of any portion fails, appends the un-decodable %xx%xx sequence
	 * extracted from inputStr instead of decoded characters. See "bad unicode"
	 * tests in GoogleCanonicalizerTest#testDecode(). Variables only make sense
	 * within context of {@link #unescape(String)}.
	 * 
	 * @param sb
	 *            StringBuilder to append to
	 * @param bbuf
	 *            raw bytes decoded from %-encoded input
	 * @param inputStr
	 *            full input string
	 * @param seqStart
	 *            start index inclusive within inputStr of %-encoded sequence
	 * @param seqEnd
	 *            end index exclusive within inputStr of %-encoded sequence
	 * @param utf8decoder
	 */
	private void appendDecodedPctUtf8(StringBuilder sb, ByteBuffer bbuf,
			String inputStr, int seqStart, int seqEnd,
			CharsetDecoder utf8decoder) {
		// assert bbuf.position() * 3 == seqEnd - seqStart;
		utf8decoder.reset();
		CharBuffer cbuf = CharBuffer.allocate(bbuf.position());
		bbuf.flip();
		while (bbuf.position() < bbuf.limit()) {
			CoderResult coderResult = utf8decoder.decode(bbuf, cbuf, true);
			sb.append(cbuf.flip());
			if (coderResult.isMalformed()) {
				// put the malformed %xx%xx into the result un-decoded
				CharSequence undecodablePctHex = inputStr.subSequence(seqStart
						+ 3 * bbuf.position(), seqStart + 3 * bbuf.position()
						+ 3 * coderResult.length());
				sb.append(undecodablePctHex);

				// there could be more good stuff after the bad
				bbuf.position(bbuf.position() + coderResult.length());
			}
			cbuf.clear();
		}
	}

	public int getHex(final int b) {
		if (b < '0') {
			return -1;
		}
		if (b <= '9') {
			return b - '0';
		}
		if (b < 'A') {
			return -1;
		}
		if (b <= 'F') {
			return 10 + (b - 'A');
		}
		if (b < 'a') {
			return -1;
		}
		if (b <= 'f') {
			return 10 + (b - 'a');
		}
		return -1;
	}
	
	public String normalizePath(String path) {
		// -1 gives an empty trailing element if path ends with '/':
		String[] segments = SINGLE_SLASH_PATTERN.split(path, -1);
		LinkedList<String> keptSegments = new LinkedList<String>();
		for (int i = 0; i < segments.length; i++) {
			String p = segments[i];
			if (i == 0 && i != segments.length - 1) {
			} else if (".".equals(p)) {
				if (i == segments.length - 1) {
					// ending of a/. normalizes to a/ 
					keptSegments.add("");
				}
			} else if ("..".equals(p)) {
				// pop the last path, if present:
				if (keptSegments.size() > 0) {
					keptSegments.remove(keptSegments.size() - 1);
				}
				if (i == segments.length - 1) {
					// ending of a/b/.. normalizes to a/ 
					keptSegments.add("");
				}
			} else {
				keptSegments.add(p);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String segment: keptSegments) {
			sb.append('/').append(segment);
		}
		return sb.toString();
	}
	
	public String attemptIPv4Formats(String host) { // throws URIException {
		if (host == null) {
			return null;
		}
		if (host.matches("^\\d+$")) {
			try {
				Long l = Long.parseLong(host);
				return InetAddresses.fromInteger(l.intValue()).getHostAddress();
			} catch (NumberFormatException e) {
			}
		} else {
			// check for octal:
			Matcher m = OCTAL_IP.matcher(host);
			if (m.matches()) {
				int parts = m.groupCount();
				if (parts > 4) {
					// WHAT TO DO?
					return null;
					// throw new URIException("Bad Host("+host+")");
				}
				int[] ip = new int[] { 0, 0, 0, 0 };
				for (int i = 0; i < parts; i++) {
					int octet;
					try {
						octet = Integer.parseInt(
								m.group(i + 1).substring((i == 0) ? 0 : 1), 8);
					} catch (Exception e) {
						return null;
					}
					if ((octet < 0) || (octet > 255)) {
						return null;
						// throw new URIException("Bad Host("+host+")");
					}
					ip[i] = octet;
				}
				return String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
			} else {
				Matcher m2 = DECIMAL_IP.matcher(host);
				if (m2.matches()) {
					int parts = m2.groupCount();
					if (parts > 4) {
						// WHAT TO DO?
						return null;
						// throw new URIException("Bad Host("+host+")");
					}
					int[] ip = new int[] { 0, 0, 0, 0 };
					for (int i = 0; i < parts; i++) {

						String m2Group = m2.group(i + 1);
						if (m2Group == null)
							return null;
						// int octet =
						// Integer.parseInt(m2.group(i+1).substring((i==0)?0:1));
						int octet;
						try {
							octet = Integer.parseInt(m2Group
									.substring((i == 0) ? 0 : 1));
						} catch (Exception e) {
							return null;
						}
						if ((octet < 0) || (octet > 255)) {
							return null;
							// throw new URIException("Bad Host("+host+")");
						}
						ip[i] = octet;
					}
					return String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2],
							ip[3]);

				}
			}
		}
		return null;
	}

}
