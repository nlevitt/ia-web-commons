package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class AggressiveIAURLCanonicalizerTest extends TestCase {
	static AggressiveIAURLCanonicalizer ia = new AggressiveIAURLCanonicalizer();
	public void testCanonicalize() throws URISyntaxException {
		// FULL end-to-end tests:
		check("http://www.alexa.com/","http://alexa.com/");
		check("http://archive.org/index.html","http://archive.org/index.html");
		check("http://archive.org/index.html?","http://archive.org/index.html");
		check("http://archive.org/index.html?a=b","http://archive.org/index.html?a=b");
		check("http://archive.org/index.html?b=b&a=b","http://archive.org/index.html?a=b&b=b");
		check("http://archive.org/index.html?b=a&b=b&a=b","http://archive.org/index.html?a=b&b=a&b=b");
		check("http://www34.archive.org/index.html?b=a&b=b&a=b","http://archive.org/index.html?a=b&b=a&b=b");
		check("www34.archive.org/index.html?b=a&b=b&a=b","http://archive.org/index.html?a=b&b=a&b=b");
	}

	public void testWaybackSurtCanon() throws URISyntaxException {
		checkWaybackSurt("dns:example.com", "dns:example.com");
	}
	
	protected void checkWaybackSurt(String orig, String want) throws URISyntaxException {
		HandyURL handy = URLParser.parse(orig, false);
		ia.canonicalize(handy);
		String got = handy.getSURTString(false);
		assertEquals(want, got);
	}

	private static void check(String orig, String want) throws URISyntaxException {
		RulesBasedURLCanonicalizerTest.checkCanonicalization(ia, want, orig);
	}
}
