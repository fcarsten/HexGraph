/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;
// Represents an edge between two nodes

class Edge {
	public Node node1;
	public Node node2;

	public Edge(Node n1, Node n2) {
		node1 = n1;
		node2 = n2;
	}

}