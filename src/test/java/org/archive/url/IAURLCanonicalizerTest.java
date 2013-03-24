package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class IAURLCanonicalizerTest extends TestCase {

	public void testFull() throws URISyntaxException {
		AggressiveIAURLCanonicalizer iaC = new AggressiveIAURLCanonicalizer();
		compCan(iaC,"http://www.archive.org:80/","http://archive.org/");
		compCan(iaC,"https://www.archive.org:80/","https://archive.org:80/");
		compCan(iaC,"http://www.archive.org:443/","http://archive.org:443/");
		compCan(iaC,"https://www.archive.org:443/","https://archive.org/");
		compCan(iaC,"http://www.archive.org/big/","http://archive.org/big");
		compCan(iaC,"dns:www.archive.org","dns:www.archive.org");
	}	

	private void compCan(URLCanonicalizer c, String orig, String want) throws URISyntaxException {
		RulesBasedURLCanonicalizerTest.checkCanonicalization(c, want, orig);
	}

	public void testAlphaReorderQuery() {
		assertEquals(null,RulesBasedURLCanonicalizer.alphaReorderQuery(null));
		assertEquals("",RulesBasedURLCanonicalizer.alphaReorderQuery(""));
		assertEquals("a",RulesBasedURLCanonicalizer.alphaReorderQuery("a"));
		assertEquals("ab",RulesBasedURLCanonicalizer.alphaReorderQuery("ab"));
		assertEquals("a=1",RulesBasedURLCanonicalizer.alphaReorderQuery("a=1"));
		assertEquals("ab=1",RulesBasedURLCanonicalizer.alphaReorderQuery("ab=1"));
		assertEquals("&a=1",RulesBasedURLCanonicalizer.alphaReorderQuery("a=1&"));
		assertEquals("a=1&b=1",RulesBasedURLCanonicalizer.alphaReorderQuery("a=1&b=1"));
		assertEquals("a=1&b=1",RulesBasedURLCanonicalizer.alphaReorderQuery("b=1&a=1"));
		assertEquals("a=a&a=a",RulesBasedURLCanonicalizer.alphaReorderQuery("a=a&a=a"));
		assertEquals("a=a&a=b",RulesBasedURLCanonicalizer.alphaReorderQuery("a=b&a=a"));
		assertEquals("a=a&a=b&b=a&b=b",RulesBasedURLCanonicalizer.alphaReorderQuery("b=b&a=b&b=a&a=a"));
	}

	public void testMassageHost() {
		assertEquals("foo.com",RulesBasedURLCanonicalizer.massageHost("foo.com"));
		assertEquals("foo.com",RulesBasedURLCanonicalizer.massageHost("www.foo.com"));
		assertEquals("foo.com",RulesBasedURLCanonicalizer.massageHost("www12.foo.com"));
		assertEquals("www2foo.com",RulesBasedURLCanonicalizer.massageHost("www2foo.com"));
		assertEquals("www2foo.com",RulesBasedURLCanonicalizer.massageHost("www2.www2foo.com"));
	}

	public void testGetDefaultPort() {
		assertEquals(0,RulesBasedURLCanonicalizer.getDefaultPort("foo"));
		assertEquals(80,RulesBasedURLCanonicalizer.getDefaultPort("http"));
		assertEquals(443,RulesBasedURLCanonicalizer.getDefaultPort("https"));
	}

}
