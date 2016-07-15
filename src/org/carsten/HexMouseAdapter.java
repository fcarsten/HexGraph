/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;
import java.awt.geom.*;

// detects a click on the board
public class HexMouseAdapter extends MouseAdapter {

	private Hex hex;
	private Point2D.Double point = null;

	public HexMouseAdapter(Hex h) {
		hex = h;
	}

	// determines if a click was within a valid node and then selects it
	@Override
	public void mousePressed(MouseEvent e) {
		if (hex.isExpectingMove()) {
			int x = e.getX();
			int y = e.getY();
			if (point == null) {
				point = new Point2D.Double(x, y);
			} else {
				point.x = x;
				point.y = y;
			}

			Vector<Node> nodes = hex.getNodes();
			for (int i = 0; i != nodes.size(); ++i) {
				Node current = (Node) (nodes.elementAt(i));
				Point2D.Double nodeCentre = new Point2D.Double(current.x_coord, current.y_coord);
				double distance = Math.sqrt(
						(double) ((nodeCentre.x - x) * (nodeCentre.x - x) + (nodeCentre.y - y) * (nodeCentre.y - y)));

				// if click is within a node
				if (distance <= current.nodeRadius) {
					if (current.colour == Color.black || (hex.canSwap() && !current.fixed)) {
						hex.doMove(current);
					}
					break;
				}
			}
		}
	}
}
