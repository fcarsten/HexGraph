/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * Coord.java
 *
 *
 * Created: Fri Jan 10 16:27:11 2003
 *
 * @version
 */
public class Coord {
	public int i = -1;
	public int j = -1;

	public Coord(int i1, int j1) {
		i = i1;
		j = j1;
	}

	public Coord() {
	}

	@Override
	public String toString() {
		return "(" + i + "," + j + ")";
	}

	public void invert() {
		int tmp = i;
		i = j;
		j = tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Coord))
			return false;

		Coord other = (Coord) obj;
		return (other.i == i && other.j == j);
	}

}// Coord
