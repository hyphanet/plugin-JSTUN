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

import de.javawi.jstun.util.*;

public class ChangeRequest extends MessageAttribute {
   /* 
    *  0                   1                   2                   3
    *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    * |0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 A B 0|
    * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */
	boolean changeIP = false;
	boolean changePort = false;
	
	public ChangeRequest() {
		super(MessageAttribute.MessageAttributeType.ChangeRequest);
	}
	
	public boolean isChangeIP() {
		return changeIP;
	}
	
	public boolean isChangePort() {
		return changePort;
	}
	
	public void setChangeIP() {
		changeIP = true;
	}
	
	public void setChangePort() {
		changePort = true;
	}
	
	public byte[] getBytes() throws UtilityException {
		byte[] result = new byte[8];
		// message attribute header
		// type
		System.arraycopy(Utility.IntegerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
		// length
		System.arraycopy(Utility.IntegerToTwoBytes(4), 0, result, 2, 2);
		
		// change request header
		if (changeIP) result[7] = Utility.IntegerToOneByte(4);
		if (changePort) result[7] = Utility.IntegerToOneByte(2);
		if (changeIP && changePort) result[7] = Utility.IntegerToOneByte(6);
		return result;
	}
	
	public static ChangeRequest parse(byte[] data) throws MessageAttributeParsingException {
		try {
			if (data.length < 4) {
				throw new MessageAttributeParsingException("Data array too short");
			}
			ChangeRequest cr = new ChangeRequest();
			int status = Utility.OneByteToInteger(data[3]);
			switch (status) {
			case 2: cr.setChangePort(); break;
			case 4: cr.setChangeIP(); break;
			case 6: cr.setChangeIP(); cr.setChangePort(); break;
			default: throw new MessageAttributeParsingException("Status parsing error"); 
			}
			return cr;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}
