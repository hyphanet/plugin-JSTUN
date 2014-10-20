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

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.logging.Logger;

import plugins.JSTUN.de.javawi.jstun.attribute.ChangeRequest;
import plugins.JSTUN.de.javawi.jstun.attribute.ChangedAddress;
import plugins.JSTUN.de.javawi.jstun.attribute.ErrorCode;
import plugins.JSTUN.de.javawi.jstun.attribute.MappedAddress;
import plugins.JSTUN.de.javawi.jstun.attribute.MessageAttribute;
import plugins.JSTUN.de.javawi.jstun.attribute.MessageAttributeException;
import plugins.JSTUN.de.javawi.jstun.attribute.MessageAttributeParsingException;
import plugins.JSTUN.de.javawi.jstun.header.MessageHeader;
import plugins.JSTUN.de.javawi.jstun.header.MessageHeaderParsingException;
import plugins.JSTUN.de.javawi.jstun.util.UtilityException;

public class DiscoveryTest_ {
    private static Logger logger = Logger.getLogger("de.javawi.stun.test.DiscoveryTest");
    InetAddress           iaddress;
    String                stunServer;
    int                   port;
    int                   timeoutInitValue = 300;    // ms
    MappedAddress         ma               = null;
    ChangedAddress        ca               = null;
    boolean               nodeNatted       = true;
    DatagramSocket        socketTest1      = null;
    DiscoveryInfo         di               = null;

    public DiscoveryTest_(InetAddress iaddress, String stunServer, int port) {
        super();
        this.iaddress   = iaddress;
        this.stunServer = stunServer;
        this.port       = port;
    }

    public DiscoveryInfo test()
            throws UtilityException, SocketException, UnknownHostException, IOException,
                   MessageAttributeParsingException, MessageAttributeException,
                   MessageHeaderParsingException {
        ma          = null;
        ca          = null;
        nodeNatted  = true;
        socketTest1 = null;
        di          = new DiscoveryInfo(iaddress);

        if (test1()) {
            if (test2()) {
                if (test1Redo()) {
                    test3();
                }
            }
        }

        socketTest1.close();

        return di;
    }

    private boolean test1()
            throws UtilityException, SocketException, UnknownHostException, IOException,
                   MessageAttributeParsingException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout                    = timeoutInitValue;

        while (true) {
            try {

                // Test 1 including response
                socketTest1 = new DatagramSocket(new InetSocketAddress(iaddress, 0));
                socketTest1.setReuseAddress(true);
                socketTest1.connect(InetAddress.getByName(stunServer), port);
                socketTest1.setSoTimeout(timeout);

                MessageHeader sendMH =
                    new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);

                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();

                sendMH.addMessageAttribute(changeRequest);

                byte[]         data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);

                socketTest1.send(send);
                logger.finer("Test 1: Binding Request sent.");

                MessageHeader receiveMH = new MessageHeader();

                while ( !(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);

                    socketTest1.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                }

                ma = (MappedAddress) receiveMH.getMessageAttribute(
                    MessageAttribute.MessageAttributeType.MappedAddress);
                ca = (ChangedAddress) receiveMH.getMessageAttribute(
                    MessageAttribute.MessageAttributeType.ChangedAddress);

                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(
                                   MessageAttribute.MessageAttributeType.ErrorCode);

                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    logger.config("Message header contains errorcode message attribute.");

                    return false;
                }

                if ((ma == null) || (ca == null)) {
                    di.setError(
                        700,
                        "The server is sending incomplete response (Mapped Address and Changed" +
                        " Address message attributes are missing). The client should not retry.");
                    logger.config("Response does not contain a mapped address or changed address" +
                                  " message attribute.");

                    return false;
                } else {
                    di.setPublicIP(ma.getAddress().getInetAddress());

                    if ((ma.getPort() == socketTest1.getLocalPort()) &&
                            (ma.getAddress().getInetAddress().equals(
                                socketTest1.getLocalAddress()))) {
                        logger.fine("Node is not natted.");
                        nodeNatted = false;
                    } else {
                        logger.fine("Node is natted.");
                    }

                    return true;
                }
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {
                    logger.finer("Test 1: Socket timeout while receiving the response.");
                    timeSinceFirstTransmission += timeout;

                    int timeoutAddValue = (timeSinceFirstTransmission * 2);

                    if (timeoutAddValue > 1600) {
                        timeoutAddValue = 1600;
                    }

                    timeout = timeoutAddValue;
                } else {

                    // node is not capable of udp communication
                    logger.finer(
                        "Test 1: Socket timeout while receiving the response. Maximum retry limit" +
                        " exceed. Give up.");
                    di.setBlockedUDP();
                    logger.fine("Node is not capable of udp communication.");

                    return false;
                }
            }
        }
    }

    private boolean test2()
            throws UtilityException, SocketException, UnknownHostException, IOException,
                   MessageAttributeParsingException, MessageAttributeException,
                   MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout                    = timeoutInitValue;

        while (true) {
            try {

                // Test 2 including response
                DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));

                sendSocket.connect(InetAddress.getByName(stunServer), port);
                sendSocket.setSoTimeout(timeout);

                MessageHeader sendMH =
                    new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);

                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();

                changeRequest.setChangeIP();
                changeRequest.setChangePort();
                sendMH.addMessageAttribute(changeRequest);

                byte[]         data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);

                sendSocket.send(send);
                logger.finer("Test 2: Binding Request sent.");

                int         localPort    = sendSocket.getLocalPort();
                InetAddress localAddress = sendSocket.getLocalAddress();

                sendSocket.close();

                DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);

                receiveSocket.connect(ca.getAddress().getInetAddress(), ca.getPort());
                receiveSocket.setSoTimeout(timeout);

                MessageHeader receiveMH = new MessageHeader();

                while ( !(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);

                    receiveSocket.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                }

                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(
                                   MessageAttribute.MessageAttributeType.ErrorCode);

                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    logger.config("Message header contains errorcode message attribute.");

                    return false;
                }

                if ( !nodeNatted) {
                    di.setOpenAccess();
                    logger.fine(
                        "Node has open access to the internet (or, at least the node is a" +
                        " full-cone NAT without translation).");
                } else {
                    di.setFullCone();
                    logger.fine("Node is behind a full-cone NAT.");
                }

                return false;
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {
                    logger.finer("Test 2: Socket timeout while receiving the response.");
                    timeSinceFirstTransmission += timeout;

                    int timeoutAddValue = (timeSinceFirstTransmission * 2);

                    if (timeoutAddValue > 1600) {
                        timeoutAddValue = 1600;
                    }

                    timeout = timeoutAddValue;
                } else {
                    logger.finer(
                        "Test 2: Socket timeout while receiving the response. Maximum retry limit" +
                        " exceed. Give up.");

                    if ( !nodeNatted) {
                        di.setSymmetricUDPFirewall();
                        logger.fine("Node is behind a symmetric udp firewall.");

                        return false;
                    } else {

                        // not is natted
                        // redo test 1 with address and port as offered in the changed-address
                        // message attribute
                        return true;
                    }
                }
            }
        }
    }

    private boolean test1Redo()
            throws UtilityException, SocketException, UnknownHostException, IOException,
                   MessageAttributeParsingException, MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout                    = timeoutInitValue;

        while (true) {

            // redo test 1 with address and port as offered in the changed-address message attribute
            try {

                // Test 1 with changed port and address values
                socketTest1.connect(ca.getAddress().getInetAddress(), ca.getPort());
                socketTest1.setSoTimeout(timeout);

                MessageHeader sendMH =
                    new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);

                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();

                sendMH.addMessageAttribute(changeRequest);

                byte[]         data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);

                socketTest1.send(send);
                logger.finer("Test 1 redo with changed address: Binding Request sent.");

                MessageHeader receiveMH = new MessageHeader();

                while ( !(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);

                    socketTest1.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                }

                MappedAddress ma2 = (MappedAddress) receiveMH.getMessageAttribute(
                                        MessageAttribute.MessageAttributeType.MappedAddress);
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(
                                   MessageAttribute.MessageAttributeType.ErrorCode);

                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    logger.config("Message header contains errorcode message attribute.");

                    return false;
                }

                if (ma2 == null) {
                    di.setError(
                        700,
                        "The server is sending incomplete response (Mapped Address message" +
                        " attribute is missing). The client should not retry.");
                    logger.config("Response does not contain a mapped address message attribute.");

                    return false;
                } else {
                    if ((ma.getPort() != ma2.getPort()) ||
                            ( !(ma.getAddress().getInetAddress().equals(
                                ma2.getAddress().getInetAddress())))) {
                        di.setSymmetricCone();
                        logger.fine("Node is behind a symmetric NAT.");

                        return false;
                    }
                }

                return true;
            } catch (SocketTimeoutException ste2) {
                if (timeSinceFirstTransmission < 7900) {
                    logger.config(
                        "Test 1 redo with changed address: Socket timeout while receiving the" +
                        " response.");
                    timeSinceFirstTransmission += timeout;

                    int timeoutAddValue = (timeSinceFirstTransmission * 2);

                    if (timeoutAddValue > 1600) {
                        timeoutAddValue = 1600;
                    }

                    timeout = timeoutAddValue;
                } else {

                    // TODO: error handling here
                    logger.config(
                        "Test 1 redo with changed address: Socket timeout while receiving the" +
                        " response.  Maximum retry limit exceed. Give up.");

                    return false;
                }
            }
        }
    }

    private void test3()
            throws UtilityException, SocketException, UnknownHostException, IOException,
                   MessageAttributeParsingException, MessageAttributeException,
                   MessageHeaderParsingException {
        int timeSinceFirstTransmission = 0;
        int timeout                    = timeoutInitValue;

        while (true) {
            try {

                // Test 3 including response
                DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));

                sendSocket.connect(InetAddress.getByName(stunServer), port);
                sendSocket.setSoTimeout(timeout);

                MessageHeader sendMH =
                    new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);

                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();

                changeRequest.setChangePort();
                sendMH.addMessageAttribute(changeRequest);

                byte[]         data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);

                sendSocket.send(send);
                logger.finer("Test 3: Binding Request sent.");

                int         localPort    = sendSocket.getLocalPort();
                InetAddress localAddress = sendSocket.getLocalAddress();

                sendSocket.close();

                DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);

                receiveSocket.connect(InetAddress.getByName(stunServer), ca.getPort());
                receiveSocket.setSoTimeout(timeout);

                MessageHeader receiveMH = new MessageHeader();

                while ( !(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);

                    receiveSocket.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                }

                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(
                                   MessageAttribute.MessageAttributeType.ErrorCode);

                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    logger.config("Message header contains errorcode message attribute.");

                    return;
                }

                if (nodeNatted) {
                    di.setRestrictedCone();
                    logger.fine("Node is behind a restricted NAT.");
                }
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {
                    logger.finer("Test 3: Socket timeout while receiving the response.");
                    timeSinceFirstTransmission += timeout;

                    int timeoutAddValue = (timeSinceFirstTransmission * 2);

                    if (timeoutAddValue > 1600) {
                        timeoutAddValue = 1600;
                    }

                    timeout = timeoutAddValue;
                } else {
                    logger.finer(
                        "Test 3: Socket timeout while receiving the response. Maximum retry limit" +
                        " exceed. Give up.");
                    di.setPortRestrictedCone();
                    logger.fine("Node is behind a port restricted NAT.");

                    return;
                }
            }
        }
    }
}
