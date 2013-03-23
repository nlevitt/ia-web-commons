package org.archive.url;

import java.net.MalformedURLException;
import java.net.URL;

public class HandyURL {
	public final static int DEFAULT_PORT = -1;

	private String scheme;
	private String authUser;
	private String authPass;
	private String host;
	private int port;
	private String path;
	private String query;
	private String fragment;

	// cached values:
	private String cachedPubSuffix;
	private String cachedPubPrefix;

	public HandyURL() {
		this.scheme = null;
		this.authUser = null;
		this.authPass = null;
		this.host = null;
		this.port = DEFAULT_PORT;
		this.path = "";
		this.query = null;
		this.fragment = null;
	}

	public HandyURL(String scheme,
			String authUser,
			String authPass,
			String host,
			int port,
			String path,
			String query,
			String fragment) {
		this.scheme = scheme;
		this.authUser = authUser;
		this.authPass = authPass;
		this.host = host;
		this.port = port;
		this.path = path;
		if (this.path == null) {
			this.path = "";
		}
		this.query = query;
		this.fragment = fragment;
	}

	public String getSURTString(boolean includeScheme) {
		return getURLString(true, includeScheme, false);
	}

	public String getURLString(boolean surt, boolean includeScheme, boolean publicSuffix) {
		StringBuilder sb = new StringBuilder();

		if(includeScheme) {
			if (scheme != null) {
				sb.append(scheme).append(':');
			}
			if (host != null) {
				sb.append("//");
				if (surt) {
					sb.append("(");
				}
			}
		}
		if(!surt && authUser != null) {
			sb.append(authUser);
			if(authPass != null) {
				sb.append(":").append(authPass);
			}
			sb.append("@");
		}
		if (host != null) {
			String hostSrc = host;
			if(publicSuffix) {
				hostSrc = getPublicSuffix();
			}
			if(surt) {
				hostSrc = URLRegexTransformer.hostToSURT(hostSrc);
			}
			sb.append(hostSrc);
		}
		if(port != DEFAULT_PORT) {
			sb.append(":").append(port);
		}
		if (surt && authUser != null) {
			// see org.archive.util.SURT.fromURI(String, boolean)
			sb.append('@').append(authUser);
			if(authPass != null) {
				sb.append(":").append(authPass);
			}
		}
		if(host != null && surt) {
			sb.append(")");
		}
		if(path != null) {
			sb.append(path);
		}
		if(query != null) {
			sb.append('?').append(query);
		}
		if(fragment != null) {
			sb.append('#').append(fragment);
		}
		return sb.toString();
	}

	public String getURLString() {
		return getURLString(false, true, false);
	}

	public String getPathQuery() {
		StringBuilder sb = new StringBuilder();
		if(path != null) {
			sb.append(path);
		}
		if(query != null) {
			sb.append('?').append(query);
		}
		return sb.toString();
	}

	public URL toURL() throws MalformedURLException {
		return new URL(getURLString());
	}

	public String getPublicSuffix() {
		if(cachedPubSuffix != null) {
			return cachedPubSuffix;
		}
		if(host == null) {
			return null;
		}
		cachedPubSuffix = URLRegexTransformer.hostToPublicSuffix(host);
		return cachedPubSuffix;
	}

	public String getPublicPrefix() {
		if(cachedPubPrefix != null) {
			return cachedPubPrefix;
		}
		if(host == null) {
			return null;
		}
		String pubS = getPublicSuffix();
		if(pubS == null) {
			return null;
		}
		int hostLen = host.length();
		hostLen -= pubS.length();
		if(hostLen > 1) {
			cachedPubPrefix = host.substring(0,(host.length() - pubS.length())-1);
		} else {
			cachedPubPrefix = "";
		}
		return cachedPubPrefix;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}
	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	/**
	 * @return the authUser
	 */
	public String getAuthUser() {
		return authUser;
	}
	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}
	/**
	 * @return the authPass
	 */
	public String getAuthPass() {
		return authPass;
	}
	/**
	 * @param authPass the authPass to set
	 */
	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
		cachedPubPrefix = null;
		cachedPubSuffix = null;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * @return the path, which is the one and only segment guaranteed to never
	 *         be null (it can be an empty string "" though)
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
		if (this.path == null) {
			this.path = "";
		}
	}
	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	/**
	 * @return the hash
	 * @deprecated use {@link #getFragment()}
	 */
	public String getHash() {
		return fragment;
	}
	/**
	 * @param hash the hash to set
	 * @deprecated use {@link #setFragment(String)}
	 */
	public void setHash(String hash) {
		this.fragment = hash;
	}

	public String getFragment() {
		return fragment;
	}
	public void setFragment(String fragment) {
		this.fragment = fragment;
	}

	/**
	 * @return authority (userinfo@host:port)
	 */
	public String getAuthority() {
		StringBuilder sb = new StringBuilder();
		
		if(authUser != null) {
			sb.append(authUser);
			if(authPass != null) {
				sb.append(":").append(authPass);
			}
			sb.append("@");
		}
		
		sb.append(host);
		
		if(port != DEFAULT_PORT) {
			sb.append(":").append(port);
		}
		
		return sb.toString();
	}
	
	public String getUserinfo() {
		if (authUser != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(authUser);
			if (authPass != null) {
				sb.append(":").append(authPass);
			}
			return sb.toString();
		} else {
			return null;
		}
	}

	public String toDebugString() {
		return String.format("Scheme(%s) AuthUser(%s) AuthPass(%s) Host(%s) Port(%d) Path(%s) Query(%s) Frag(%s)",
				scheme, authUser, authPass, host, port, path, query, fragment);
	}

	@Override
	public boolean equals(Object obj) {
		 if (obj == this) {
		     return true;
		 }
		 if (!(obj instanceof UsableURI)) {
		     return false;
		 }
		 
		 UsableURI other = (UsableURI) obj;
		 
		 // ordered by guessed likelihood of difference for efficiency
		 return equals(path, other.getPath())
				 && equals(query, other.getQuery())
				 && equals(fragment, other.getFragment())
				 && equals(host, other.getHost())
				 && port == other.getPort()
				 && equals(scheme, other.getScheme())
				 && equals(authUser, other.getAuthUser())
				 && equals(authPass, other.getAuthPass());
	}

	private boolean equals(Object a, Object b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}
}
