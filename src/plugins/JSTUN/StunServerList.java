package plugins.JSTUN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Loads a list of STUN servers from a given URL.
 * <h2>Usage</h2>
 * <pre>
 *     List&lt;StunServer> stunServers = StunServerList.loadStunServers("https://my.stun-servers.test/stun-servers.txt");
 *     for (StunServer stunServer : stunServers) {
 *         doSomethingWithStunServer(stunServer);
 *     }
 * </pre>
 *
 * @see #loadStunServers(String)
 */
public class StunServerList {

	/**
	 * Loads a list of STUN servers from the given URL. The response is
	 * expected to be a plain text document, comprising STUN server
	 * specifications, one per line, separated by LF or CR/LF.
	 *
	 * @param stunServerListUrl The URL of the STUN server list
	 * @return The parsed list of STUN servers
	 * @throws IOException if an I/O error occurs
	 */
	public static List<StunServer> loadStunServers(String stunServerListUrl) throws IOException {
		URLConnection httpURLConnection = new URL(stunServerListUrl).openConnection();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (OutputStream outputStream = byteArrayOutputStream;
			 InputStream inputStream = httpURLConnection.getInputStream()) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
		}
		String serverListString = new String(byteArrayOutputStream.toByteArray(), UTF_8);
		return stream(serverListString.split("\r?\n"))
				.filter(line -> !line.isEmpty())
				.map(StunServer::parse)
				.collect(toList());
	}

}
