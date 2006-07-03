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

import java.util.logging.Logger;

public class ReflectedFrom extends MappedResponseChangedSourceAddressReflectedFrom {
	private static Logger logger = Logger.getLogger("de.javawi.stun.attribute.ReflectedFrom");
	
	public ReflectedFrom() {
		super(MessageAttribute.MessageAttributeType.ReflectedFrom);
	}
	
	public static ReflectedFrom parse(byte[] data) throws MessageAttributeParsingException {
		ReflectedFrom result = new ReflectedFrom();
		MappedResponseChangedSourceAddressReflectedFrom.parse(result, data);
		logger.finer("Message Attribute: ReflectedFrom parsed: " + result.toString() + ".");
		return result;
	}

	
}
