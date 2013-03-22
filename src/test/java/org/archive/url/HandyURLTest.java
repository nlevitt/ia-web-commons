package org.archive.url;

import junit.framework.TestCase;

public class HandyURLTest extends TestCase {

	public void testGetPublicSuffix() {
		HandyURL h = new HandyURL();
		h.setHost("www.fool.com");
		assertEquals("fool.com",h.getPublicSuffix());
		assertEquals("www",h.getPublicPrefix());

		h.setHost("www.amazon.co.uk");
		assertEquals("amazon.co.uk",h.getPublicSuffix());
		assertEquals("www",h.getPublicPrefix());

		h.setHost("www.images.amazon.co.uk");
		assertEquals("amazon.co.uk",h.getPublicSuffix());
		assertEquals("www.images",h.getPublicPrefix());

		h.setHost("funky-images.fancy.co.jp");
		assertEquals("fancy.co.jp",h.getPublicSuffix());
		assertEquals("funky-images",h.getPublicPrefix());
	}

	public void testGetUrlString() {
		HandyURL h = new HandyURL("https", "jdoe", "*****",
				"www24.us.archive.org", 1443, "/foo", "x=y&a=b", "hashish");
		assertEquals("https://jdoe:*****@www24.us.archive.org:1443/foo?x=y&a=b#hashish", h.getURLString());
		assertEquals("jdoe:*****@www24.us.archive.org:1443/foo?x=y&a=b#hashish", h.getURLString(false, false, false));
		assertEquals("jdoe:*****@archive.org:1443/foo?x=y&a=b#hashish", h.getURLString(false, false, true));
		assertEquals("https://jdoe:*****@www24.us.archive.org:1443/foo?x=y&a=b#hashish", h.getURLString(false, true, false));
		assertEquals("https://jdoe:*****@archive.org:1443/foo?x=y&a=b#hashish", h.getURLString(false, true, true));
		assertEquals("org,archive,us,www24,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getURLString(true, false, false));
		assertEquals("org,archive,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getURLString(true, false, true));
		assertEquals("https://(org,archive,us,www24,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getURLString(true, true, false));
		assertEquals("https://(org,archive,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getURLString(true, true, true));
		assertEquals("org,archive,us,www24,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(false));
		assertEquals("https://(org,archive,us,www24,:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(true));
		assertEquals(SURT.toSURT(h.getURLString()) + "#hashish", h.getSURTString(true));

		h = new HandyURL("https", "jdoe", "*****",
				"192.168.99.99", 1443, "/foo", "x=y&a=b", "hashish");
		assertEquals("192.168.99.99:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(false));
		assertEquals("https://(192.168.99.99:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(true));
		assertEquals(SURT.toSURT(h.getURLString()) + "#hashish", h.getSURTString(true));
		
		h = new HandyURL("https", "jdoe", "*****",
				"[1234:5555::face:0f:beef:15:f00d:::d00d]", 1443, "/foo", "x=y&a=b", "hashish");
		assertEquals("[1234:5555::face:0f:beef:15:f00d:::d00d]:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(false));
		assertEquals("https://([1234:5555::face:0f:beef:15:f00d:::d00d]:1443@jdoe:*****)/foo?x=y&a=b#hashish", h.getSURTString(true));
	}

}
