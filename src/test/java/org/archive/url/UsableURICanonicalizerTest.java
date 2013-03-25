package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class UsableURICanonicalizerTest extends TestCase {
	private UsableURICanonicalizer guc = new UsableURICanonicalizer();

	public void testEscapedReserved() throws URISyntaxException {
		// encoded '.' in host
		checkCanonicalization("http://www%2eexample.com/path/foo?c=d&a=b",
				"http://www.example.com/path/foo?c=d&a=b");

		// do not want to decode encoded '/' in host and path
		checkCanonicalization("http://exa%2fmple.com/path/foo?c=d&a=b",
				"http://exa%2fmple.com/path/foo?c=d&a=b");
		checkCanonicalization("http://example.com/pa%2fth/foo?c=d&a=b",
				"http://example.com/pa%2fth/foo?c=d&a=b");

		// we do want to decode and then resolve encoded dot path segments
		checkCanonicalization("http://example.com/path/%2e%2e/foo?c=d&a=b",
				"http://example.com/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/.%2e/foo?c=d&a=b",
				"http://example.com/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/%2e./foo?c=d&a=b",
				"http://example.com/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/%2e/foo?c=d&a=b",
				"http://example.com/path/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/%2e%2e%2e/foo?c=d&a=b",
				"http://example.com/path/.../foo?c=d&a=b");

		// do not want to decode encoded '?' in path or query
		checkCanonicalization("http://example.com/pa%3fth/foo?c=d&a=b",
				"http://example.com/pa%3fth/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/foo?c=d&a%3f=b",
				"http://example.com/path/foo?c=d&a%3f=b");
		// raw '?' in query should not be encoded(?)
		checkCanonicalization("http://example.com/path/foo?c=d&a?=b",
				"http://example.com/path/foo?c=d&a?=b");

		// do not want to decode encoded '#' in path or query
		checkCanonicalization("http://example.com/pa%23th/foo?c=d&a=b",
				"http://example.com/pa%23th/foo?c=d&a=b");
		checkCanonicalization("http://example.com/path/foo?c=%23d&a=b",
				"http://example.com/path/foo?c=%23d&a=b");

		// do not want to decode encoded '&' in query
		checkCanonicalization("http://example.com/path/foo?c=%26d&a=b",
				"http://example.com/path/foo?c=%26d&a=b");

		// do not want to decode encoded '=' in query
		checkCanonicalization("http://example.com/path/foo?c%3d=d&a=b",
				"http://example.com/path/foo?c%3d=d&a=b");
	}
	
	/*
	 * Some tests copied from
	 * https://developers.google.com/safe-browsing/developers_guide_v2
	 * #Canonicalization but many removed or modified because we've diverged so
	 * much from google.
	 */
	public void testGoogleExamples() throws URISyntaxException  {
		checkCanonicalization("http://host/asdf%25%32%35asd", "http://host/asdf%2525asd");
		checkCanonicalization("http://host/%%%25%32%35asd%%", "http://host/%%%2525asd%%");
		checkCanonicalization("http://www.google.com/", "http://www.google.com/");
		checkCanonicalization("http://%31%36%38%2e%31%38%38%2e%39%39%2e%32%36/%2E%73%65%63%75%72%65/%77%77%77%2E%65%62%61%79%2E%63%6F%6D/", "http://168.188.99.26/.secure/www.ebay.com/");
		checkCanonicalization("http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/", "http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/");
		
		checkCanonicalization("http://3279880203/blah", "http://195.127.0.11/blah");
		checkCanonicalization("http://www.google.com/blah/..", "http://www.google.com/");
		checkCanonicalization("www.google.com/", "http://www.google.com/");
		checkCanonicalization("www.google.com", "http://www.google.com/");
		checkCanonicalization("http://www.evil.com/blah#frag", "http://www.evil.com/blah");
		checkCanonicalization("http://www.GOOgle.com/", "http://www.google.com/");
		checkCanonicalization("http://www.google.com.../", "http://www.google.com/");
		checkCanonicalization("http://www.google.com/foo\tbar\rbaz\n2", "http://www.google.com/foobarbaz2");
		checkCanonicalization("http://www.google.com/q?", "http://www.google.com/q?");
		checkCanonicalization("http://www.google.com/q?r?", "http://www.google.com/q?r?");
		checkCanonicalization("http://www.google.com/q?r?s", "http://www.google.com/q?r?s");
		checkCanonicalization("http://evil.com/foo#bar#baz", "http://evil.com/foo");
		checkCanonicalization("http://evil.com/foo;", "http://evil.com/foo;");
		checkCanonicalization("http://evil.com/foo?bar;", "http://evil.com/foo?bar;");
		checkCanonicalization("http://notrailingslash.com", "http://notrailingslash.com/");
		checkCanonicalization("http://www.gotaport.com:1234/", "http://www.gotaport.com:1234/");
		checkCanonicalization("  http://www.google.com/  ", "http://www.google.com/");
		checkCanonicalization("https://www.securesite.com/", "https://www.securesite.com/");
		checkCanonicalization("http://host.com/ab%23cd", "http://host.com/ab%23cd");
	}
	
	public void testStraySpacing() throws URISyntaxException {
		checkCanonicalization("http://example.org/\u2028", "http://example.org/");
		checkCanonicalization("\nhttp://examp\rle.org/", "http://example.org/");
		checkCanonicalization("\nhttp://examp\u2029\t\rle.org/         ", "http://example.org/");
	}
	
	public void testSchemeCapitalsPreserved() throws URISyntaxException {
		checkCanonicalization("Http://example.com", "Http://example.com/");
		checkCanonicalization("HTTP://example.com", "HTTP://example.com/");
		checkCanonicalization("ftP://example.com", "ftP://example.com/");
	}
	
	public void testUnicodeEscaping() throws URISyntaxException {
		checkCanonicalization("http://example.org/\u2691", "http://example.org/%e2%9a%91");
		checkCanonicalization("http://example.org/%e2%9a%91", "http://example.org/%e2%9a%91");
		checkCanonicalization("http://example.org/blah?x=\u268b", "http://example.org/blah?x=%e2%9a%8b");
		checkCanonicalization("http://example.org/blah?x=%e2%9a%8b", "http://example.org/blah?x=%e2%9a%8b");
		checkCanonicalization("http://example.org/blah?z\u265fz=z\u4e00z", "http://example.org/blah?z%e2%99%9fz=z%e4%b8%80z");
		checkCanonicalization("http://example.org/blah?z%e2%99%9Fz=z%e4%b8%80z", "http://example.org/blah?z%e2%99%9fz=z%e4%b8%80z");
		checkCanonicalization("http://example.org/bl\u2691ah?z\u265fz=z\u4e00z", "http://example.org/bl%e2%9a%91ah?z%e2%99%9fz=z%e4%b8%80z");
		checkCanonicalization("http://example.org/bl%E2%9A%91ah?z%e2%99%9Fz=z%E4%b8%80z", "http://example.org/bl%e2%9a%91ah?z%e2%99%9fz=z%e4%b8%80z");
		// character above u+ffff represented in java with surrogate pair
		checkCanonicalization("http://example.org/\ud83c\udca1", "http://example.org/%f0%9f%82%a1");
		// make sure character above u+ffff survives unescaping and re-escaping
		checkCanonicalization("http://example.org/%F0%9F%82%A1", "http://example.org/%f0%9f%82%a1");
	}

	private void checkCanonicalization(String in, String want) throws URISyntaxException {
		RulesBasedURLCanonicalizerTest.checkCanonicalization(guc, want, in);
	}
}
