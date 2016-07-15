/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * HexContestant.java
 *
 *
 * Created: Sat Jan 11 16:40:05 2003
 *
 */
public interface HexContestant {
	void yourTurn(Coord lastMove);

	boolean initGame(int player); // player tells you whether you are player one
									// or two

	void stopGame(int player); // player tells you whether you are player one or
								// two

	void youWin(Coord winningMove);

	void youLose(Coord winningMove);

}// HexContestant
