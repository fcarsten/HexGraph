/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;
/**
 * HexEngine.java
 *
 *
 * Created: Sat Jan 11 14:42:20 2003
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */

// Test bla bla comment

import javax.swing.*;

public class HexEngine {
	public static final int EMPTY = 0;
	public static final int PLAYER_ONE = 1;
	public static final int PLAYER_TWO = 2;

	public static final int HUMAN_HUMAN = 0;
	public static final int HUMAN_COMPUTER = 1;
	public static final int NETWORK_GAME = 2;
	public static final int HUMAN_NETWORK = 2;
	public static final int NETWORK_HUMAN = 3;

	public static final Coord swap = new Coord(-10, -10);
	public static final Coord resign_p1 = new Coord(-15, -15);
	public static final Coord resign_p2 = new Coord(-20, -20);

	private int gameMode = HUMAN_HUMAN;

	private HexContestant computer;
	private HexContestant panel;

	private HexContestant classicGUI;
	private HexContestant colinGUI;

	public static Coord nbor[] = { new Coord(1, -1), new Coord(1, 0), new Coord(0, 1), new Coord(-1, 1),
			new Coord(-1, 0), new Coord(0, -1) };

	private int boardSize;
	private int board[][];
	private int numMoves = 0;
	// private boolean hasSwapped = false;
	private boolean visit[][];
	private boolean gameRunning = false;

	private Coord last = new Coord();
	private int currentPlayer = 0;
	private HexListener listener;
	private HexContestant[] players;

	public int getBoardSize() {
		return boardSize;
	}

	public int[][] getBoard() {
		return board;
	}

	public int getNumMoves() {
		return numMoves;
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	// public boolean isSwappingAllowed()
	// {
	// return true;
	// }

	public int getGameMode() {
		return gameMode;
	}

	public HexEngine(int n, int mode) {
		players = new HexContestant[3];

		boardSize = n;
		board = new int[n][n];
		visit = new boolean[n][n];
		listener = new HexMulticast(this);
		Thread thread = new Thread(listener);
		thread.start();

		Object[] possibleValues = { "Classic GUI", "New GUI" };
		Object selectedValue = JOptionPane.showInputDialog(null, "Select User Interface", "GUI",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);

		computer = new HexPlayer(this);
		if (selectedValue == null) {
			System.exit(0);
			return;
		}

		if (selectedValue.equals(possibleValues[0]))
			panel = new HexPanel(this);
		else
			panel = new Hex(this);

	}

	public void stopGame() {
		players[PLAYER_ONE].stopGame(PLAYER_ONE);
		players[PLAYER_TWO].stopGame(PLAYER_TWO);
		gameRunning = false;
	}

	public void initGame(int n) {
		gameRunning = true;
		gameMode = n;
		numMoves = 0;
		for (int i = 0; i < boardSize; i++) {
			for (int k = 0; k < boardSize; k++) {
				board[i][k] = 0;
				visit[i][k] = false;
			}
		}

		// hasSwapped = false;
		currentPlayer = PLAYER_ONE;
		last = new Coord(-1, -1);

		if (gameMode == HUMAN_HUMAN) {
			players[PLAYER_ONE] = panel;
			players[PLAYER_TWO] = panel;
		} else if (gameMode == HUMAN_COMPUTER) {
			players[PLAYER_ONE] = panel;
			players[PLAYER_TWO] = computer;
		} else if (gameMode == HUMAN_NETWORK) {
			players[PLAYER_ONE] = panel;
			players[PLAYER_TWO] = listener;
		} else if (gameMode == NETWORK_HUMAN) {
			players[PLAYER_ONE] = listener;
			players[PLAYER_TWO] = panel;
		}
		if (players[PLAYER_ONE].initGame(PLAYER_ONE) && players[PLAYER_TWO].initGame(PLAYER_TWO)) {
			players[currentPlayer].yourTurn(last);
		} else {
			stopGame();
		}
	}

	public int opponent(int p) {
		return (p == PLAYER_TWO) ? PLAYER_ONE : PLAYER_TWO;
	}

	public void doMove(Coord c) {
		// System.err.println("Received move: "+c);

		int x = c.i;
		int y = c.j;

		if (c.equals(swap)) {
			board[last.i][last.j] = EMPTY;
			board[last.j][last.i] = currentPlayer;

			last.i = swap.i;
			last.j = swap.j;
		} else if (c.equals(resign_p1)) {
			currentPlayer = opponent(currentPlayer);
			players[PLAYER_ONE].youLose(resign_p1);
			players[PLAYER_TWO].youWin(resign_p1);

			gameRunning = false;
			System.out.println("Player 2 won!");
			return;
		} else if (c.equals(resign_p2)) {
			currentPlayer = opponent(currentPlayer);
			players[PLAYER_TWO].youLose(resign_p2);
			players[PLAYER_ONE].youWin(resign_p2);

			gameRunning = false;
			System.out.println("Player 1 won!");
			return;
		} else {
			board[x][y] = currentPlayer;

			last.i = x;
			last.j = y;

			if (gameWon(currentPlayer)) {
				players[opponent(currentPlayer)].youLose(last);
				players[currentPlayer].youWin(last);

				gameRunning = false;
				System.out.println("Player " + currentPlayer + " won!");
				return;
			}
		}

		numMoves++;
		currentPlayer = opponent(currentPlayer);
		players[currentPlayer].yourTurn(last);
	}

	public boolean gameWon(int who) {
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				visit[i][j] = false;
			}
		}

		if (who == PLAYER_TWO) {
			for (int i = 0; i < boardSize; i++)
				if (board[0][i] == PLAYER_TWO && (!visit[0][i]) && gameWon(PLAYER_TWO, new Coord(i, 0)))
					return true;
		} else {
			for (int j = 0; j < boardSize; j++)
				if (board[j][0] == PLAYER_ONE && (!visit[j][0]) && gameWon(PLAYER_ONE, new Coord(0, j)))
					return true;
		}
		return false;
	}

	public boolean gameWon(int who, Coord from) {
		if ((who == PLAYER_ONE && from.i == boardSize - 1) || (who == PLAYER_TWO && from.j == boardSize - 1))
			return true;

		visit[from.j][from.i] = true; // this coord has been visited

		for (int n = 0; n < 6; n++) {
			// Check neighbours for continuation of chain
			Coord to = new Coord(from.i + nbor[n].i, from.j + nbor[n].j);
			if (isValid(to) && (board[to.j][to.i] == who) && (!visit[to.j][to.i]) && gameWon(who, to))
				return true;
		}
		return false;
	}

	public boolean isValid(Coord posn) {
		return (posn.i >= 0 && posn.i < boardSize && posn.j >= 0 && posn.j < boardSize);
	}

	static public void main(String arg[]) {

		if (arg.length > 0)
			new HexEngine(11, 0);
		else
			new HexEngine(11, 1);

	}

}// HexEngine
