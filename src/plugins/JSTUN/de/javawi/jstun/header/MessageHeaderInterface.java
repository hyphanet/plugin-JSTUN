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

package de.javawi.jstun.header;

public interface MessageHeaderInterface {
	public enum MessageHeaderType { BindingRequest, BindingResponse, BindingErrorResponse, SharedSecretRequest, SharedSecretResponse, SharedSecretErrorResponse };
	final static int BINDINGREQUEST = 0x0001;
	final static int BINDINGRESPONSE = 0x0101;
	final static int BINDINGERRORRESPONSE = 0x0111;
	final static int SHAREDSECRETREQUEST = 0x0002;
	final static int SHAREDSECRETRESPONSE = 0x0102;
	final static int SHAREDSECRETERRORRESPONSE = 0x0112;
}