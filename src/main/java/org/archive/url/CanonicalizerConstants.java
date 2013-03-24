package org.archive.url;


public interface CanonicalizerConstants {
	public static final int HOST_SETTINGS = 0;
	
	public static final int HOST_ORIGINAL = 0;
	public static final int HOST_LOWERCASE = 1;
	public static final int HOST_MASSAGE = 2;
	public static final int HOST_MINIMAL_ESCAPE = 4;
	public static final int HOST_IDN_TO_ASCII = 8;
	public static final int HOST_REMOVE_EXTRA_DOTS = 16;
	public static final int HOST_CANONICALIZE_IPV4 = 32;
	public static final int HOST_TRIM_ENCODED_WHITESPACE = 64;

	
	public static final int PORT_SETTINGS = 1;
	
	public static final int PORT_ORIGINAL = 0;
	public static final int PORT_STRIP_DEFAULT = 1;

	
	public static final int PATH_SETTINGS = 2;

	public static final int PATH_ORIGINAL = 0;
	public static final int PATH_LOWERCASE = 1;
	public static final int PATH_STRIP_SESSION_ID = 2;
	public static final int PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY = 8;
	public static final int PATH_MINIMAL_ESCAPE = 16;
	public static final int PATH_COLLAPSE_MULTIPLE_SLASHES = 32;
	public static final int PATH_NORMALIZE_DOT_SEGMENTS = 64;
	public static final int PATH_BACKSLASH_TO_SLASH = 128;

	
	public static final int QUERY_SETTINGS = 3;

	public static final int QUERY_ORIGINAL = 0;
	public static final int QUERY_LOWERCASE = 1;
	public static final int QUERY_STRIP_SESSION_ID = 2;
	public static final int QUERY_STRIP_EMPTY = 4;
	public static final int QUERY_ALPHA_REORDER = 8;
	public static final int QUERY_MINIMAL_ESCAPE = 16;
	// TODO: Need a setting to remove empty query ARGs..

	public static final int FRAGMENT_SETTINGS = 4;

	public static final int FRAGMENT_ORIGINAL = 0;
	public static final int FRAGMENT_STRIP = 1;


	public static final int AUTH_SETTINGS = 5;

	public static final int AUTH_ORIGINAL = 0;
	public static final int AUTH_STRIP_AUTH = 1;
	public static final int AUTH_STRIP_PASS = 2;
	public static final int AUTH_MINIMAL_ESCAPE = 4;
	
	public static final int SCHEME_SETTINGS = 6;

	public static final int SCHEME_ORIGINAL = 0;
	public static final int SCHEME_LOWERCASE = 1;
	
	
	public static final int NUM_SETTINGS = 7;

}
