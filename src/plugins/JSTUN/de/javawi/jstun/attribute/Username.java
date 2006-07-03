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

package de.javawi.jstun.attribute;

import de.javawi.jstun.util.Utility;
import de.javawi.jstun.util.UtilityException;

public class Username extends MessageAttribute {
	String username;
	
	public Username() {
		super(MessageAttribute.MessageAttributeType.Username);
	}
	
	public Username(String username) {
		super(MessageAttribute.MessageAttributeType.Username);
		setUsername(username);
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public byte[] getBytes() throws UtilityException {
		int length = username.length();
		// username header
		if ((length % 4) != 0) {
			length += 4 - (length % 4);
		}
		// message attribute header
		length += 4;
		byte[] result = new byte[length];
		// message attribute header
		// type
		System.arraycopy(Utility.IntegerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
		// length
		System.arraycopy(Utility.IntegerToTwoBytes(length-4), 0, result, 2, 2);
		
		// username header
		byte[] temp = username.getBytes();
		System.arraycopy(temp, 0, result, 4, temp.length);
		return result;
	}
	
	public static Username parse(byte[] data) {
		Username result = new Username();
		String username = new String(data);
		result.setUsername(username);
		return result;
	}
}
