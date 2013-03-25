package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URIException;

public class RulesBasedURLCanonicalizerTest extends TestCase {
	private final RulesBasedURLCanonicalizer guc = new RulesBasedURLCanonicalizer() {
		@Override
		protected CanonicalizeRules buildRules() {
			return new CanonicalizeRules();
		}
	};
	
	public void testGetHex() {
		assertEquals(0,guc.getHex('0'));
		assertEquals(1,guc.getHex('1'));
		assertEquals(2,guc.getHex('2'));
		assertEquals(3,guc.getHex('3'));
		assertEquals(4,guc.getHex('4'));
		assertEquals(5,guc.getHex('5'));
		assertEquals(6,guc.getHex('6'));
		assertEquals(7,guc.getHex('7'));
		assertEquals(8,guc.getHex('8'));
		assertEquals(9,guc.getHex('9'));
		assertEquals(10,guc.getHex('a'));
		assertEquals(11,guc.getHex('b'));
		assertEquals(12,guc.getHex('c'));
		assertEquals(13,guc.getHex('d'));
		assertEquals(14,guc.getHex('e'));
		assertEquals(15,guc.getHex('f'));
		assertEquals(10,guc.getHex('A'));
		assertEquals(11,guc.getHex('B'));
		assertEquals(12,guc.getHex('C'));
		assertEquals(13,guc.getHex('D'));
		assertEquals(14,guc.getHex('E'));
		assertEquals(15,guc.getHex('F'));
		assertEquals(-1,guc.getHex('G'));
		assertEquals(-1,guc.getHex('G'));
		assertEquals(-1,guc.getHex('q'));
		assertEquals(-1,guc.getHex(' '));
	}
	
	public void testUnescape() {
		assertEquals("A",guc.unescape("A"));
		assertEquals("AA",guc.unescape("AA"));
		assertEquals("Aa",guc.unescape("Aa"));
		assertEquals("aA",guc.unescape("aA"));
		assertEquals("%aq",guc.unescape("%aq"));
		assertEquals("%aQ",guc.unescape("%aQ"));
		assertEquals("\n",guc.unescape("%0a"));
		assertEquals("\t",guc.unescape("%09"));
		
		assertEquals("}",guc.unescape("%7d"));
		
		// don't touch escaped reserved characters
		assertEquals("%3a",guc.unescape("%3a")); // ':'
		assertEquals("%2F",guc.unescape("%2F")); // '/'
		assertEquals("%3f",guc.unescape("%3f")); // '?'
		assertEquals("%23",guc.unescape("%23")); // '#'
		assertEquals("%5b",guc.unescape("%5b")); // '['
		assertEquals("%5d",guc.unescape("%5d")); // ']'
		assertEquals("%40",guc.unescape("%40")); // '@'
		assertEquals("%21",guc.unescape("%21")); // '!'
		assertEquals("%24",guc.unescape("%24")); // '$'
		assertEquals("%26",guc.unescape("%26")); // '&'
		assertEquals("%27",guc.unescape("%27")); // '\''
		assertEquals("%28",guc.unescape("%28")); // '('
		assertEquals("%29",guc.unescape("%29")); // ')'
		assertEquals("%2a",guc.unescape("%2a")); // '*'
		assertEquals("%2B",guc.unescape("%2B")); // '+'
		assertEquals("%2c",guc.unescape("%2c")); // ','
		assertEquals("%3b",guc.unescape("%3b")); // ';'
		assertEquals("%3d",guc.unescape("%3d")); // '='

		assertEquals("||",guc.unescape("%7c%7c"));
		assertEquals("A||",guc.unescape("A%7c%7C"));
		assertEquals("|A||",guc.unescape("|A%7C%7c"));
		assertEquals("|A||",guc.unescape("%7cA%7c%7C"));
		assertEquals("%|A||",guc.unescape("%|A%7c%7c"));
		assertEquals("%|A||%",guc.unescape("%|A%7c%7c%"));
		assertEquals("%|A||%5",guc.unescape("%|A%7c%7c%5"));
		assertEquals("%|A||%25",guc.unescape("%|A%7c%7c%25"));

		// unicode
		assertEquals("\u2691", guc.unescape("%E2%9A%91"));
		assertEquals("\u2691x", guc.unescape("%E2%9A%91x"));
		assertEquals("\u2691xx", guc.unescape("%E2%9A%91xx"));
		assertEquals("\u2691xxx", guc.unescape("%E2%9A%91xxx"));
		assertEquals("x\u2691", guc.unescape("x%E2%9A%91"));
		assertEquals("xx\u2691", guc.unescape("xx%E2%9A%91"));
		assertEquals("xxx\u2691", guc.unescape("xxx%E2%9A%91"));
		assertEquals("\u2691\u2691", guc.unescape("%E2%9A%91%E2%9A%91"));
		assertEquals("\u2691x\u2691", guc.unescape("%E2%9A%91x%E2%9A%91"));
		assertEquals("\u2691xx\u2691", guc.unescape("%E2%9A%91xx%E2%9A%91"));
		assertEquals("\u2691xxx\u2691", guc.unescape("%E2%9A%91xxx%E2%9A%91"));
		assertEquals("\u2691x\u2691x", guc.unescape("%E2%9A%91x%E2%9A%91x"));
		assertEquals("\u2691x\u2691xx", guc.unescape("%E2%9A%91x%E2%9A%91xx"));
		assertEquals("\u2691x\u2691xxx", guc.unescape("%E2%9A%91x%E2%9A%91xxx"));
		assertEquals("\u2691xx\u2691x", guc.unescape("%E2%9A%91xx%E2%9A%91x"));
		assertEquals("\u2691xx\u2691xx", guc.unescape("%E2%9A%91xx%E2%9A%91xx"));
		assertEquals("\u2691xx\u2691xxx", guc.unescape("%E2%9A%91xx%E2%9A%91xxx"));
		assertEquals("\u2691xxx\u2691%", guc.unescape("%E2%9A%91xxx%E2%9A%91%"));
		assertEquals("\u2691xxx\u2691%a", guc.unescape("%E2%9A%91xxx%E2%9A%91%a"));
		assertEquals("\u2691xxx\u2691%a%", guc.unescape("%E2%9A%91xxx%E2%9A%91%a%"));
		// character above u+ffff represented in java with surrogate pair
		assertEquals("\ud83c\udca1", guc.unescape("%F0%9F%82%A1"));
		assertEquals("\ud83c\udca1x", guc.unescape("%F0%9F%82%A1x"));
		assertEquals("x\ud83c\udca1", guc.unescape("x%F0%9F%82%A1"));
		assertEquals("x\ud83c\udca1x", guc.unescape("x%F0%9F%82%A1x"));
		assertEquals("x\ud83c\udca1xx", guc.unescape("x%F0%9F%82%A1xx"));
		assertEquals("x\ud83c\udca1xxx", guc.unescape("x%F0%9F%82%A1xxx"));
		
		// mixing unicode and non-unicode
		assertEquals("|\u2691",guc.unescape("%7c%E2%9A%91"));
		assertEquals("|\u2691x",guc.unescape("%7c%E2%9A%91x"));
		assertEquals("|\u2691xx",guc.unescape("%7c%E2%9A%91xx"));
		assertEquals("|\u2691xxx",guc.unescape("%7c%E2%9A%91xxx"));
		assertEquals("\u2691|",guc.unescape("%E2%9A%91%7c"));
		assertEquals("\u2691|x",guc.unescape("%E2%9A%91%7cx"));
		assertEquals("\u2691|xx",guc.unescape("%E2%9A%91%7cxx"));
		assertEquals("\u2691|xxx",guc.unescape("%E2%9A%91%7cxxx"));
		
		// bad unicode and mixtures
		assertEquals("%E2%9A", guc.unescape("%E2%9A"));
		assertEquals("%E2%9Ax", guc.unescape("%E2%9Ax"));
		assertEquals("%E2%9Axx", guc.unescape("%E2%9Axx"));
		assertEquals("%E2%9Axxx", guc.unescape("%E2%9Axxx"));
		assertEquals("x%E2%9A", guc.unescape("x%E2%9A"));
		assertEquals("xx%E2%9A", guc.unescape("xx%E2%9A"));
		assertEquals("xxx%E2%9A", guc.unescape("xxx%E2%9A"));
		assertEquals("%E2x%9A", guc.unescape("%E2x%9A"));
		assertEquals("%E2|%9A", guc.unescape("%E2%7c%9A"));
		assertEquals("\u2691%E2%9A", guc.unescape("%E2%9A%91%E2%9A"));
		assertEquals("\u2691%E2%9Ax", guc.unescape("%E2%9A%91%E2%9Ax"));
		assertEquals("\u2691%E2%9Axx", guc.unescape("%E2%9A%91%E2%9Axx"));
		assertEquals("\u2691%E2%9Axxx", guc.unescape("%E2%9A%91%E2%9Axxx"));
		assertEquals("x\u2691%E2%9A", guc.unescape("x%E2%9A%91%E2%9A"));
		assertEquals("xx\u2691%E2%9A", guc.unescape("xx%E2%9A%91%E2%9A"));
		assertEquals("xxx\u2691%E2%9A", guc.unescape("xxx%E2%9A%91%E2%9A"));
		assertEquals("%E2%9A\u2691", guc.unescape("%E2%9A%E2%9A%91"));
		assertEquals("%E2%9A\u2691x", guc.unescape("%E2%9A%E2%9A%91x"));
		assertEquals("%E2%9A\u2691xx", guc.unescape("%E2%9A%E2%9A%91xx"));
		assertEquals("%E2%9A\u2691xxx", guc.unescape("%E2%9A%E2%9A%91xxx"));
		assertEquals("\u2691%E2%9A\u2691%E2%9A", guc.unescape("%E2%9A%91%E2%9A%E2%9A%91%E2%9A"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Ax", guc.unescape("%E2%9A%91%E2%9A%E2%9A%91%E2%9Ax"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Axx", guc.unescape("%E2%9A%91%E2%9A%E2%9A%91%E2%9Axx"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Axxx", guc.unescape("%E2%9A%91%E2%9A%E2%9A%91%E2%9Axxx"));
		assertEquals("%E2%9A|\u2691", guc.unescape("%E2%9A%7c%E2%9A%91"));
		assertEquals("%E2%9A\u2691|x", guc.unescape("%E2%9A%E2%9A%91%7cx"));
		assertEquals("%E2|%9A\u2691xx", guc.unescape("%E2%7c%9A%E2%9A%91xx"));
		assertEquals("%E2%9A\u2691xxx", guc.unescape("%E2%9A%E2%9A%91xxx"));
		assertEquals("\u2691%E2%9A|\u2691%E2%9A", guc.unescape("%E2%9A%91%E2%9A%7c%E2%9A%91%E2%9A"));
		
		assertEquals("%|A||%25",guc.unescape("%|A%7c%7c%25"));
		assertEquals("%",guc.unescape("%"));
		assertEquals("%2",guc.unescape("%2"));
		assertEquals("%25",guc.unescape("%25"));
		assertEquals("%25%",guc.unescape("%25%"));
		assertEquals("%2525",guc.unescape("%2525"));
		assertEquals("%252525",guc.unescape("%252525"));
		assertEquals("%2525",guc.unescape("%25%32%35"));
		
		assertEquals("168.188.99.26",guc.unescape("%31%36%38%2e%31%38%38%2e%39%39%2e%32%36"));
	}
	
	public void testAttemptIPv4Formats() throws URIException {
		assertEquals(null,guc.attemptIPv4Formats(null));
		assertEquals(null,guc.attemptIPv4Formats("www.foo.com"));
		assertEquals("127.0.0.1",guc.attemptIPv4Formats("127.0.0.1"));
		assertEquals("15.0.0.1",guc.attemptIPv4Formats("017.0.0.1"));
		assertEquals("168.188.99.26",guc.attemptIPv4Formats("168.188.99.26"));
		
		// TODO: should flesh these out. No IPv6 tests..
		/*
		 * These test may not be complete. Specifically there is mention in the
		 * spec that "partial" IP addresses should be handled. One non-googler
		 * suggested the following:
		 *     
		 *     http://10.9
		 *     http://10.0.0.011
		 *     http://10.0.0.0x09
		 *     http://10.0.0.9/#link
		 *     
		 * Are all equivalent to:
		 * 
		 *     http://10.0.0.9/
		 * 
		 * Further mention, from page 8 of:
		 * 
		 *    http://tools.ietf.org/html/draft-iab-identifier-comparison-00
		 *

   In specifying the inet_addr() API, the POSIX standard [IEEE-1003.1]
   defines "IPv4 dotted decimal notation" as allowing not only strings
   of the form "10.0.1.2", but also allows octal and hexadecimal, and
   addresses with less than four parts.  For example, "10.0.258",
   "0xA000001", and "012.0x102" all represent the same IPv4 address in
   standard "IPv4 dotted decimal" notation.  We will refer to this as
   the "loose" syntax of an IPv4 address literal.

		 * 
		 * I found few other examples of partial ports in a semi-quick google
		 * search.. should verify in a browser and add them as tests..
		 * 
		 *  For now, we'll enforce some strictness:
		 */

		assertEquals(null,guc.attemptIPv4Formats("10.0.258"));
		assertEquals(null,guc.attemptIPv4Formats("1.2.3.256"));
		
	}
		
	public void xestFoo() {
		String path = "/a/b/c/";
		String[] paths = path.split("/",-1);
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
		paths = path.split("/");
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
		
		path = "/a/b/c";
		paths = path.split("/",-1);
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
		paths = path.split("/");
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
	}
	

	public void testNormalizePath() {
		assertEquals("/a/b/c/", guc.normalizePath("/a/b/c/"));
		assertEquals("/a/b/c/", guc.normalizePath("/a/b/c/."));
//		assertEquals("/a/b/c/", guc.normalizePath("/a/b/c//."));
//		assertEquals("/a/b/c/", guc.normalizePath("/a/b/c/////."));
		assertEquals("/a/b/", guc.normalizePath("/a/b/c/.."));
		assertEquals("/a/c", guc.normalizePath("/a/b/../c"));
		assertEquals("/a/c/", guc.normalizePath("/a/b/../c/"));
		assertEquals("/", guc.normalizePath("/../"));
		assertEquals("/", guc.normalizePath("/.."));
		assertEquals("/", guc.normalizePath("/./"));
		assertEquals("/", guc.normalizePath("/."));
		assertEquals("/", guc.normalizePath("/a/../"));
		assertEquals("/", guc.normalizePath("/a/.."));
		assertEquals("/a/", guc.normalizePath("/a/./"));
		assertEquals("/a/", guc.normalizePath("/a/."));
//		assertEquals("/a/b/c/", guc.normalizePath("//a/b/c/"));
//		assertEquals("/a/b/c/", guc.normalizePath("/a///b//c/."));
//		assertEquals("/a/b/", guc.normalizePath("/a/b/c///.."));
//		assertEquals("/a/c", guc.normalizePath("/a//b/..//c"));
//		assertEquals("/a/c/", guc.normalizePath("/a///b/..//c/"));
//		assertEquals("/", guc.normalizePath("/..///"));
		assertEquals("/", guc.normalizePath("/.."));
//		assertEquals("/", guc.normalizePath("/.///"));
//		assertEquals("/", guc.normalizePath("//."));
//		assertEquals("/", guc.normalizePath("/a//../"));
//		assertEquals("/", guc.normalizePath("//a/.."));
//		assertEquals("/a/", guc.normalizePath("/a/.///"));
//		assertEquals("/a/", guc.normalizePath("//a//."));
		assertEquals("/a/.../", guc.normalizePath("/a/.../"));
		assertEquals("/a/...", guc.normalizePath("/a/..."));
//		assertEquals("/a/.../", guc.normalizePath("//a//.../"));
//		assertEquals("/a/...", guc.normalizePath("//a//..."));
	}
	
	public void testEscape() {
		assertEquals("%20", guc.escape(" "));
		assertEquals("!", guc.escape("!"));
		assertEquals("%22", guc.escape("\""));
		assertEquals("#", guc.escape("#"));
		assertEquals("$", guc.escape("$"));
		assertEquals("%", guc.escape("%"));
		assertEquals("&", guc.escape("&"));
		assertEquals("'", guc.escape("'"));
		assertEquals("(", guc.escape("("));
		assertEquals(")", guc.escape(")"));
		assertEquals("*", guc.escape("*"));
		assertEquals("+", guc.escape("+"));
		assertEquals(",", guc.escape(","));
		assertEquals("-", guc.escape("-"));
		assertEquals(".", guc.escape("."));
		assertEquals("/", guc.escape("/"));
		assertEquals("0", guc.escape("0"));
		assertEquals("9", guc.escape("9"));
		assertEquals(":", guc.escape(":"));
		assertEquals(";", guc.escape(";"));
		assertEquals("%3c", guc.escape("<"));
		assertEquals("=", guc.escape("="));
		assertEquals("%3e", guc.escape(">"));
		assertEquals("?", guc.escape("?"));
		assertEquals("@", guc.escape("@"));
		assertEquals("A", guc.escape("A"));
		assertEquals("B", guc.escape("B"));
		assertEquals("Y", guc.escape("Y"));
		assertEquals("Z", guc.escape("Z"));
		assertEquals("[", guc.escape("["));
		assertEquals("\\", guc.escape("\\"));
		assertEquals("]", guc.escape("]"));
		assertEquals("%5e", guc.escape("^"));
		assertEquals("_", guc.escape("_"));
		assertEquals("%60", guc.escape("`"));
		assertEquals("a", guc.escape("a"));
		assertEquals("b", guc.escape("b"));
		assertEquals("y", guc.escape("y"));
		assertEquals("z", guc.escape("z"));
		assertEquals("%7b", guc.escape("{"));
		assertEquals("|", guc.escape("|"));
		assertEquals("%7d", guc.escape("}"));
		assertEquals("~", guc.escape("~"));
	}
	
	static void checkCanonicalization(URLCanonicalizer guc, String expected,
			String input) throws URISyntaxException {
		String canonOnce = guc.canonicalize(input);
		assertEquals(expected, canonOnce);
		
		// check that canonicalization is idempotent
		String canonTwice = guc.canonicalize(canonOnce);
		assertEquals(expected, canonTwice);
	}

}
