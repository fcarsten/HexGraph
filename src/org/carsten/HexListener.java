/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * HexListener.java
 *
 *
 * Created: Sat Jan 11 15:20:40 2003
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class HexListener implements Runnable, HexContestant {
	protected HexEngine engine;
	protected boolean expectingMove = false;
	InetAddress remoteAddr = null;
	int remotePort = -1;
	int localPort = 45678;
	Set<String> localAddresses = null;// new HashSet();
	protected int playerNumber = 0;

	public HexListener(HexEngine e) {
		engine = e;
	}

	@Override
	public void stopGame(int n) {
		expectingMove = false;
	}

	public boolean tellOpponent(String msg) {
		try (Socket socket = new Socket(remoteAddr, remotePort)) {
			socket.setSoTimeout(10000);

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			sendOutput(out, msg);

			String reply = readInput(in);
			if (reply.equals("ERROR")) {
				JOptionPane.showConfirmDialog(null, "Communication breakdown with opponent.\nResetting game.",
						"Network Problem", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				engine.stopGame();
			} else
				return true;
		} catch (UnknownHostException e) {
			JOptionPane.showConfirmDialog(null,
					"Lost contact to your opponent.\nYour opponent's computer is no longer known on the network.\nResetting game.",
					"Network Problem", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
			// System.err.println("Unknown host exception: " + e.getMessage());
			engine.stopGame();
		} catch (IOException eio) {
			JOptionPane.showConfirmDialog(null,
					"Lost contact to your opponent: " + eio.getMessage() + "\nResetting game.", "Network Problem",
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
			// System.err.println("Network exception: " + eio.getMessage());
			engine.stopGame();
		}
		return false;
	}

	@Override
	public void yourTurn(Coord last) {
		if (last.i == -1) {
			expectingMove = true;
			return;
		}

		String msg = "SWAP";
		if (!last.equals(HexEngine.swap)) {
			char a = (char) ('a' + last.i);
			char b = (char) ('a' + last.j);
			msg = "MOVE " + a + b;
		}
		expectingMove = tellOpponent(msg);
	}

	@Override
	public void youWin(Coord last) // Unless we resign your opponent should know
									// by now
	{
		if (last.equals(HexEngine.resign_p1) || last.equals(HexEngine.resign_p2)) {
			String msg = "RESIGNP1";

			if (playerNumber == HexEngine.PLAYER_ONE)
				msg = "RESIGNP2";
			tellOpponent(msg);
		}

		expectingMove = false;
	}

	@Override
	public void youLose(Coord winningMove) { // We still have to tell the other
												// end how we won
		if (winningMove.equals(HexEngine.resign_p1) || winningMove.equals(HexEngine.resign_p2))
			return;

		char a = (char) ('a' + winningMove.i);
		char b = (char) ('a' + winningMove.j);

		tellOpponent("THANKS " + a + b);
		expectingMove = false;
	}

	public boolean initGame(InetAddress ra, int port) throws UnknownHostException {
		System.err.println("Trying: " + ra + " on port " + port);
		if (isLocalAddress(ra.getHostAddress()) && (port == localPort)) {
			System.err.println("Can't play with yourself");
			return false;
		}

		try (Socket socket = new Socket(ra, port)) {
			socket.setSoTimeout(10000);

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.err.println(readInput(in)); // Reading welcome message

			sendOutput(out, "PLAY " + localPort);
			String reply = readInput(in);
			if (reply.equals("YES")) {
				// JOptionPane.showConfirmDialog(null, "Remote player accepted",
				// "Reply", JOptionPane.DEFAULT_OPTION,
				// JOptionPane.INFORMATION_MESSAGE);
				remoteAddr = ra;
				remotePort = port;
				sendOutput(out, "OK");
				return true;
			} else if (reply.equals("BUSY")) {
				System.err.println("Remote player is currently playing another game");
				// JOptionPane.showConfirmDialog(null, "Remote player is
				// currently playing another game",
				// "Reply", JOptionPane.DEFAULT_OPTION,
				// JOptionPane.INFORMATION_MESSAGE);
			} else if (reply.equals("NO")) {
				System.err.println("Remote player rejected your offer");

				// JOptionPane.showConfirmDialog(null, "Remote player rejected
				// your offer",
				// "Reply", JOptionPane.DEFAULT_OPTION,
				// JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (java.net.SocketTimeoutException e) {
			System.err.println("Network Timeout: " + e);
			return false;
		} catch (IOException eio) {
			System.err.println("Network error: " + eio.getMessage());
			return false;
		}
		return false;
	}

	@Override
	public boolean initGame(int n) {
		expectingMove = false;
		playerNumber = n;

		if (n == HexEngine.PLAYER_ONE) // we are the client and have to contact
										// the other player
		{
			// System.err.println("Trying to contact other player on
			// localhost:"+remotePort);
			String remoteHostName = "localhost";

			if (remoteAddr != null)
				remoteHostName = remoteAddr.getHostName();

			String remote = JOptionPane.showInputDialog("Enter contact: hostname:port",
					remoteHostName + ":" + remotePort);
			String remoteInfo[] = remote.split(":");
			remoteHostName = remoteInfo[0];

			int port = -1;

			try {
				port = Integer.parseInt(remoteInfo[1]);
			} catch (NumberFormatException e) {
				System.err.println("Illegal port number!");
				JOptionPane.showConfirmDialog(null, "Illegal port number.\nMust be be a positive integer.", "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			// InetAddress remoteAddr = null;
			try {
				remoteAddr = InetAddress.getByName(remoteHostName);
			} catch (UnknownHostException e) {
				JOptionPane.showConfirmDialog(null, "Unknown host " + remoteHostName, "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			boolean isLocalAddress = isLocalAddress(remoteAddr.getHostAddress());

			if (isLocalAddress) {
				System.err.println("We have a local address");

				if (port == localPort) {
					JOptionPane.showConfirmDialog(null, "You can't play against yourself", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
			}

			int tmpPort = -1;

			if (port == -1)
				tmpPort = 45678;
			else
				tmpPort = port;

			while (true) {
				if (isLocalAddress && tmpPort == localPort)
					tmpPort++;

				try {
					if (initGame(remoteAddr, tmpPort)) {
						return true;
					} else if (port != -1) {
						JOptionPane.showConfirmDialog(null, "Network error: No (willing) Opponent on port " + port,
								"Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
						return false;
					}
				} catch (UnknownHostException e) {
					JOptionPane.showConfirmDialog(null, "Network error: Unknown host", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					return false;
				}

				if (tmpPort == 45700) {
					JOptionPane.showConfirmDialog(null, "Network error: No Opponent on any port", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					return false;
				}

				tmpPort++;
			}
		} else // we already accepted during network accept handling
			return true;

		// return false;
	}

	synchronized public String readInput(BufferedReader in) throws IOException {
		System.err.println("Reading input");

		String lengthS = "";
		int c;
		while (true) {
			c = in.read();
			if (c == -1)
				throw new IOException("Unexpected end of stream");
			if (c == ':')
				break;
			lengthS += (char) c;
			// System.err.println(lengthS);
		}

		if (lengthS.length() == 0)
			throw new IOException("Header corrupt");

		int length = Integer.parseInt(lengthS);

		char buffer[] = new char[length];

		int numRead = 0;
		String resS = "";// new String(buffer);

		while (numRead < length) {
			int res = in.read(buffer, 0, length);
			resS += new String(buffer);
			numRead += res;
		}

		if (numRead > length)
			System.err.println("Read " + numRead + " instead of expected " + length + " characters");

		System.err.println("Read: \"" + resS + "\"");

		return resS;
	}

	synchronized public void sendOutput(PrintWriter out, String msg) {
		System.err.println("Sending: \"" + msg + "\"");

		out.print("" + msg.length() + ":" + msg);
		out.flush();
	}

	public boolean isLocalAddress(String address) {
		if (localAddresses == null) {
			System.err.println("Determining local addresses");
			localAddresses = new HashSet<>();
			try {
				for (Enumeration<?> enums = NetworkInterface.getNetworkInterfaces(); enums.hasMoreElements();) {
					NetworkInterface inter = (NetworkInterface) enums.nextElement();

					for (Enumeration<InetAddress> enum2 = inter.getInetAddresses(); enum2.hasMoreElements();) {
						InetAddress addr = (InetAddress) enum2.nextElement();
						System.err.println(" Address: " + addr.getHostName() + " (" + addr.getHostAddress() + ")");
						localAddresses.add(addr.getHostAddress());
					}
				}
			} catch (SocketException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
		return localAddresses.contains(address);

	}

	@Override
	@SuppressWarnings("null")
	public void run() {
		// try
		// {
		// String msg = "Hello";
		// InetAddress group = InetAddress.getByName("228.5.6.7");
		// MulticastSocket s = new MulticastSocket(6789);

		// s.joinGroup(group);
		// DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
		// group, 6789);

		// System.err.println("Sending hi");
		// s.send(hi);
		// // get their responses!
		// byte[] buf = new byte[1000];
		// DatagramPacket recv = new DatagramPacket(buf, buf.length);
		// System.err.println("receiving");
		// boolean bbb=true;

		// while(bbb){
		// s.receive(recv);
		// // ...
		// // OK, I'm done talking - leave the group...
		// System.err.println("received from "+recv.getSocketAddress()+ " :"+
		// (new String(recv.getData(), recv.getOffset(),
		// recv.getLength())));
		// }

		// s.leaveGroup(group);
		// }
		// catch(UnknownHostException e){
		// System.err.println("Unknown host exception: " + e.getMessage());
		// }
		// catch(IOException eio){
		// System.err.println("Network exception: " + eio.getMessage());
		// }

		ServerSocket serverSocket = null;
		localPort = 45678;

		while (localPort < 45700) {
			try {
				serverSocket = new ServerSocket(localPort);
				break;
			} catch (IOException e) {
				System.err.println("Could not listen on port: " + localPort);
				localPort++;
			}
		}

		if (localPort == 45700) {
			System.err.println("Could not establish network port");
			return;
		}

		System.err.println("Listening to port: " + localPort);

		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(10000);

				try {
					SocketAddress sa = clientSocket.getRemoteSocketAddress();
					if (sa instanceof InetSocketAddress) {
						InetSocketAddress isa = (InetSocketAddress) sa;
						String remoteName = isa.getAddress().getHostName();
						String remoteAddrString = isa.getAddress().getHostAddress();

						System.err.println("Connection from " + remoteName + " (" + remoteAddrString + ")");

						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						try {
							String msg = "Welcome to Hex";

							sendOutput(out, msg);

							String query = readInput(in);
							query = query.toUpperCase();

							// System.err.println("Received: " + query);
							if (query.startsWith("PLAY")) {
								String portString = query.substring(5);
								System.err.println(remoteName + " wants to play on port: " + portString);
								if (!engine.isGameRunning()) {
									if (JOptionPane.showConfirmDialog(null,
											"Accept challange from \"" + remoteName + "\"", "Accept Challange?",
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
										try {
											sendOutput(out, "YES");
											query = readInput(in);
											query = query.toUpperCase();
											if (query.equals("OK")) {
												remoteAddr = isa.getAddress();
												remotePort = Integer.parseInt(portString);
												engine.initGame(HexEngine.HUMAN_NETWORK);
											}
										} catch (IOException e) {
											JOptionPane.showConfirmDialog(null,
													"Communication breakdown with opponent.\nGame not started.",
													"Network Problem", JOptionPane.DEFAULT_OPTION,
													JOptionPane.INFORMATION_MESSAGE);
										}

									} else
										sendOutput(out, "NO");
								} else {
									sendOutput(out, "BUSY");
								}

							} else if (query.equals("SWAP")) {
								System.err.println("Received swap");
								if (!expectingMove || (engine.getNumMoves() != 1)) {
									sendOutput(out, "ERROR");
								} else {
									sendOutput(out, "OK");
									engine.doMove(HexEngine.swap);
								}

							} else if (query.equals("RESIGNP1")) {
								System.err.println("Received resign from player one");
								sendOutput(out, "OK");
								engine.doMove(HexEngine.resign_p1);
							} else if (query.equals("RESIGNP2")) {
								System.err.println("Received resign from player one");
								sendOutput(out, "OK");
								engine.doMove(HexEngine.resign_p2);
							} else if (query.startsWith("MOVE")) {
								if (!expectingMove) {
									sendOutput(out, "ERROR");
								} else {
									char x = query.charAt(5);
									char y = query.charAt(6);
									sendOutput(out, "OK");

									engine.doMove(new Coord(x - 'A', y - 'A'));
								}

							} else if (query.startsWith("THANKS")) {
								if (!expectingMove) {
									sendOutput(out, "ERROR");
								} else {
									char x = query.charAt(7);
									char y = query.charAt(8);
									sendOutput(out, "WELCOME");

									engine.doMove(new Coord(x - 'A', y - 'A'));
								}

							}

						} catch (IOException e) {
							System.err.println("Network communication failed: " + e.getMessage());
						}
						out.close();
						in.close();
					}

				} catch (IOException e) {
					System.err.println("Network stream creation failed: " + e.getMessage());
				}

				try {
					clientSocket.close();
				} catch (IOException e) {
					System.err.println("Could not close client socket");
				}
			} catch (IOException e) {
				System.err.println("Accept failed: 45678");
			}
		}
	}

}// HexListener
