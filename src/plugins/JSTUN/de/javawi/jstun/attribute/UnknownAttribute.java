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

import java.util.*;

import de.javawi.jstun.util.Utility;
import de.javawi.jstun.util.UtilityException;

public class UnknownAttribute extends MessageAttribute {
	/* 
	 *  0                   1                   2                   3
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |      Attribute 1 Type           |     Attribute 2 Type        |
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |      Attribute 3 Type           |     Attribute 4 Type    ...
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	
	Vector<MessageAttributeType> unkown = new Vector<MessageAttributeType>();
	
	public UnknownAttribute() {
		super(MessageAttribute.MessageAttributeType.UnknownAttribute);
	}
	
	public void addAttribute(MessageAttributeType attribute) {
		unkown.add(attribute);
	}
	
	public byte[] getBytes() throws UtilityException {
		int length = 0;
		if (unkown.size()%2 == 1) {
			length = 2 * (unkown.size() + 1) + 4;
		} else {
			length = 2 * unkown.size() + 4;
		}
		byte[] result = new byte[length];
		// message attribute header
		// type
		System.arraycopy(Utility.IntegerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
		// length
		System.arraycopy(Utility.IntegerToTwoBytes(length - 4), 0, result, 2, 2);
		
		// unkown attribute header
		Iterator<MessageAttributeType> it = unkown.iterator();
		while(it.hasNext()) {
			MessageAttributeType attri = it.next();
			System.arraycopy(Utility.IntegerToTwoBytes(typeToInteger(attri)), 0, result, 4, 2);
		}
		// padding
		if (unkown.size()%2 == 1) {
			System.arraycopy(Utility.IntegerToTwoBytes(typeToInteger(unkown.elementAt(1))), 0, result, 4, 2);
		}
		return result;
	}

	public static UnknownAttribute parse(byte[] data) throws MessageAttributeParsingException {
		try {
			UnknownAttribute result = new UnknownAttribute();
			if (data.length % 4 != 0) throw new MessageAttributeParsingException("Data array too short");
			for (int i = 0; i < data.length; i += 4) {
				byte[] temp = new byte[4];
				System.arraycopy(data, i, temp, 0, 4);
				long attri = Utility.FourBytesToLong(temp);
				result.addAttribute(MessageAttribute.intToType(attri));
			}
			return result;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}