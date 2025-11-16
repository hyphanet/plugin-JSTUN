package plugins.JSTUN;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import junit.framework.TestCase;

import static java.util.stream.Collectors.joining;

public class StunServerListTest extends TestCase {

	public void testServerListIsLoadedFromRemoteUrl() throws IOException {
		setupServerAndVerifyReturnedList(this::getStunServerList);
	}

	public void testServerListIsAllowedToContainEmptyLines() throws IOException {
		setupServerAndVerifyReturnedList(this::getStunServerListWithEmptyLines);
	}

	private void setupServerAndVerifyReturnedList(Supplier<String> responseSupplier) throws IOException {
		int httpServerPort = startHttpServer(responseSupplier);
		List<StunServer> stunServers = StunServerList.loadStunServers("http://localhost:" + httpServerPort + "/stun-servers");
		assertEquals("number of STUN servers", 6, stunServers.size());
		assertEquals("first STUN server", StunServer.parse("first.stun-server.test"), stunServers.get(0));
		assertEquals("second STUN server", StunServer.parse("second.stun-server.test:1234"), stunServers.get(1));
		assertEquals("third STUN server", StunServer.parse("127.16.17.18"), stunServers.get(2));
		assertEquals("fourth STUN server", StunServer.parse("172.17.18.19:2021"), stunServers.get(3));
		assertEquals("fifth STUN server", StunServer.parse("[fc00::1234]"), stunServers.get(4));
		assertEquals("sixth STUN server", StunServer.parse("[fc00::2345]:3456"), stunServers.get(5));
	}

	private static int startHttpServer(Supplier<String> responseSupplier) throws IOException {
		HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		httpServer.createContext("/stun-servers", exchange -> {
			exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
			byte[] response = responseSupplier.get().getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			try (OutputStream body = exchange.getResponseBody()) {
				body.write(response);
			}
			exchange.close();
		});
		httpServer.start();
		return httpServer.getAddress().getPort();
	}

	private String getStunServerList() {
		return Stream.of("first.stun-server.test", "second.stun-server.test:1234", "127.16.17.18", "172.17.18.19:2021", "[fc00::1234]", "[fc00::2345]:3456")
				.collect(joining("\n"));
	}

	private String getStunServerListWithEmptyLines() {
		return Stream.of("", "first.stun-server.test", "", "second.stun-server.test:1234", "", "", "127.16.17.18", "172.17.18.19:2021", "", "[fc00::1234]", "[fc00::2345]:3456", "", "")
				.collect(joining("\n"));
	}

}
