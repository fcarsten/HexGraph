/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

/**
 * Hexagon.java
 *
 *
 * Created: Fri Jan 10 12:25:04 2003
 *
 * @author <a href="mailto:carsten@it.usyd.edu.au"></a>
 * @version
 */
import java.awt.*;

public class Hexagon {
	private Color color;

	public Hexagon() {
		color = Color.white;
	}

	/**
	 * Get the value of color.
	 * 
	 * @return value of color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the value of color.
	 * 
	 * @param v
	 *            Value to assign to color.
	 */
	public void setColor(Color v) {
		this.color = v;
	}

}// Hexagon
