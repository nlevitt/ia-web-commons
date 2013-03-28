package org.archive.url;

import java.io.UnsupportedEncodingException;
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
	 * https://developers.google.com/safe-browsing/developers_guide_v2#Canonicalization 
	 * but many removed or modified because we've diverged so
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
	
	public void testSchemeLowercase() throws URISyntaxException {
		checkCanonicalization("Http://example.com", "http://example.com/");
		checkCanonicalization("HTTP://example.com", "http://example.com/");
		checkCanonicalization("ftP://example.com", "ftp://example.com/");
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
	
	public void testIDN() throws URISyntaxException, UnsupportedEncodingException {
		checkCanonicalization("http://\u2691.com/foo", "http://xn--p7h.com/foo");
		checkCanonicalization("http://%e2%9a%91.com/foo", "http://xn--p7h.com/foo");

		// example hosts from http://idn.icann.org/
		checkCanonicalization("http://\u0645\u062B\u0627\u0644.\u0625\u062E\u062A\u0628\u0627\u0631",  "http://xn--mgbh0fb.xn--kgbechtv/");
		checkCanonicalization("http://%D9%85%D8%AB%D8%A7%D9%84.%D8%A5%D8%AE%D8%AA%D8%A8%D8%A7%D8%B1",  "http://xn--mgbh0fb.xn--kgbechtv/");
		checkCanonicalization("http://\u4F8B\u5B50.\u6D4B\u8BD5",  "http://xn--fsqu00a.xn--0zwm56d/");
		checkCanonicalization("http://%E4%BE%8B%E5%AD%90.%E6%B5%8B%E8%AF%95",  "http://xn--fsqu00a.xn--0zwm56d/");
		checkCanonicalization("http://\u4F8B\u5B50.\u6E2C\u8A66",  "http://xn--fsqu00a.xn--g6w251d/");
		checkCanonicalization("http://%E4%BE%8B%E5%AD%90.%E6%B8%AC%E8%A9%A6",  "http://xn--fsqu00a.xn--g6w251d/");
		checkCanonicalization("http://\u03C0\u03B1\u03C1\u03AC\u03B4\u03B5\u03B9\u03B3\u03BC\u03B1.\u03B4\u03BF\u03BA\u03B9\u03BC\u03AE",  "http://xn--hxajbheg2az3al.xn--jxalpdlp/");
		checkCanonicalization("http://%CF%80%CE%B1%CF%81%CE%AC%CE%B4%CE%B5%CE%B9%CE%B3%CE%BC%CE%B1.%CE%B4%CE%BF%CE%BA%CE%B9%CE%BC%CE%AE",  "http://xn--hxajbheg2az3al.xn--jxalpdlp/");
		checkCanonicalization("http://\u0909\u0926\u093E\u0939\u0930\u0923.\u092A\u0930\u0940\u0915\u094D\u0937\u093E",  "http://xn--p1b6ci4b4b3a.xn--11b5bs3a9aj6g/");
		checkCanonicalization("http://%E0%A4%89%E0%A4%A6%E0%A4%BE%E0%A4%B9%E0%A4%B0%E0%A4%A3.%E0%A4%AA%E0%A4%B0%E0%A5%80%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%BE",  "http://xn--p1b6ci4b4b3a.xn--11b5bs3a9aj6g/");
		checkCanonicalization("http://\u4F8B\u3048.\u30C6\u30B9\u30C8",  "http://xn--r8jz45g.xn--zckzah/");
		checkCanonicalization("http://%E4%BE%8B%E3%81%88.%E3%83%86%E3%82%B9%E3%83%88",  "http://xn--r8jz45g.xn--zckzah/");
		checkCanonicalization("http://\uC2E4\uB840.\uD14C\uC2A4\uD2B8",  "http://xn--9n2bp8q.xn--9t4b11yi5a/");
		checkCanonicalization("http://%EC%8B%A4%EB%A1%80.%ED%85%8C%EC%8A%A4%ED%8A%B8",  "http://xn--9n2bp8q.xn--9t4b11yi5a/");
		checkCanonicalization("http://\u0645\u062B\u0627\u0644.\u0622\u0632\u0645\u0627\u06CC\u0634\u06CC",  "http://xn--mgbh0fb.xn--hgbk6aj7f53bba/");
		checkCanonicalization("http://%D9%85%D8%AB%D8%A7%D9%84.%D8%A2%D8%B2%D9%85%D8%A7%DB%8C%D8%B4%DB%8C",  "http://xn--mgbh0fb.xn--hgbk6aj7f53bba/");
		checkCanonicalization("http://\u043F\u0440\u0438\u043C\u0435\u0440.\u0438\u0441\u043F\u044B\u0442\u0430\u043D\u0438\u0435",  "http://xn--e1afmkfd.xn--80akhbyknj4f/");
		checkCanonicalization("http://%D0%BF%D1%80%D0%B8%D0%BC%D0%B5%D1%80.%D0%B8%D1%81%D0%BF%D1%8B%D1%82%D0%B0%D0%BD%D0%B8%D0%B5",  "http://xn--e1afmkfd.xn--80akhbyknj4f/");
		checkCanonicalization("http://\u0B89\u0BA4\u0BBE\u0BB0\u0BA3\u0BAE\u0BCD.\u0BAA\u0BB0\u0BBF\u0B9F\u0BCD\u0B9A\u0BC8",  "http://xn--zkc6cc5bi7f6e.xn--hlcj6aya9esc7a/");
		checkCanonicalization("http://%E0%AE%89%E0%AE%A4%E0%AE%BE%E0%AE%B0%E0%AE%A3%E0%AE%AE%E0%AF%8D.%E0%AE%AA%E0%AE%B0%E0%AE%BF%E0%AE%9F%E0%AF%8D%E0%AE%9A%E0%AF%88",  "http://xn--zkc6cc5bi7f6e.xn--hlcj6aya9esc7a/");
		checkCanonicalization("http://\u05D1\u05F2\u05B7\u05E9\u05E4\u05BC\u05D9\u05DC.\u05D8\u05E2\u05E1\u05D8",  "http://xn--fdbk5d8ap9b8a8d.xn--deba0ad/");
		checkCanonicalization("http://%D7%91%D7%B2%D6%B7%D7%A9%D7%A4%D6%BC%D7%99%D7%9C.%D7%98%D7%A2%D7%A1%D7%98",  "http://xn--fdbk5d8ap9b8a8d.xn--deba0ad/");
		checkCanonicalization("http://\u12A0\u121B\u122D\u129B.idn.icann.org",  "http://xn--1xd0bwwra.idn.icann.org/");
		checkCanonicalization("http://%E1%8A%A0%E1%88%9B%E1%88%AD%E1%8A%9B.idn.icann.org",  "http://xn--1xd0bwwra.idn.icann.org/");
		checkCanonicalization("http://\u09AC\u09BE\u0982\u09B2\u09BE.idn.icann.org",  "http://xn--54b7fta0cc.idn.icann.org/");
		checkCanonicalization("http://%E0%A6%AC%E0%A6%BE%E0%A6%82%E0%A6%B2%E0%A6%BE.idn.icann.org",  "http://xn--54b7fta0cc.idn.icann.org/");
		checkCanonicalization("http://\u05E2\u05D1\u05E8\u05D9\u05EA.idn.icann.org",  "http://xn--5dbqzzl.idn.icann.org/");
		checkCanonicalization("http://%D7%A2%D7%91%D7%A8%D7%99%D7%AA.idn.icann.org",  "http://xn--5dbqzzl.idn.icann.org/");
		checkCanonicalization("http://\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A.idn.icann.org",  "http://xn--j2e7beiw1lb2hqg.idn.icann.org/");
		checkCanonicalization("http://%E1%9E%97%E1%9E%B6%E1%9E%9F%E1%9E%B6%E1%9E%81%E1%9F%92%E1%9E%98%E1%9F%82%E1%9E%9A.idn.icann.org",  "http://xn--j2e7beiw1lb2hqg.idn.icann.org/");
		checkCanonicalization("http://\u0E44\u0E17\u0E22.idn.icann.org",  "http://xn--o3cw4h.idn.icann.org/");
		checkCanonicalization("http://%E0%B9%84%E0%B8%97%E0%B8%A2.idn.icann.org",  "http://xn--o3cw4h.idn.icann.org/");
		checkCanonicalization("http://\u0627\u0631\u062F\u0648.idn.icann.org",  "http://xn--mgbqf7g.idn.icann.org/");
		checkCanonicalization("http://%D8%A7%D8%B1%D8%AF%D9%88.idn.icann.org",  "http://xn--mgbqf7g.idn.icann.org/");
	}
	
	public void testBackslash() throws URISyntaxException {
		checkCanonicalization("http://example.com/foo\\bar?baz", "http://example.com/foo/bar?baz");
		checkCanonicalization("http://example.com/foo%5cbar?baz", "http://example.com/foo%5cbar?baz");
		checkCanonicalization("http://example.com/foo?bar\\baz", "http://example.com/foo?bar\\baz");
		checkCanonicalization("http://example.com/foo?bar%5cbaz", "http://example.com/foo?bar%5cbaz");
	}

	private void checkCanonicalization(String in, String want) throws URISyntaxException {
		RulesBasedURLCanonicalizerTest.checkCanonicalization(guc, want, in);
	}
}
