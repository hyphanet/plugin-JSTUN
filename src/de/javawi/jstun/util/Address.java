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

package de.javawi.jstun.util;

import java.util.*;
import java.net.*;

public class Address {
	int firstOctet;
	int secondOctet;
	int thirdOctet;
	int fourthOctet;
	
	public Address(int firstOctet, int secondOctet, int thirdOctet, int fourthOctet) throws UtilityException {
		if ((firstOctet < 0) || (firstOctet > 255) || (secondOctet < 0) || (secondOctet > 255) || (thirdOctet < 0) || (thirdOctet > 255) || (fourthOctet < 0) || (fourthOctet > 255)) {
			throw new UtilityException("Address is malformed.");
		}
		this.firstOctet = firstOctet;
		this.secondOctet = secondOctet;
		this.thirdOctet = thirdOctet;
		this.fourthOctet = fourthOctet;
	}
	
	public Address(String address) throws UtilityException {
		StringTokenizer st = new StringTokenizer(address, ".");
		if (st.countTokens() != 4) {
			throw new UtilityException("4 octets in address string are required.");
		}
		int i = 0;
		while (st.hasMoreTokens()) {
			int temp = Integer.parseInt(st.nextToken());
			if ((temp < 0) || (temp > 255)) {
				throw new UtilityException("Address is in incorrect format.");
			}
			switch (i) {
			case 0: firstOctet = temp; ++i; break;
			case 1: secondOctet = temp; ++i; break;
			case 2: thirdOctet = temp; ++i; break;
			case 3: fourthOctet = temp; ++i; break;
			}
		}
	}
	
	public Address(byte[] address) throws UtilityException {
		if (address.length < 4) {
			throw new UtilityException("4 bytes are required.");
		}
		firstOctet = Utility.OneByteToInteger(address[0]);
		secondOctet = Utility.OneByteToInteger(address[1]);
		thirdOctet = Utility.OneByteToInteger(address[2]);
		fourthOctet = Utility.OneByteToInteger(address[3]);
	}
	
	public String toString() {
		return new String(firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet);
	}
	
	public byte[] getBytes() throws UtilityException {
		byte[] result = new byte[4];
		result[0] = Utility.IntegerToOneByte(firstOctet);
		result[1] = Utility.IntegerToOneByte(secondOctet);
		result[2] = Utility.IntegerToOneByte(thirdOctet);
		result[3] = Utility.IntegerToOneByte(fourthOctet);
		return result;
	}
	
	public InetAddress getInetAddress() throws UtilityException, UnknownHostException {
		byte[] address = new byte[4];
		address[0] = Utility.IntegerToOneByte(firstOctet);
		address[1] = Utility.IntegerToOneByte(secondOctet);
		address[2] = Utility.IntegerToOneByte(thirdOctet);
		address[3] = Utility.IntegerToOneByte(fourthOctet);
		return InetAddress.getByAddress(address);
	}
	
	public boolean equals(Object obj) {
		try {
			byte[] data1 = this.getBytes();
			byte[] data2 = ((Address) obj).getBytes();
			if ((data1[0] == data2[0]) && (data1[1] == data2[1]) &&
			    (data1[2] == data2[2]) && (data1[3] == data2[3])) return true;
			return false;
		} catch (UtilityException ue) {
			return false;
		}
	}

}
