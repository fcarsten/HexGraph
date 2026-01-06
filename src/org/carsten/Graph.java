/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import java.awt.Color;

import java.util.Vector;

// represents the hex game graph as a group of nodes and links between nodes
public class Graph {
	public Vector<Node> nodes;
	public Vector<Edge> edges;

	public int size;
	public double width;
	public double height;

	public double min_x;
	public double min_y;
	public double max_x;
	public double max_y;

	// Creates all nodes and links between nodes of the initial configuration
	// of the hex game graph
	public Graph(int s, double screen_width, double screen_height) {
		edges = new Vector<>();

		size = s;
		width = screen_width;
		height = screen_height;

		// screen_width -= screen_width/10;
		screen_height -= screen_height / 10;

		nodes = new Vector<>(s * s + 4);
		// double x_middle = screen_width/2;
		double y_middle = screen_height / 2;
		double x_margin = screen_width / 10;
		double y_margin = screen_height / 10;

		double x_inc = (screen_width - 2 * x_margin) / (2 * (s - 1));
		double y_inc = (screen_height - 2 * y_margin) / (2 * (s - 1));

		double start_x = x_margin;
		double start_y = y_middle;

		// add nodes
		for (int i = 0; i != s; ++i) {
			for (int j = 0; j != s; ++j) {
				double x = start_x + j * x_inc;
				double y = start_y - j * y_inc;

				Node current = new Node(x, y, i * s + j);
				nodes.add(current);
			}
			start_x += x_inc;
			start_y += y_inc;
		}

		min_x = x_margin;
		min_y = y_margin;
		max_x = screen_width - x_margin;
		max_y = screen_height - y_margin;

		// add end nodes
		double xs1 = x_margin;
		double ys1 = y_margin;
		Node s1 = new Node(xs1, ys1, (s - 1) * (s - 1) + 1);
		s1.colour = Color.blue;
		s1.fixed = true;
		s1.nodeRadius *= 2;
		for (int c = 0; c != s; ++c) {
			s1.neighbours.add(nodes.elementAt(0 * s + c));
			addEdge(s1, (Node) (nodes.elementAt(0 * s + c)));
		}
		nodes.add(s1);

		double xs2 = x_margin;
		double ys2 = screen_height - y_margin;
		Node s2 = new Node(xs2, ys2, (s - 1) * (s - 1) + 2);
		s2.colour = Color.red;
		s2.fixed = true;
		s2.nodeRadius *= 2;
		for (int r = 0; r != s; ++r) {
			s2.neighbours.add(nodes.elementAt(r * s + 0));
			addEdge(s2, (Node) (nodes.elementAt(r * s + 0)));
		}
		nodes.add(s2);

		double xt1 = screen_width - x_margin;
		double yt1 = screen_height - y_margin;
		Node t1 = new Node(xt1, yt1, (s - 1) * (s - 1) + 3);
		t1.colour = Color.blue;
		t1.fixed = true;
		t1.nodeRadius *= 2;
		for (int c = 0; c != s; ++c) {
			t1.neighbours.add(nodes.elementAt((s - 1) * s + c));
			addEdge(t1, (Node) (nodes.elementAt((s - 1) * s + c)));
		}
		nodes.add(t1);

		double xt2 = screen_width - x_margin;
		double yt2 = y_margin;
		Node t2 = new Node(xt2, yt2, (s - 1) * (s - 1) + 4);
		t2.colour = Color.red;
		t2.fixed = true;
		t2.nodeRadius *= 2;
		for (int r = 0; r != s; ++r) {
			t2.neighbours.add(nodes.elementAt(r * s + (s - 1)));
			addEdge(t2, (Node) (nodes.elementAt(r * s + (s - 1))));
		}
		nodes.add(t2);

		// establish edges between nodes
		for (int r = 0; r != s; ++r) {
			for (int c = 0; c != s; ++c) {
				Node current = (Node) (nodes.elementAt(r * s + c));

				int nr;
				int nc;

				nr = r - 1;
				nc = c + 1;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				nr = r;
				nc = c + 1;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				nr = r + 1;
				nc = c;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				nr = r + 1;
				nc = c - 1;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				nr = r;
				nc = c - 1;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				nr = r - 1;
				nc = c;
				if (nr >= 0 && nr < s && nc >= 0 && nc < s) {
					current.neighbours.add(nodes.elementAt(nr * s + nc));
					addEdge(current, (Node) (nodes.elementAt(nr * s + nc)));
				}

				// add links to fixed nodes
				if (r == 0) {
					current.neighbours.add(nodes.elementAt((s * s - 1) + 1));
					addEdge(current, (Node) (nodes.elementAt((s * s - 1) + 1)));
				}
				if (r == s - 1) {
					current.neighbours.add(nodes.elementAt((s * s - 1) + 3));
					addEdge(current, (Node) (nodes.elementAt((s * s - 1) + 3)));
				}
				if (c == 0) {
					current.neighbours.add(nodes.elementAt((s * s - 1) + 2));
					addEdge(current, (Node) (nodes.elementAt((s * s - 1) + 2)));
				}
				if (c == s - 1) {
					current.neighbours.add(nodes.elementAt((s * s - 1) + 4));
					addEdge(current, (Node) (nodes.elementAt((s * s - 1) + 4)));
				}
			}

		}

		// invisible nodes to create a border, within which all other nodes will
		// stay
		Node tl = new Node(min_x - 80, min_y - 50, (s * s - 1) + 5);
		Node tr = new Node(max_x + 75, min_y - 50, (s * s - 1) + 6);
		Node bl = new Node(min_x - 80, max_y + 60, (s * s - 1) + 7);
		Node br = new Node(max_x + 75, max_y + 60, (s * s - 1) + 8);

		tl.fixed = true;
		tr.fixed = true;
		bl.fixed = true;
		br.fixed = true;

		addEdge(tl, bl);
		addEdge(tl, tr);
		addEdge(tr, br);
		addEdge(bl, br);

		/*
		 * for (int i = 0; i != nodes.size(); ++i) { Node current =
		 * (Node)(nodes.elementAt(i)); current.old_neighbours_size =
		 * current.neighbours.size(); }
		 */

	}

	public void addEdge(Node n1, Node n2) {
		if (!containsEdge(n1, n2))
			edges.add(new Edge(n1, n2));
	}

	public void removeEdge(Node n1, Node n2) {
		for (int i = 0; i != edges.size(); ++i) {
			Edge e = (Edge) (edges.elementAt(i));
			if ((e.node1 == n1 && e.node2 == n2) || (e.node1 == n2 && e.node2 == n1)) {
				edges.remove(e);
				break;
			}
		}
	}

	public boolean containsEdge(Node n1, Node n2) {
		for (int i = 0; i != edges.size(); ++i) {
			Edge e = (Edge) (edges.elementAt(i));
			if ((e.node1 == n1 && e.node2 == n2) || (e.node1 == n2 && e.node2 == n1)) {
				return true;
			}
		}
		return false;
	}

}
