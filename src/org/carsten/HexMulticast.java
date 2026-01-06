/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * HexMulticast.java
 *
 *
 * Created: Tue Jan 14 12:54:11 2003
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class HexMulticast extends HexListener {

	static private InetAddress group = null;

	class HexPortBroadCaster implements Runnable {
		@Override
		public void run() {
			System.err.println("Started broadcaster");

			try {
				MulticastSocket s[] = getMulticastSocket();
				sendDatagram("Hi", s);
				while (true) {
					try {
						System.err.println("Trying to receive");

						DatagramPacket recv = receiveDatagram(s);
						String reply = getMessage(recv);

						System.err.println("received boradcast from " + recv.getSocketAddress() + " :" + reply);

						if (reply.startsWith("WANNAPLAY") && (!engine.isGameRunning())) {
							sendDatagram("TRY@" + localPort, s);
						}
					} catch (IOException e) {
						System.err.println("Network error: " + e.getMessage());
					}
				}
			} catch (IOException e) {
				System.err.println("Bad error, can't establish multicast: " + e.getMessage());
			}

		}

	}

	public HexMulticast(HexEngine e) {
		super(e);
		if (group == null) {
			try {
				group = InetAddress.getByName("228.5.6.7");
			} catch (UnknownHostException ex) {
				System.err.println("Bas error, can't establish multicast: " + ex.getMessage());
				System.exit(-1);
			}
		}
		new Thread(new HexPortBroadCaster()).start();

	}

	MulticastSocket[] getMulticastSocket() throws IOException {
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		List<NetworkInterface> l = new LinkedList<>();
		while (enums.hasMoreElements())
			l.add(enums.nextElement());

		if (l.size() == 0)
			throw new IOException("No network interface found");

		ArrayList<MulticastSocket> sList = new ArrayList<>();

		System.err.println("Joining group: " + group);
		for (int i = 0; i < l.size(); i++) {
			try {
				MulticastSocket s = new MulticastSocket(6789);
				s.setTimeToLive(64);
				NetworkInterface ni = (NetworkInterface) l.get(i);

				s.setNetworkInterface(ni);
				// s[i].setLoopbackMode(true);

				s.joinGroup(group);
				System.err.println("Multicast interface: " + ni.getName() + " enabled, ttl:" + s.getTimeToLive());
				sList.add(s);

			} catch (IOException eio) {
				System.err.println("Multicast error on interface: " + l.get(i));
			}

		}

		return sList.toArray(new MulticastSocket[0]);
	}

	void sendDatagram(String msg, MulticastSocket s[]) throws IOException {
		DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, 6789);
		System.err.println("Sending request: " + msg);
		for (int i = 0; i < s.length; i++) {
			if (s[i] != null)
				s[i].send(hi);
		}

	}

	DatagramPacket receiveDatagram(MulticastSocket s[]) throws IOException {
		int index = 0;// s.length-1;

		System.err.println("receiving from interface: " + s[index].getNetworkInterface().getName() + ", ttl: "
				+ s[index].getTimeToLive());
		byte[] buf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		s[index].receive(recv);

		return recv;

	}

	String getMessage(DatagramPacket recv) {
		String reply = new String(recv.getData(), recv.getOffset(), recv.getLength());
		return reply;
	}

	@Override
	public boolean initGame(int n) {
		playerNumber = n;

		expectingMove = false;
		if (n == HexEngine.PLAYER_ONE) // we are the client and have to contact
										// the other player
		{
			try {
				MulticastSocket s[] = getMulticastSocket();
				sendDatagram("WANNAPLAY", s);

				while (true) {
					try {
						for (int i = 0; i < s.length; i++) {
							if (s[i] != null)
								s[i].setSoTimeout(10000);
						}

						DatagramPacket recv = receiveDatagram(s);
						String reply = getMessage(recv);

						System.err.println("received from " + recv.getSocketAddress() + " :" + reply);

						if (reply.startsWith("TRY")) {
							String remoteInfo[] = reply.split("@");

							try {
								int port = Integer.parseInt(remoteInfo[1]);
								if (initGame(recv.getAddress(), port)) {
									// s.leaveGroup(group);
									return true;
								}
								// System.err.println("No success, trying
								// again");
							} catch (NumberFormatException e) {
								System.err.println("Illegal port number: " + remoteInfo[1]);
							}
						}
					} catch (SocketTimeoutException e) {
						JOptionPane.showConfirmDialog(null,
								"No one answered positively.\nEither there is nobody or they are all busy",
								"Network error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

						// System.err.println("Didn't get a reply from the
						// entwork :-(");
						return false;
					}
				}

			} catch (UnknownHostException e) {
				System.err.println("Unknown host exception: " + e.getMessage());
			} catch (IOException eio) {
				System.err.println("Network exception: " + eio.getMessage());
			}

		} else // we already accepted during network accept handling
			return true;
		return false;
	}

}// HexMulticast
