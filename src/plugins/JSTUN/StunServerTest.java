package plugins.JSTUN;

import junit.framework.TestCase;

public class StunServerTest extends TestCase {

	public void testHostnameCanBeParsed() {
		StunServer stunServer = StunServer.parse("simple.stun-server.test");
		assertEquals("parsed server host", "simple.stun-server.test", stunServer.getHostname());
		assertEquals("parsed server port", 3478, stunServer.getPort());
	}

	public void testHostnameWithPortNumberCanBeParsed() {
		StunServer stunServer = StunServer.parse("with-port.stun-server.test:12345");
		assertEquals("parsed server host", "with-port.stun-server.test", stunServer.getHostname());
		assertEquals("parsed server port", 12345, stunServer.getPort());
	}

	public void testHostnameWithNonNumericPortNumberWillCauseException() {
		try {
			StunServer.parse("with-port.stun-server.test:not-a-number");
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			/* expected. */
		}
	}

	public void testToStringOfHostnameContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("simple.stun-server.test");
		assertEquals("string representation", "simple.stun-server.test:3478", stunServer.toString());
	}

	public void testToStringOfHostnameWithPortContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("with-port.stun-server.test:23456");
		assertEquals("string representation", "with-port.stun-server.test:23456", stunServer.toString());
	}

	public void testIPv4AddressCanBeParsed() {
		StunServer stunServer = StunServer.parse("172.16.17.18");
		assertEquals("parsed server host", "172.16.17.18", stunServer.getHostname());
		assertEquals("parsed server port", 3478, stunServer.getPort());
	}

	public void testIPv4AddressWithPortNumberCanBeParsed() {
		StunServer stunServer = StunServer.parse("172.17.18.19:2021");
		assertEquals("parsed server host", "172.17.18.19", stunServer.getHostname());
		assertEquals("parsed server port", 2021, stunServer.getPort());
	}

	public void testIPv4AddressWithNonNumericPortNumberWillCauseException() {
		try {
			StunServer.parse("172.18.19.20:not-a-number");
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			/* expected. */
		}
	}

	public void testToStringOfIPv4AddressContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("172.19.20.21");
		assertEquals("string representation", "172.19.20.21:3478", stunServer.toString());
	}

	public void testToStringOfIPv4AddressWithPortContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("172.20.21.22:34567");
		assertEquals("string representation", "172.20.21.22:34567", stunServer.toString());
	}

	public void testIPv6AddressCanBeParsed() {
		StunServer stunServer = StunServer.parse("[fc00::1234]");
		assertEquals("parsed server host", "[fc00::1234]", stunServer.getHostname());
		assertEquals("parsed server port", 3478, stunServer.getPort());
	}

	public void testIPv6AddressWithPortNumberCanBeParsed() {
		StunServer stunServer = StunServer.parse("[fc00::2345]:3456");
		assertEquals("parsed server host", "[fc00::2345]", stunServer.getHostname());
		assertEquals("parsed server port", 3456, stunServer.getPort());
	}

	public void testIPv6AddressWithNonNumericPortNumberWillCauseException() {
		try {
			StunServer.parse("[fc00::3456]:not-a-number");
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			/* expected. */
		}
	}

	public void testToStringOfIPv6AddressContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("[fc00::3456]");
		assertEquals("string representation", "[fc00::3456]:3478", stunServer.toString());
	}

	public void testToStringOfIPv6AddressWithPortContainsSensibleRepresentation() {
		StunServer stunServer = StunServer.parse("[fc00::4567]:45678");
		assertEquals("string representation", "[fc00::4567]:45678", stunServer.toString());
	}

	public void testHashcodeIsTheSameOnStunServersWithSameHostnameAndPort() {
		int firstHashcode = StunServer.parse("host:1234").hashCode();
		int secondHashcode = StunServer.parse("host:1234").hashCode();
		assertEquals("hashcodes", firstHashcode, secondHashcode);
	}

	public void testHashcodeIsDifferentOnStunServersWithDifferentHostnames() {
		int firstHashcode = StunServer.parse("host:1234").hashCode();
		int secondHashcode = StunServer.parse("hostname:1234").hashCode();
		assertFalse("hashcodes should not be equal", firstHashcode == secondHashcode);
	}

	public void testHashcodeIsDifferentOnStunServersWithDifferentPorts() {
		int firstHashcode = StunServer.parse("host:1234").hashCode();
		int secondHashcode = StunServer.parse("host").hashCode();
		assertFalse("hashcodes should not be equal", firstHashcode == secondHashcode);
	}

	public void testEqualsDoesNotMatchNonStunServerObjects() {
		StunServer stunServer = StunServer.parse("host:1234");
		assertFalse("equals must be false", stunServer.equals(new Object()));
	}

	public void testEqualsDoesNotMatchIfHostnameIsDifferent() {
		StunServer firstStunServer = StunServer.parse("host:1234");
		StunServer secondStunServer = StunServer.parse("hostname:1234");
		assertFalse("equals must be false", firstStunServer.equals(secondStunServer));
	}

	public void testEqualsDoesNotMatchIfPortIsDifferent() {
		StunServer firstStunServer = StunServer.parse("host:1234");
		StunServer secondStunServer = StunServer.parse("host");
		assertFalse("equals must be false", firstStunServer.equals(secondStunServer));
	}

	public void testEqualsDoesMatchIfHostnameAndPortAreIdentical() {
		StunServer firstStunServer = StunServer.parse("host:1234");
		StunServer secondStunServer = StunServer.parse("host:1234");
		assertEquals("equals must be true", firstStunServer, secondStunServer);
	}

}
