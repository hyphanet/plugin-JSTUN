/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de>
 *
 * JSTUN is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSTUN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSTUN; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package plugins.JSTUN.de.javawi.jstun.test;

import java.net.*;

public class DiscoveryInfo {
	private InetAddress testIP;
	private boolean error = false;
	private int errorResponseCode = 0;
	private String errorReason;
	private boolean openAccess = false;
	private boolean blockedUDP = false;
	private boolean fullCone = false;
	private boolean restrictedCone = false;
	private boolean portRestrictedCone = false;
	private boolean symmetricCone = false;
	private boolean symmetricUDPFirewall = false;
	private InetAddress publicIP;
	
	public DiscoveryInfo(InetAddress testIP) {
		this.testIP = testIP;
	}
	
	public boolean isError() {
		return error;
	}
	
	public void setError(int responseCode, String reason) {
		this.error = true;
		this.errorResponseCode = responseCode;
		this.errorReason = reason;
	}
	
	public boolean isOpenAccess() {
		if (error) return false;
		return openAccess;
	}

	public void setOpenAccess() {
		this.openAccess = true;
	}

	public boolean isBlockedUDP() {
		if (error) return false;
		return blockedUDP;
	}

	public void setBlockedUDP() {
		this.blockedUDP = true;
	}
	
	public boolean isFullCone() {
		if (error) return false;
		return fullCone;
	}

	public void setFullCone() {
		this.fullCone = true;
	}

	public boolean isPortRestrictedCone() {
		if (error) return false;
		return portRestrictedCone;
	}

	public void setPortRestrictedCone() {
		this.portRestrictedCone = true;
	}

	public boolean isRestrictedCone() {
		if (error) return false;
		return restrictedCone;
	}

	public void setRestrictedCone() {
		this.restrictedCone = true;
	}

	public boolean isSymmetricCone() {
		if (error) return false;
		return symmetricCone;
	}

	public void setSymmetricCone() {
		this.symmetricCone = true;
	}

	public boolean isSymmetricUDPFirewall() {
		if (error) return false;
		return symmetricUDPFirewall;
	}

	public void setSymmetricUDPFirewall() {
		this.symmetricUDPFirewall = true;
	}
	
	public InetAddress getPublicIP() {
		return publicIP;
	}
	
	public void setPublicIP(InetAddress publicIP) {
		this.publicIP = publicIP;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Network interface: ");
		try {
			sb.append(NetworkInterface.getByInetAddress(testIP).getName());
		} catch (SocketException se) {
			sb.append("unknown");
		}
		sb.append('\n');
		sb.append("Local IP address: ");
		sb.append(testIP.getHostAddress());
		sb.append('\n');
		if (error) {
            sb.append(errorReason).append(" - Responsecode: ").append(errorResponseCode);
			return sb.toString();
		}
		sb.append("Result: ");
		if (openAccess) sb.append("Open access to the Internet.\n");
		if (blockedUDP) sb.append("Firewall blocks UDP or server down.\n");
		if (fullCone) sb.append("Full Cone NAT handles connections.\n");
		if (restrictedCone) sb.append("Restricted Cone NAT handles connections.\n");
		if (portRestrictedCone) sb.append("Port restricted Cone NAT handles connections.\n");
		if (symmetricCone) sb.append("Symmetric Cone NAT handles connections.\n");
		if (symmetricUDPFirewall) sb.append ("Symmetric UDP Firewall handles connections.\n");
		if (!openAccess && !blockedUDP && !fullCone && !restrictedCone && !portRestrictedCone && !symmetricCone && !symmetricUDPFirewall) sb.append("unkown\n");
		sb.append("Public IP: ");
		if (publicIP != null) {
			sb.append(publicIP.getHostAddress());
		} else {
			sb.append("unknown");
		}
		sb.append('\n');
		return sb.toString();
	}	
}
