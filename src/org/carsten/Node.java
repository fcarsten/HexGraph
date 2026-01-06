/***
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
***/

package org.carsten;

import java.awt.Color;

import java.util.Vector;
import java.awt.geom.*;

// represents a node within the graph
public class Node {
	public double x_coord;
	public double y_coord;

	public Vector<Node> neighbours;

	// colour
	public Color colour = Color.black;
	public int identifier;

	public boolean fixed = false;

	public double nodeRadius = 10;

	// for animation purposes
	public double new_x_coord;
	public double new_y_coord;
	public Vector<Node> new_neighbours;
	public boolean to_be_deleted = false;
	public double temp_x_coord;
	public double temp_y_coord;

	// public double old_neighbours_size;

	public int tutteValue;

	public Vector<Node> remainingNeighbours = new Vector<>();

	// for force directed algorithm
	Point2D.Double force = new Point2D.Double();
	double[] zone = new double[8];

	// creates a node with given coordinates and identifier
	public Node(double x, double y, int i) {
		x_coord = x;
		y_coord = y;
		identifier = i;

		neighbours = new Vector<>();

		new_x_coord = x;
		new_y_coord = y;
		new_neighbours = new Vector<>();
		temp_x_coord = x;
		temp_y_coord = y;

		for (int z = 0; z != 8; ++z) {
			zone[z] = Double.POSITIVE_INFINITY;
		}

	}

	// add neighbour
}
