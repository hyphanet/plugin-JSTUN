package plugins.JSTUN;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple container for a STUN server’s hostname/IP address (both IPv4 and
 * IPv6) and port number. You get an instance of this class using the
 * {@link #parse(String)} method.
 *
 * <h2>Usage</h2>
 * <pre>
 *     StunServer stunServer = StunServer.parse("my.stun.server:12345");
 *     System.out.println("STUN server is at " + stunServer + ".");
 * </pre>
 */
public class StunServer {

	/**
	 * Parses the given string into a {@link StunServer} object. There are
	 * six possible ways to specify a STUN server:
	 * <p>
	 * <ul>
	 *     <li>A hostname.</li>
	 *     <li>A hostname and a port number.</li>
	 *     <li>An IPv4 address.</li>
	 *     <li>An IPv4 address and a port number.</li>
	 *     <li>An IPv6 address.</li>
	 *     <li>An IPv6 address and a port number.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * If no port number is specified, the default port of 3478 will be used.
	 * </p>
	 * <p>
	 * An IPv6 address must be specified in square brackets, and the
	 * {@link #getHostname() parsed hostname} will include the square
	 * brackets.
	 * </p>
	 *
	 * @param stunServerSpecification The STUN server specification (must
	 * 		not be {@code null})
	 * @return A parsed STUN server
	 */
	public static StunServer parse(String stunServerSpecification) {
		Matcher matcher = addressParser.matcher(stunServerSpecification);
		/* it is impossible for the regex to not match the string. */
		matcher.matches();
		String hostname = matcher.group(1);
		int port = parsePortNumberFromString(matcher.group(2));
		return new StunServer(hostname, port);
	}

	/**
	 * Returns the hostname of the STUN server.
	 *
	 * @return The hostname of the STUN server
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Returns the port number of the STUN server.
	 *
	 * @return The port number of the STUN server
	 */
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return hostname + ":" + port;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StunServer)) {
			return false;
		}
		StunServer that = (StunServer) o;
		return port == that.port && Objects.equals(hostname, that.hostname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hostname, port);
	}

	private StunServer(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	private static int parsePortNumberFromString(String portNumber) {
		return (portNumber == null) ? 3478 : Integer.parseInt(portNumber);
	}

	private final String hostname;
	private final int port;
	private static final Pattern addressParser = Pattern.compile("(\\[.*]|.*?)(?::(.*))?");

}
