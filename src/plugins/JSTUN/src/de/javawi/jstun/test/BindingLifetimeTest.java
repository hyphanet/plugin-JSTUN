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

package de.javawi.jstun.test;

import java.util.logging.*;
import java.util.*;
import java.io.*;
import java.net.*;

import de.javawi.jstun.attribute.*;
import de.javawi.jstun.header.*;
import de.javawi.jstun.util.UtilityException;

public class BindingLifetimeTest {
	private static Logger logger = Logger.getLogger("de.javawi.stun.test.BindingLifetimeTest");
	String stunServer;
	int port;
	int timeout = 300; //ms
	MappedAddress ma;
	Timer timer;
	DatagramSocket initialSocket;
	
	// start value for binary search - should be carefully choosen
	int upperBinarySearchLifetime = 345000; // ms
	int lowerBinarySearchLifetime = 0;
	int binarySearchLifetime = ( upperBinarySearchLifetime + lowerBinarySearchLifetime ) / 2;
	
	// lifetime value
	int lifetime = -1; // -1 means undefined.
	boolean completed = false;
		
	public BindingLifetimeTest(String stunServer, int port) {
		super();
		this.stunServer = stunServer;
		this.port = port;
		timer = new Timer(true);
	}
	
	public void test() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageAttributeException, MessageHeaderParsingException {
		initialSocket = new DatagramSocket();
		initialSocket.connect(InetAddress.getByName(stunServer), port);
		initialSocket.setSoTimeout(timeout);
		
		if (BindingCommunicationInitialSocket()) {
			return;
		}
		BindingLifetimeTask task = new BindingLifetimeTask();
		timer.schedule(task, binarySearchLifetime);
		logger.finer("Timer scheduled initially: " + binarySearchLifetime + ".");
	}
	
	private boolean BindingCommunicationInitialSocket() throws UtilityException, IOException, MessageHeaderParsingException, MessageAttributeParsingException {
		MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
		sendMH.generateTransactionID();
		ChangeRequest changeRequest = new ChangeRequest();
		sendMH.addMessageAttribute(changeRequest);
		byte[] data = sendMH.getBytes();
		
		DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(stunServer), port);
		initialSocket.send(send);
		logger.finer("Binding Request sent.");
	
		MessageHeader receiveMH = new MessageHeader();
		while (!(receiveMH.equalTransactionID(sendMH))) {
			DatagramPacket receive = new DatagramPacket(new byte[200], 200);
			initialSocket.receive(receive);
			receiveMH = MessageHeader.parseHeader(receive.getData());
		}
		ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
		ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
		if (ec != null) {
			logger.config("Message header contains errorcode message attribute.");
			return true;
		}
		if (ma == null) {
			logger.config("Response does not contain a mapped address message attribute.");
			return true;
		}
		return false;
	}
	
	public int getLifetime() {
		return lifetime;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public void setUpperBinarySearchLifetime(int upperBinarySearchLifetime) {
		this.upperBinarySearchLifetime = upperBinarySearchLifetime;
		binarySearchLifetime = ( upperBinarySearchLifetime + lowerBinarySearchLifetime ) / 2;
	}
	
	class BindingLifetimeTask extends TimerTask {
		
		public BindingLifetimeTask() {
			super();
		}
		
		public void run() {
			try {
				lifetimeQuery();
			} catch (Exception e) {
				logger.config("Unhandled Exception. BindLifetimeTasks stopped.");
				e.printStackTrace();
			}
		}
		
		public void lifetimeQuery() throws UtilityException, MessageAttributeException, MessageHeaderParsingException, MessageAttributeParsingException, IOException {
			try {
				DatagramSocket socket = new DatagramSocket();
				socket.connect(InetAddress.getByName(stunServer), port);
				socket.setSoTimeout(timeout);
			
				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				ChangeRequest changeRequest = new ChangeRequest();
				ResponseAddress responseAddress = new ResponseAddress();
				responseAddress.setAddress(ma.getAddress());
				responseAddress.setPort(ma.getPort());
				sendMH.addMessageAttribute(changeRequest);
				sendMH.addMessageAttribute(responseAddress);
				byte[] data = sendMH.getBytes();
			
				DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(stunServer), port);
				socket.send(send);
				logger.finer("Binding Request sent.");
		
				MessageHeader receiveMH = new MessageHeader();
				while (!(receiveMH.equalTransactionID(sendMH))) {
					DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					initialSocket.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
				}
				ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					logger.config("Message header contains errorcode message attribute.");
					return;
				}
				logger.finer("Binding Response received.");
				if (upperBinarySearchLifetime == (lowerBinarySearchLifetime + 1)) {
					logger.config("BindingLifetimeTest completed. UDP binding lifetime: " + binarySearchLifetime + ".");
					completed = true;
					return;
				}
				lifetime = binarySearchLifetime;
				logger.finer("Lifetime update: " + lifetime + ".");
				lowerBinarySearchLifetime = binarySearchLifetime;
				binarySearchLifetime = ( upperBinarySearchLifetime + lowerBinarySearchLifetime ) / 2;
				if (binarySearchLifetime > 0) {
					BindingLifetimeTask task = new BindingLifetimeTask();
					timer.schedule(task, binarySearchLifetime);
					logger.finer("Timer scheduled: " + binarySearchLifetime + ".");
				} else {
					completed = true;
				}
			} catch (SocketTimeoutException ste) {
				logger.finest("Read operation at query socket timeout.");
				if (upperBinarySearchLifetime == (lowerBinarySearchLifetime + 1)) {
					logger.config("BindingLifetimeTest completed. UDP binding lifetime: " + binarySearchLifetime + ".");
					completed = true;
					return;
				}
				upperBinarySearchLifetime = binarySearchLifetime;
				binarySearchLifetime = ( upperBinarySearchLifetime + lowerBinarySearchLifetime ) / 2;
				if (binarySearchLifetime > 0) {
					if (BindingCommunicationInitialSocket()) {
						return;
					}
					BindingLifetimeTask task = new BindingLifetimeTask();
					timer.schedule(task, binarySearchLifetime);
					logger.finer("Timer scheduled: " + binarySearchLifetime + ".");
				} else {
					completed = true;
				}
			}
		}
	}
}

