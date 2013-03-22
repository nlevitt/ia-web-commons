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
		checkParse("http://www.archive.org:/", 
				"http", null, null, "www.archive.org", -1, "/", null, null,
				"http://www.archive.org/", "/");
		checkParse("http://www.archive.org:80/", 
				"http", null, null, "www.archive.org", 80, "/", null, null,
				"http://www.archive.org:80/", "/");
		checkParse("http://www.archive.org", 
				"http", null, null, "www.archive.org", -1, "", null, null,
				"http://www.archive.org", "");
		checkParse("http://www.archive.org?",
				"http", null, null, "www.archive.org", -1, "", "", null,
				"http://www.archive.org?", "?");
		checkParse("http://www.archive.org#",
				"http", null, null, "www.archive.org", -1, "", null, "",
				"http://www.archive.org#", "");
		checkParse("http://www.archive.org#foo#bar#baz",
				"http", null, null, "www.archive.org", -1, "", null, "foo#bar#baz",
				"http://www.archive.org#foo#bar#baz", "");
		checkParse("http://www.archive.org:8080/index.html?query#foo",
				"http", null, null, "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://www.archive.org:8080/index.html?#foo",
				"http", null, null, "www.archive.org", 8080, "/index.html", "", "foo",
				"http://www.archive.org:8080/index.html?#foo", "/index.html?");
		checkParse("http://www.archive.org:8080?#foo",
				"http", null, null, "www.archive.org", 8080, "", "", "foo",
				"http://www.archive.org:8080?#foo", "?");
		
		checkParse("http://bücher.ch:8080?#foo",
				"http", null, null, "bücher.ch", 8080, "", "", "foo",
				"http://bücher.ch:8080?#foo", "?");

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
		
		checkParse("http://[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo",
				"http", null, null, "[1234:5555::face:0f:beef:15:f00d:::d00d]", -1, "/ipv6", "mofo", null, 
				"http://[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo", "/ipv6?mofo");
		checkParse("https://[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo",
				"https", null, null, "[1234:5555::face:0f:beef:15:f00d:::d00d]", 8443, "/ipv6", "mofo", null, 
				"https://[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo", "/ipv6?mofo");
		checkParse("http://jdoe@[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo",
				"http", "jdoe", null, "[1234:5555::face:0f:beef:15:f00d:::d00d]", -1, "/ipv6", "mofo", null, 
				"http://jdoe@[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo", "/ipv6?mofo");
		checkParse("http://jdoe:****@[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo",
				"http", "jdoe", "****", "[1234:5555::face:0f:beef:15:f00d:::d00d]", -1, "/ipv6", "mofo", null, 
				"http://jdoe:****@[1234:5555::face:0f:beef:15:f00d:::d00d]/ipv6?mofo", "/ipv6?mofo");
		checkParse("https://jdoe@[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo",
				"https", "jdoe", null, "[1234:5555::face:0f:beef:15:f00d:::d00d]", 8443, "/ipv6", "mofo", null, 
				"https://jdoe@[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo", "/ipv6?mofo");
		checkParse("https://jdoe:****@[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo",
				"https", "jdoe", "****", "[1234:5555::face:0f:beef:15:f00d:::d00d]", 8443, "/ipv6", "mofo", null, 
				"https://jdoe:****@[1234:5555::face:0f:beef:15:f00d:::d00d]:8443/ipv6?mofo", "/ipv6?mofo");
	}

	private void checkParse(String s, String scheme, String authUser,
			String authPass, String host, int port, String path,
			String query, String fragment, String urlString, String pathQuery) throws URISyntaxException {
		HandyURL h = URLParser.parse(s);
		System.out.format("Input:(%s)\nHandyURL\t%s\nURLString(%s)\tAuthority(%s)\tPathQuery(%s)\n",
						s, h.toDebugString(), h.getURLString(), h.getAuthority(), h.getPathQuery());
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
		checkParse("?query#foo",
				null, null, null, null, -1, "", "query", "foo",
				"?query#foo", "?query");
		checkParse("#foo",
				null, null, null, null, -1, "", null, "foo",
				"#foo", "");
		checkParse("funky.path.not.hostname.com///slashes//index.html?query#foo",
				null, null, null, null, -1, "funky.path.not.hostname.com///slashes//index.html", "query", "foo",
				"funky.path.not.hostname.com///slashes//index.html?query#foo", "funky.path.not.hostname.com///slashes//index.html?query");
	}

	public void testOtherSchemes() throws URISyntaxException {
		checkParse("dns:bücher.ch",
				"dns", null, null, null, -1, "bücher.ch", null, null,
				"dns:bücher.ch", "bücher.ch");
		checkParse("file:/tmp/foo.txt",
				"file", null, null, null, -1, "/tmp/foo.txt", null, null,
				"file:/tmp/foo.txt", "/tmp/foo.txt");
		checkParse("file:///tmp/foo.txt",
				"file", null, null, "", -1, "/tmp/foo.txt", null, null,
				"file:///tmp/foo.txt", "/tmp/foo.txt");
		checkParse("urn:uuid:e14814d9-33fe-437b-8c61-012345d74d9a", 
				"urn", null, null, null, -1, "uuid:e14814d9-33fe-437b-8c61-012345d74d9a", null, null, 
				"urn:uuid:e14814d9-33fe-437b-8c61-012345d74d9a", "uuid:e14814d9-33fe-437b-8c61-012345d74d9a");
		checkParse("mailto:username@example.com?subject=Topic",
				"mailto", null, null, null, -1, "username@example.com", "subject=Topic", null, 
				"mailto:username@example.com?subject=Topic", "username@example.com?subject=Topic");
		checkParse("ldap://[2001:db8::7]/c=GB?objectClass?one",
				"ldap", null, null, "[2001:db8::7]", -1, "/c=GB", "objectClass?one", null, 
				"ldap://[2001:db8::7]/c=GB?objectClass?one", "/c=GB?objectClass?one");
		checkParse("news:comp.infosystems.www.serers.unix",
				"news", null, null, null, -1, "comp.infosystems.www.serers.unix", null, null, 
				"news:comp.infosystems.www.serers.unix", "comp.infosystems.www.serers.unix");
		checkParse("tel:+1-816-555-1212",
				"tel", null, null, null, -1, "+1-816-555-1212", null, null, 
				"tel:+1-816-555-1212", "+1-816-555-1212");
		checkParse("telnet://192.0.2.16:80/",
				"telnet", null, null, "192.0.2.16", 80, "/", null, null, 
				"telnet://192.0.2.16:80/", "/");
		checkParse("bitcoin:blahblah?amount=123", 
				"bitcoin", null, null, null, -1, "blahblah", "amount=123", null, 
				"bitcoin:blahblah?amount=123", "blahblah?amount=123");
	}
}
