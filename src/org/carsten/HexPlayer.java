/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * HexPlayer.java
 *
 * Created: Fri Jan 10 15:56:15 2003
 *
 * Based on Hex Maniac v1.2, Copyright Cameron Browne 5/10/2000
 *
 */

public class HexPlayer implements HexContestant {
	private HexEngine engine;
	private Coord last = new Coord();

	int NUM_DEFENSE_TEMPLATES = 25;
	static String HEX_DefenseTemplate[][] = // [NUM_DEFENSE_TEMPLATES][2] =
			{ { "H V - -", "- - - -" }, // acute blind corner
					{ "- - . H", "- - V -" }, // obtuse blind corner
					{ "- V H v", "- - - -" }, { "- v H V", "- - - -" }, { "- V H .", "h . . h" },
					{ "- . H V", "h . . h" }, { "v h H -", "V . h -" }, { "- H h v", "h . V -" },
					{ "- V H -", "h . h -" }, { "- - H V", "- h . h" }, { "- V H -", "- v - -" },
					{ "- H V -", "- v - -" }, { "- V H -", "- - h -" }, { "- H V -", "h - - -" },
					{ ". H h -", "V - - -" }, { "- h H .", "- - V -" }, { "- V H -", "h . - -" },
					{ "- H V -", "- . h -" }, { "- h H h", "- v V v" }, { "- h H h", "v V v -" },
					{ "- h H v", "- v V -" }, { "- v H h", "- V v -" }, { "- H r -", "h - - -" },
					{ "- r H -", "- - h -" }, { "- . H .", "- V . -" }, // reentrant
																		// block
																		// below
																		// (general
																		// case)
			};

	@Override
	public void youWin(Coord last) {
	}

	@Override
	public void youLose(Coord last) {
	}

	public HexPlayer(HexEngine p) {
		engine = p;
	}

	@Override
	public void stopGame(int n) {
	}

	@Override
	public boolean initGame(int n) {
		return true;
	}

	@Override
	public void yourTurn(Coord l) {
		last.i = l.j; // Somehow the computer got it the other way round ...
		last.j = l.i;

		int boardSize = engine.getBoardSize();
		Coord theMove = new Coord();

		if (last.i == -1) {
			bestOpening(theMove); // opening move
		} else if ((engine.getNumMoves() == 1) && (engine.getCurrentPlayer() == HexEngine.PLAYER_TWO)
				&& goodSwap(last)) {
			engine.doMove(HexEngine.swap); // swap opponent's opening move
			return;
		} else if (last.j == 0) {
			// System.err.println("Defending blind row");
			defendBlindRow(theMove); // defend the blind row
		} else {

			// System.err.println("Trying diagonal move");
			if (last.i < boardSize - last.j) {// determine dual point on left
				theMove.i = boardSize - last.j;
				theMove.j = boardSize - last.i - 1;
			} else {// determine dual point on right
				theMove.i = boardSize - last.j - 1;
				theMove.j = boardSize - last.i;
			}
		}

		if (!engine.isValid(theMove) || (engine.getBoard()[theMove.j][theMove.i] != HexEngine.EMPTY)) {
			// System.err.println(" can't, so doing spare move");
			spareMove(theMove); // spare move - do some damage!
		}

		theMove.invert();
		engine.doMove(theMove);
	}

	void bestOpening(Coord move) {
		move.i = engine.getBoardSize() - 1;
		move.j = 0;
	}

	boolean goodSwap(Coord move) {
		return (move.i == engine.getBoardSize() - move.j - 1);
	}

	boolean defendBlindRow(Coord move) {
		move.i = move.j = -1;
		int boardSize = engine.getBoardSize();
		int board[][] = engine.getBoard();

		for (int d = 0; d < NUM_DEFENSE_TEMPLATES; d++) {
			// Find root position H on top row
			int root = 0;

			for (; root < 4; root++)
				if (HEX_DefenseTemplate[d][0].charAt(root * 2) == 'H')
					break;

			if (root >= 4 // no root found
					|| root == 0 && !(last.i == 0 && last.j == 0) // acute
																	// corner no
																	// good
					|| root == 3 && !(last.i == boardSize - 1 && last.j == 0) // obtuse
																				// corner
																				// no
																				// good
			)
				continue;

			// Check whether rest of template matches board position
			boolean valid = true;

			int reentrant = 0;

			for (int j = 0; j < 2 && valid; j++)
				for (int c = 0; c < 4 && valid; c++) {
					int i = last.i - root + c;

					if (i < 0 || i >= boardSize)
						continue;
					if (j < 0 || j >= boardSize)
						continue;

					char ch = HEX_DefenseTemplate[d][j].charAt(c * 2);

					if (ch == 'V' && (board[j][i] == HexEngine.EMPTY)) {
						move.i = i;// Coord(i, j);
						move.j = j;
					} else if (ch == 'V' && (board[j][i] != HexEngine.EMPTY))
						valid = false;
					else if (ch == 'h' && board[j][i] != HexEngine.PLAYER_ONE)
						valid = false;
					else if (ch == 'v' && board[j][i] != HexEngine.PLAYER_TWO)
						valid = false;
					else if (ch == 'x' && (board[j][i] == HexEngine.EMPTY))
						valid = false;
					else if (ch == '.' && (board[j][i] != HexEngine.EMPTY))
						valid = false;
					else if (ch == 'r') {
						if (board[j][i] == HexEngine.PLAYER_TWO)
							valid = false;
						else {
							move.i = last.i;// Coord(last);
							move.j = last.j;

							reentrant = (i < root) ? -1 : 1; // is reentrant
																// move
						}
					}
				}

			if (!valid)
				continue; // invalid template

			if (move.i == -1 || move.j == -1)
				continue; // no valid reply found

			if (reentrant != 0) {
				if (reentrant > 0) {
					// Find reentrant block above
					while (move.i < boardSize - 1) {
						if (board[0][move.i] == 0)
							return true; // adjacent block above

						if (board[1][move.i] == 0) {
							move.j = 1;// = Coord(move.i, 1);
							return true; // reentrant block above
						}
						move.i++;
					}
				} else {
					// Find reentrant block below
					while (move.i >= 0) {
						if (board[0][move.i] == 0)
							return true; // adjacent block above
						if (board[1][move.i - 1] == 0) {
							move.i--;// = Coord(move.i - 1, 1);
							move.j = 1;
							return true; // reentrant block above
						}
						move.i--;
					}
				}
				return false; // no reentrant block found
			}
			return true; // good move found!
		}
		return false;
	}

	// Determines best spare move. There will ALWAYS be a spare move somewhere.
	//
	void spareMove(Coord move) {
		int boardSize = engine.getBoardSize();
		int board[][] = engine.getBoard();

		// Look for urgent moves on the blind row
		move.j = 0;
		for (move.i = 0; move.i < boardSize; move.i++)
			if ((board[0][move.i] == HexEngine.EMPTY) && ((move.i < boardSize - 1)
					&& (board[0][move.i + 1] == HexEngine.PLAYER_ONE)
					&& ((board[1][move.i] == HexEngine.PLAYER_TWO) || (move.i > 0)
							&& (board[1][move.i] == HexEngine.EMPTY) && (board[1][move.i - 1] == HexEngine.PLAYER_ONE))
					|| move.i > 0 && board[0][move.i - 1] == HexEngine.PLAYER_ONE
							&& ((board[1][move.i - 1] == HexEngine.PLAYER_TWO) || (board[1][move.i] == HexEngine.EMPTY)
									&& (board[1][move.i + 1] == HexEngine.PLAYER_ONE))))
				return;

		// If top obtuse corner is empty, take it
		if (board[0][boardSize - 1] == HexEngine.EMPTY) {
			move.i = boardSize - 1;// = Coord(boardSize - 1, 0);
			move.j = 0;
			return;
		}

		// Block any reentrant point on row 1
		move.j = 1;
		for (move.i = 0; move.i < boardSize - 1; move.i++) // skip move.i == N -
															// 1
			if ((board[1][move.i] == HexEngine.EMPTY) && (board[0][move.i] == HexEngine.PLAYER_ONE
					&& board[0][move.i + 1] != HexEngine.PLAYER_ONE
					|| board[0][move.i + 1] == HexEngine.PLAYER_ONE && board[0][move.i] != HexEngine.PLAYER_ONE))
				return;

		// Block any point adjacent to White on row 0
		move.j = 0;
		for (move.i = 0; move.i < boardSize; move.i++)
			if ((board[0][move.i] == HexEngine.EMPTY) && (move.i > 0 && board[0][move.i - 1] == HexEngine.PLAYER_ONE
					|| move.i < boardSize - 1 && board[0][move.i + 1] == HexEngine.PLAYER_ONE))
				return;

		// Take any point an empty point away from Black on row 0
		move.j = 0;
		for (move.i = 0; move.i < boardSize; move.i++)
			if ((board[0][move.i] == HexEngine.EMPTY) && (move.i > 1 && (board[0][move.i - 1] == HexEngine.EMPTY)
					&& board[0][move.i - 2] == HexEngine.PLAYER_TWO
					|| move.i < boardSize - 2 && (board[0][move.i + 1] == HexEngine.EMPTY)
							&& board[0][move.i + 2] == HexEngine.PLAYER_TWO))
				return;

		// Take any empty point from top obtuse corner down (must be at least
		// one)
		for (move.j = 0; move.j < boardSize; move.j++)
			for (move.i = boardSize - 1; move.i >= 0; move.i--)
				if (board[move.j][move.i] == HexEngine.EMPTY)
					return;

		move.i = move.j = -1;// = Coord(-1, -1); // bad result
	}

}// HexPlayer
