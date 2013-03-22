package org.archive.url;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.apache.commons.httpclient.URIException;

import com.google.common.net.InetAddresses;

import junit.framework.TestCase;

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
		dumpParse("http://www.archive.org/index.html#foo");
		dumpParse("http://www.archive.org/");
		dumpParse("http://www.archive.org");
		dumpParse("http://www.archive.org?");
		dumpParse("http://www.archive.org:8080/index.html?query#foo");
		dumpParse("http://www.archive.org:8080/index.html?#foo");
		dumpParse("http://www.archive.org:8080?#foo");
		dumpParse("http://bŸcher.ch:8080?#foo");
		
		dumpParse("dns:bŸcher.ch");

	}
	
	private void dumpParse(String s) throws URISyntaxException {
		HandyURL h = URLParser.parse(s);
		System.out.format("Input:(%s)\nHandyURL\t%s\n",s,h.toDebugString());
	}

}
