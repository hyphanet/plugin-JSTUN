package de.javawi.jstun.test.demo;

import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.javawi.jstun.test.DiscoveryTest;

public class DiscoveryTestDemo implements Runnable {
	InetAddress iaddress;
	
	public DiscoveryTestDemo(InetAddress iaddress) {
		this.iaddress = iaddress;
	}
	
	public void run() {
		try {
			DiscoveryTest test = new DiscoveryTest(iaddress, "stun.xten.net", 3478);
			// iphone-stun.freenet.de:3478
			// larry.gloo.net:3478
			// stun.xten.net:3478
			System.out.println(test.test());
		} catch (BindException be) {
			System.out.println(iaddress.toString() + ": " + be.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Handler fh = new FileHandler("logging.txt");
			fh.setFormatter(new SimpleFormatter());
			Logger.getLogger("de.javawi.stun").addHandler(fh);
			Logger.getLogger("de.javawi.stun").setLevel(Level.ALL);

			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
				while (iaddresses.hasMoreElements()) {
					InetAddress iaddress = iaddresses.nextElement();
					if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress()) {
						Thread thread = new Thread(new DiscoveryTestDemo(iaddress));
						thread.start();
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
