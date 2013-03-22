package org.archive.url;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URIException;

import com.google.common.net.InetAddresses;

public class URLParserTest extends TestCase {
	public void testGuava() throws URIException, UnsupportedEncodingException {
		Long l = Long.parseLong("3279880203");
		int i2 = l.intValue();
		//		int i = Integer.decode("3279880203");
		System.err.format("FromNum(%s)\n", InetAddresses.fromInteger(i2).getHostAddress());
	}

	public void testAddDefaultSchemeIfNeeded() {
		assertEquals(null,URLParser.addDefaultSchemeIfNeeded(null));
		assertEquals("http://",URLParser.addDefaultSchemeIfNeeded(""));
		assertEquals("http://www.fool.com",URLParser.addDefaultSchemeIfNeeded("http://www.fool.com"));
		assertEquals("http://www.fool.com/",URLParser.addDefaultSchemeIfNeeded("http://www.fool.com/"));
		assertEquals("http://www.fool.com",URLParser.addDefaultSchemeIfNeeded("www.fool.com"));
		assertEquals("http://www.fool.com/",URLParser.addDefaultSchemeIfNeeded("www.fool.com/"));
	}

	public void testTrim() {
		 assertEquals("blahblah", URLParser.trim("blahblah"));
		 assertEquals("blahblah", URLParser.trim(" blahblah"));
		 assertEquals("blahblah", URLParser.trim("  \u00a0     blahblah"));
		 assertEquals("blahblah", URLParser.trim("  \t     blahblah"));
		 assertEquals("blahblah", URLParser.trim("  \t  \r   blahblah"));
		 assertEquals("blahblah", URLParser.trim("  \t \n\u0000 \r   blahblah"));
		 assertEquals("blahblah", URLParser.trim(" \u0004 \t \n\u0000 \r   blahblah"));
		 assertEquals("blahblah", URLParser.trim(" \u0004 \t \n\u0000 \r   blahblah "));
		 assertEquals("blahblah", URLParser.trim(" \u0004 \t \n\u0000 \r   blahblah \u00a0"));
		 assertEquals("bla h b lah", URLParser.trim(" \u0004 \t \n\u0000 \r   bla h b lah \u00a0"));
		 assertEquals("bla h\u00a0b lah", URLParser.trim(" \u0004 \t \n\u0000 \r   bla h\u00a0b lah \u00a0"));
		 assertEquals("bla h\u00a0b lah", URLParser.trim(" \u0004 \t \n\u0000 \r   bla h\u00a0b lah \u00a0 \t \n\u0000 \r "));
		 assertEquals("", URLParser.trim(""));
		 assertEquals("", URLParser.trim(" "));
		 assertEquals("", URLParser.trim(" \u0004 \t \n\u0000 \r  \u00a0 \t \n\u0000 \r "));

	}
	
	public void testParse() throws UnsupportedEncodingException, URISyntaxException {
		System.out.format("O(%s) E(%s)\n","%66",URLDecoder.decode("%66","UTF-8"));
		checkParse("http://www.archive.org/index.html#foo", 
				"http", null, null, "www.archive.org", -1, "/index.html", null, "foo",
				"http://www.archive.org/index.html#foo", "/index.html");
		checkParse("http://www.archive.org/", 
				"http", null, null, "www.archive.org", -1, "/", null, null,
				"http://www.archive.org/", "/");
		checkParse("http://www.archive.org", 
				"http", null, null, "www.archive.org", -1, "", null, null,
				"http://www.archive.org", "");
		checkParse("http://www.archive.org?",
				"http", null, null, "www.archive.org", -1, "", "", null,
				"http://www.archive.org/?", "/?");
		checkParse("http://www.archive.org#",
				"http", null, null, "www.archive.org", -1, "", null, "",
				"http://www.archive.org/#", "/");
		checkParse("http://www.archive.org#foo#bar#baz",
				"http", null, null, "www.archive.org", -1, "", null, "foo#bar#baz",
				"http://www.archive.org/#foo#bar#baz", "/");
		checkParse("http://www.archive.org:8080/index.html?query#foo",
				"http", null, null, "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://www.archive.org:8080/index.html?#foo",
				"http", null, null, "www.archive.org", 8080, "/index.html", "", "foo",
				"http://www.archive.org:8080/index.html?#foo", "/index.html?");
		checkParse("http://www.archive.org:8080?#foo",
				"http", null, null, "www.archive.org", 8080, "", "", "foo",
				"http://www.archive.org:8080/?#foo", "/?");
		checkParse("http://bücher.ch:8080?#foo",
				"http", null, null, "bücher.ch", 8080, "", "", "foo",
				"http://bücher.ch:8080/?#foo", "/?");

		checkParse("dns:bücher.ch",
				"dns", null, null, null, -1, "bücher.ch", null, null,
				"dns:bücher.ch", "bücher.ch");

		checkParse("http://www.archive.org/?foo?what", 
				"http", null, null, "www.archive.org", -1, "/", "foo?what", null,
				"http://www.archive.org/?foo?what", "/?foo?what");
		checkParse("http://www.archive.org/?foo?what#spuz?baz?", 
				"http", null, null, "www.archive.org", -1, "/", "foo?what", "spuz?baz?",
				"http://www.archive.org/?foo?what#spuz?baz?", "/?foo?what");
		checkParse("http://www.archive.org/?foo?what#spuz?baz?#fooo", 
				"http", null, null, "www.archive.org", -1, "/", "foo?what", "spuz?baz?#fooo",
				"http://www.archive.org/?foo?what#spuz?baz?#fooo", "/?foo?what");
		checkParse("http://jdoe@www.archive.org:8080/index.html?query#foo",
				"http", "jdoe", null, "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://jdoe@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://jdoe:****@www.archive.org:8080/index.html?query#foo",
				"http", "jdoe", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://jdoe:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://:****@www.archive.org:8080/index.html?query#foo",
				"http", "", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse(" \n http://:****@www.archive.org:8080/inde\rx.html?query#foo \r\n \t ",
				"http", "", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
	}

	private void checkParse(String s, String scheme, String authUser,
			String authPass, String host, int port, String path,
			String query, String fragment, String urlString, String pathQuery) throws URISyntaxException {
		HandyURL h = URLParser.parse(s);
		System.out.format("Input:(%s)\nHandyURL\t%s\n",s,h.toDebugString());
		assertEquals(scheme, h.getScheme());
		assertEquals(authUser, h.getAuthUser());
		assertEquals(authPass, h.getAuthPass());
		assertEquals(host, h.getHost());
		assertEquals(port, h.getPort());
		assertEquals(path, h.getPath());
		assertEquals(query, h.getQuery());
		assertEquals(fragment, h.getFragment());

		assertEquals(urlString, h.getURLString());
		assertEquals(pathQuery, h.getPathQuery());
	}

	public void testRelative() throws URISyntaxException {
		checkParse("/server-relative", 
				null, null, null, null, -1, "/server-relative", null, null,
				"/server-relative", "/server-relative");
		checkParse("relative", 
				null, null, null, null, -1, "relative", null, null,
				"relative", "relative");
		checkParse("//jdoe:****@www.archive.org:8080/index.html?query#foo",
				null, "jdoe", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"//jdoe:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("/index.html?query#foo",
				null, null, null, null, -1, "/index.html", "query", "foo",
				"/index.html?query#foo", "/index.html?query");
		checkParse("index.html?query#foo",
				null, null, null, null, -1, "index.html", "query", "foo",
				"index.html?query#foo", "index.html?query");
		checkParse("funky.path.not.hostname.com///slashes//index.html?query#foo",
				null, null, null, null, -1, "funky.path.not.hostname.com///slashes//index.html", "query", "foo",
				"funky.path.not.hostname.com///slashes//index.html?query#foo", "funky.path.not.hostname.com///slashes//index.html?query");
	}
	
	public void testFunky() throws URISyntaxException {
		checkParse("bitcoin:blahblah?amount=123", 
				"bitcoin", null, null, null, -1, "blahblah", "amount=123", null, 
				"bitcoin:blahblah?amount=123", "blahblah?amount=123");
	}
}
