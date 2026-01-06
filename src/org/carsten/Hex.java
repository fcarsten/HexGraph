/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Vector;
import java.awt.geom.*;

import Jama.*;

import static java.awt.Component.LEFT_ALIGNMENT;

/*
* Hex game controller for the graph representation
*
* This class performs the graph drawing algorithms on the
* hex game graph and controls the animation of the graph from
* one arrangement of the nodes to another.
*
*/

public class Hex implements ActionListener, HexContestant {

	Graph hex = null;
	public int size = 11;

	HexArea hexArea;
	JLabel label;

	String algorithm = "tfdpred";

//	public Vector<Node> deletedNodes = new Vector<>();

	// for animation purposes
	Timer timer;
	public int numberFrames = 15;
	public int currentFrame = numberFrames;

	public HexEngine engine;

	public int getGameMode() {
		return engine.getGameMode();
	}

	void buildUI(Container container) {
		double fps = 60;

		// How many milliseconds between frames?
		double delay = (fps > 0) ? (1000 / fps) : 100;

		// Set up a timer that calls this object's action handler.
		timer = new Timer((int) delay, this);
		timer.setInitialDelay(0);
		timer.setCoalesce(true);

		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		hexArea = new HexArea(this);
		container.add(hexArea);

		label = new JLabel();
		container.add(label);

		// Align the left edges of the components.
		hexArea.setAlignmentX(LEFT_ALIGNMENT);
		label.setAlignmentX(LEFT_ALIGNMENT); // unnecessary, but doesn't hurt
	}

	// begins the timer object that triggers each frame of animation
	public synchronized void startAnimation() {
		if (!timer.isRunning()) {
			// Start animating!
			timer.start();
			currentFrame = 0;
		}
	}

	// stops the timer object
	public synchronized void stopAnimation() {
		// Stop the animating thread.
		if (timer.isRunning()) {
			timer.stop();
		}
	}

	public Vector<Node> getNodes() {
		return hex.nodes;
	}

	public boolean canSwap() {
		return hexArea.swap;
	}

	// apply the current algorithm to the hex game graph and animates the
	// transition
	public void applyAlgorithm() {
		if (algorithm.equals("none"))
			return;
		if (algorithm.equals("fdpred")) {
			do_force(35.0, 10);
			startAnimation();
			return;
		}

		if (algorithm.equals("tutte") || algorithm.equals("tfdpred")) {
			// add fixed nodes to vector
			Vector<Node> nodes = hex.nodes;
			Vector<Node> fixedNodes = new Vector<>();
			for (int i = 0; i != nodes.size(); ++i) {
				Node current = (Node) (nodes.elementAt(i));
				if (current.fixed && !current.to_be_deleted) {
					fixedNodes.add(current);
				}
			}

			do_tutte(fixedNodes);

			if (algorithm.equals("tfdpred")) {
				do_force(35.0, 10);
			}

			startAnimation();

		}
	}

	// selects the graph drawing algorithm to be used to draw the hex board
	// the algorithm is applied if a game graph is on the screen
	public void setAlgorithm(String alg) {
		algorithm = alg;

		if (hex != null)
			applyAlgorithm();
	}

	// This is called by the timer object to perform the animation.
	// It determines the location of nodes in the current frame and calls for
	// the graph to be painted on the screen
	@Override
	public void actionPerformed(ActionEvent e) {

		double percentage = ((double) currentFrame) / ((double) numberFrames);
		Vector<Node> nodes = hex.nodes;

		if (hexArea.rearrange_nodes) {

			// nodes have been merged so we need to apply the graph drawing
			// algorithm
			hexArea.rearrange_nodes = false;
			applyAlgorithm();
			for (int i = 0; i != nodes.size(); ++i) {
				Node current = (Node) (nodes.elementAt(i));
				if (current.to_be_deleted) {
					Vector<Node> neighbours = current.neighbours;
					for (int j = 0; j != neighbours.size(); ++j) {
						Node n = (Node) (neighbours.elementAt(j));
						if (current.colour == n.colour && !n.to_be_deleted) {
							current.new_x_coord = n.new_x_coord;
							current.new_y_coord = n.new_y_coord;
						}
					}
				}
			}
		}

		for (int i = 0; i != nodes.size(); ++i) {
			Node current = (Node) (nodes.elementAt(i));

			// calculate location of node in current frame
			current.temp_x_coord = (int) ((double) current.x_coord
					+ percentage * ((double) (current.new_x_coord - current.x_coord)));
			current.temp_y_coord = (int) ((double) current.y_coord
					+ percentage * ((double) (current.new_y_coord - current.y_coord)));

		}

		// Display it.
		hexArea.saveImage();
		hexArea.repaint();

		if (currentFrame == numberFrames) {

			// nodes have been merged in the animation so update
			// the internal representation to reflect this
			for (int i = 0; i != nodes.size(); ++i) {
				Node current = (Node) (nodes.elementAt(i));
				Vector<Node> neighbours = current.neighbours;

				if (neighbours.size() == 0 && !current.fixed) {
					// remove meaningless node
					nodes.remove(current);
					--i;
					continue;
				}

				if (current.to_be_deleted) {
					for (int j = 0; j != neighbours.size(); ++j) {
						Node n = (Node) (neighbours.elementAt(j));
						Vector<Node> n_neighbours = n.neighbours;
						n_neighbours.remove(current);
						hex.removeEdge(current, n);
					}
//					deletedNodes.add(current);
					nodes.remove(current);
					--i;
				} else {
					current.x_coord = current.new_x_coord;
					current.y_coord = current.new_y_coord;

					Vector<Node> new_neighbours = current.new_neighbours;
					if (new_neighbours.size() != 0) {
						for (int j = 0; j != new_neighbours.size(); ++j) {
							Node n = (Node) (new_neighbours.elementAt(j));
							if (!neighbours.contains(n))
								neighbours.add(n);
							Vector<Node> n_neighbours = n.neighbours;
							if (!n_neighbours.contains(current))
								n_neighbours.add(current);

							hex.addEdge(current, n);
						}
						new_neighbours.clear();
					}
				}
			}

			stopAnimation();
		} else {
			// Advance animation frame.
			++currentFrame;
		}
	}

	// applys the tutte algorithm to the hex game graph
	public boolean do_tutte(Vector<Node> fixedNodes) {

		Vector<Node> remainingNodes = new Vector<>(hex.nodes);
		for (int i = 0; i != remainingNodes.size(); ++i) {
			Node n = (Node) (remainingNodes.elementAt(i));

			n.remainingNeighbours = new Vector<>(n.neighbours);
		}

		for (int i = 0; i != remainingNodes.size(); ++i) {
			Node current = (Node) (remainingNodes.elementAt(i));
			Vector<Node> neighbours = current.remainingNeighbours;

			if (current.to_be_deleted) {
				for (int j = 0; j != neighbours.size(); ++j) {
					Node n = (Node) (neighbours.elementAt(j));
					Vector<Node> n_neighbours = n.remainingNeighbours;
					n_neighbours.remove(current);
					// hex.removeEdge(current, n);
				}
				remainingNodes.remove(current);
				--i;
			} else {
				Vector<Node> new_neighbours = current.new_neighbours;
				if (new_neighbours.size() != 0) {
					for (int j = 0; j != new_neighbours.size(); ++j) {
						Node n = (Node) (new_neighbours.elementAt(j));
						if (!neighbours.contains(n))
							neighbours.add(n);
						Vector<Node> n_neighbours = n.remainingNeighbours;
						if (!n_neighbours.contains(current))
							n_neighbours.add(current);

						// hex.addEdge(current, n);
					}
				}
			}
		}

		if (fixedNodes.size() == remainingNodes.size())
			return false;
		// all nodes have fixed positions - nothing left to do

		int numNodes = remainingNodes.size() - fixedNodes.size();
		Vector<Node> tutteNodes = new Vector<>();

		for (int i = 0; i != remainingNodes.size(); ++i) {
			Node n = (Node) (remainingNodes.elementAt(i));
			if (!fixedNodes.contains(n)) {
				n.tutteValue = tutteNodes.size();
				tutteNodes.add(n);
			} else
				n.tutteValue = -1;
		}

		Matrix coord = null; // = new Matrix(numNodes,1);

		// coordinates (first x then y)

		Matrix rhs = new Matrix(numNodes, 1); // right hand side
		Matrix A = new Matrix(numNodes, numNodes); // equations

		for (int i = 0; i != numNodes; ++i) {
			Node v = (Node) (tutteNodes.elementAt(i));
			Vector<Node> neighbours = v.remainingNeighbours;
			double one_over_d = 1.0 / ((double) neighbours.size());

			for (int j = 0; j != neighbours.size(); ++j) {
				Node w = (Node) (neighbours.elementAt(j));
				if (w.tutteValue != -1) {
					A.set(v.tutteValue, w.tutteValue, one_over_d);
				}
			}
			A.set(v.tutteValue, v.tutteValue, -1);
		}

		if (A.det() == 0)
			return false; // should never happen

		// compute right hand side for x coordinates
		for (int i = 0; i != numNodes; ++i) {
			Node v = (Node) (tutteNodes.elementAt(i));

			rhs.set(i, 0, 0.0);
			Vector<Node> neighbours = v.remainingNeighbours;
			double one_over_d = 1.0 / ((double) neighbours.size());

			for (int j = 0; j != neighbours.size(); ++j) {
				Node w = (Node) (neighbours.elementAt(j));
				if (w.tutteValue == -1) {
					rhs.set(i, 0, rhs.get(i, 0) - one_over_d * w.new_x_coord);
				}
			}
		}

		// compute x coordinates
		coord = A.solve(rhs);
		for (int i = 0; i != numNodes; ++i) {
			Node v = (Node) (tutteNodes.elementAt(i));
			v.new_x_coord = coord.get(v.tutteValue, 0);
		}

		// compute right hand side for y coordinates
		for (int i = 0; i != numNodes; ++i) {
			Node v = (Node) (tutteNodes.elementAt(i));

			rhs.set(i, 0, 0.0);
			Vector<Node> neighbours = v.remainingNeighbours;
			double one_over_d = 1.0 / ((double) neighbours.size());

			for (int j = 0; j != neighbours.size(); ++j) {
				Node w = (Node) (neighbours.elementAt(j));
				if (w.tutteValue == -1) {
					rhs.set(i, 0, rhs.get(i, 0) - one_over_d * w.new_y_coord);
				}
			}
		}

		// compute y coordinates
		coord = A.solve(rhs);
		for (int i = 0; i != numNodes; ++i) {
			Node v = (Node) (tutteNodes.elementAt(i));
			v.new_y_coord = coord.get(v.tutteValue, 0);
		}

		return true;
	}

	// finds the projection of v onto the edge ab
	public Point2D.Double virtualNode(Node v, Node a, Node b, double t0) {
		Point2D.Double iv = new Point2D.Double();

		double Mx = b.new_x_coord - a.new_x_coord;
		double My = b.new_y_coord - a.new_y_coord;

		iv.x = a.new_x_coord + t0 * Mx;
		iv.y = a.new_y_coord + t0 * My;

		return iv;
	}

	// finds the value which indicates whether the projection lies on the edge
	public double get_t0(Node v, Node a, Node b) {
		double Mx = b.new_x_coord - a.new_x_coord;
		double My = b.new_y_coord - a.new_y_coord;

		double P_Bx = v.new_x_coord - a.new_x_coord;
		double P_By = v.new_y_coord - a.new_y_coord;

		double t0 = (Mx * P_Bx + My * P_By) / (Mx * Mx + My * My);
		return t0;
	}

	// calculates the force between the node v and the edge ab
	public Point2D.Double forceBetween(Node v, Node a, Node b, double range) {
		Point2D.Double result = new Point2D.Double();

		if (v != a && v != b) {
			double t0 = get_t0(v, a, b);
			// if iv lies on the line ab
			if (t0 >= 0 && t0 <= 1) {
				// find projection node iv
				Point2D.Double iv = virtualNode(v, a, b, t0);
				double distance = Math.sqrt((double) ((v.new_x_coord - iv.x) * (v.new_x_coord - iv.x)
						+ (v.new_y_coord - iv.y) * (v.new_y_coord - iv.y)));
				if (distance < range) {
					// calculate force
					if (distance == 0) {
						// v lies on the line ab but the node v is not equal to
						// the nodes a or b
						// edge should strongly repel the node

						double f = -(range * range / 16.0);
						result.x = f;
						result.y = f;
					} else {
						double f = -(((range - distance) * (range - distance)) / distance);
						result.x = f * (iv.x - v.new_x_coord);
						result.y = f * (iv.y - v.new_y_coord);
					}

					// check that values are legitimate and provide useful
					// information if they are not
					if (Double.isNaN(result.x)) {
						System.out.println("FBx is NaN");
						System.out.println("distance: " + distance);
						System.out.println("range: " + range);
						System.out.println("iv.x: " + iv.x);
						System.out.println("v.x: " + v.new_x_coord);
						System.out.println("");
					}
					if (Double.isInfinite(result.x)) {
						System.out.println("FBx is Infinite");
						System.out.println("distance: " + distance);
						System.out.println("range: " + range);
						System.out.println("iv.x: " + iv.x);
						System.out.println("v.x: " + v.new_x_coord);
						System.out.println("");
					}
					if (Double.isNaN(result.y)) {
						System.out.println("FBy is NaN");
						System.out.println("distance: " + distance);
						System.out.println("range: " + range);
						System.out.println("iv.y: " + iv.y);
						System.out.println("v.y: " + v.new_y_coord);
						System.out.println("");
					}
					if (Double.isInfinite(result.y)) {
						System.out.println("FBy is Infinite");
						System.out.println("distance: " + distance);
						System.out.println("range: " + range);
						System.out.println("iv.y: " + iv.y);
						System.out.println("v.y: " + v.new_y_coord);
						System.out.println("");
					}
				}
			}
		}

		return result;
	}

	// determines which arc of a nodes zone that a ray lies in
	public int arc(double vx, double vy, double ivx, double ivy) {
		int result = -1;

		if (ivy <= vy && ivx > vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m > -1 && m <= 0)
				result = 0;
		}

		if (ivy < vy && ivx > vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m <= -1)
				result = 1;
		}

		if (ivy < vy && ivx <= vx) {
			if (ivx == vx)
				result = 2;
			else {
				double m = (ivy - vy) / (ivx - vx);
				if (m > 1)
					result = 2;
			}
		}

		if (ivy < vy && ivx < vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m <= 1 && m > 0)
				result = 3;
		}

		if (ivy >= vy && ivx < vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m > -1 && m <= 0)
				result = 4;
		}

		if (ivy > vy && ivx < vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m <= -1)
				result = 5;
		}

		if (ivy > vy && ivx >= vx) {
			if (ivx == vx)
				result = 6;
			else {
				double m = (ivy - vy) / (ivx - vx);
				if (m > 1)
					result = 6;
			}
		}

		if (ivy > vy && ivx > vx) {
			double m = (ivy - vy) / (ivx - vx);
			if (m <= 1 && m > 0)
				result = 7;
		}

		if (result == -1) {
			// will never get here with valid values
			System.out.println("v: x=" + vx + " y=" + vy + " iv: x=" + ivx + " y=" + ivy);
			System.out.println("Zone not found");

			return 0;
		}

		return result;
	}

	public void removeEdge(Vector<Edge> edges, Node n1, Node n2) {
		for (int i = 0; i != edges.size(); ++i) {
			Edge e = (Edge) (edges.elementAt(i));
			if ((e.node1 == n1 && e.node2 == n2) || (e.node1 == n2 && e.node2 == n1)) {
				edges.remove(e);
				break;
			}
		}
	}

	public boolean containsEdge(Vector<Edge> edges, Node n1, Node n2) {
		for (int i = 0; i != edges.size(); ++i) {
			Edge e = (Edge) (edges.elementAt(i));
			if ((e.node1 == n1 && e.node2 == n2) || (e.node1 == n2 && e.node2 == n1)) {
				return true;
			}
		}
		return false;
	}

	// applys force directed algorithm that preserves edge crossing properties
	public boolean do_force(double delta, int numberOfIterations) {
		Vector<Node> remainingNodes = new Vector<>(hex.nodes);
		Vector<Edge> remainingEdges = new Vector<>(hex.edges);
		for (int i = 0; i != remainingNodes.size(); ++i) {
			Node n = (Node) (remainingNodes.elementAt(i));

			n.remainingNeighbours = new Vector<>(n.neighbours);
		}
		for (int i = 0; i != remainingNodes.size(); ++i) {
			Node current = (Node) (remainingNodes.elementAt(i));
			Vector<Node> neighbours = current.remainingNeighbours;

			if (current.to_be_deleted) {
				for (int j = 0; j != neighbours.size(); ++j) {
					Node n = (Node) (neighbours.elementAt(j));
					Vector<Node> n_neighbours = n.remainingNeighbours;
					n_neighbours.remove(current);
					removeEdge(remainingEdges, current, n);
				}
				remainingNodes.remove(current);
				--i;
			} else {
				Vector<Node> new_neighbours = current.new_neighbours;
				if (new_neighbours.size() != 0) {
					for (int j = 0; j != new_neighbours.size(); ++j) {
						Node n = (Node) (new_neighbours.elementAt(j));
						if (!neighbours.contains(n))
							neighbours.add(n);
						Vector<Node> n_neighbours = n.remainingNeighbours;
						if (!n_neighbours.contains(current))
							n_neighbours.add(current);

						if (!containsEdge(remainingEdges, current, n))
							remainingEdges.add(new Edge(current, n));
						// hex.addEdge(current, n);
					}
				}
			}
		}

		double range = delta * 4.0;
		Node nodes[] = new Node[remainingNodes.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = (Node) remainingNodes.elementAt(i);
		}
		Edge edges[] = new Edge[remainingEdges.size()];
		for (int i = 0; i < edges.length; i++) {
			edges[i] = (Edge) remainingEdges.elementAt(i);
		}

		// for each iteration
		for (int currentIter = 0; currentIter != numberOfIterations; ++currentIter) {
			for (int i = 0; i != nodes.length; ++i) {
				Node v = nodes[i];
				if (!v.fixed) {

					// calculate the force applied
					double fx = 0;
					double fy = 0;

					// attraction force between connected nodes
					Vector<Node> neighbours = v.remainingNeighbours;
					for (int j = 0; j != neighbours.size(); ++j) {
						Node u = (Node) (neighbours.elementAt(j));

						double distance = Math
								.sqrt((double) ((u.new_x_coord - v.new_x_coord) * (u.new_x_coord - v.new_x_coord)
										+ (u.new_y_coord - v.new_y_coord) * (u.new_y_coord - v.new_y_coord)));
						double afactor = distance / delta;

						fx += afactor * (v.new_x_coord - u.new_x_coord);
						fy += afactor * (v.new_y_coord - u.new_y_coord);

						// check that values are legitimate and provide useful
						// information if they are not
						if (Double.isNaN(fx)) {
							System.out.println("AFx is NaN");
							System.out.println("distance: " + distance);
							System.out.println("delta: " + delta);
							System.out.println("afactor: " + afactor);
							System.out.println("v.x: " + v.new_x_coord);
							System.out.println("u.x: " + u.new_x_coord);
							System.out.println("");
						}
						if (Double.isInfinite(fx)) {
							System.out.println("AFx is Infinite");
							System.out.println("distance: " + distance);
							System.out.println("delta: " + delta);
							System.out.println("afactor: " + afactor);
							System.out.println("v.x: " + v.new_x_coord);
							System.out.println("u.x: " + u.new_x_coord);
							System.out.println("");
						}
						if (Double.isNaN(fy)) {
							System.out.println("AFy is NaN");
							System.out.println("distance: " + distance);
							System.out.println("delta: " + delta);
							System.out.println("afactor: " + afactor);
							System.out.println("v.y " + v.new_y_coord);
							System.out.println("u.y: " + u.new_y_coord);
							System.out.println("");
						}
						if (Double.isInfinite(fy)) {
							System.out.println("AFy is Infinite");
							System.out.println("distance: " + distance);
							System.out.println("delta: " + delta);
							System.out.println("afactor: " + afactor);
							System.out.println("v.y: " + v.new_y_coord);
							System.out.println("u.y: " + u.new_y_coord);
							System.out.println("");
						}

					}

					// repulsion force between nodes
					for (int j = 0; j != nodes.length; ++j) {
						Node u = nodes[j];

						if (u != v) {
							double distance = Math
									.sqrt((double) ((u.new_x_coord - v.new_x_coord) * (u.new_x_coord - v.new_x_coord)
											+ (u.new_y_coord - v.new_y_coord) * (u.new_y_coord - v.new_y_coord)));
							if (distance == 0) {
								// two different nodes in the exact same
								// location
								// should repel each other strongly

								fx = -(delta * delta);
								fy = -(delta * delta);
							} else {

								double rfactor = (-(delta * delta)) / (distance * distance);

								fx += rfactor * (v.new_x_coord - u.new_x_coord);
								fy += rfactor * (v.new_y_coord - u.new_y_coord);

								// check that values are legitimate and provide
								// useful information if they are not
								if (Double.isNaN(fx)) {
									System.out.println("RFx is NaN");
									System.out.println("distance: " + distance);
									System.out.println("delta: " + delta);
									System.out.println("rfactor: " + rfactor);
									System.out.println("v.x: " + v.new_x_coord);
									System.out.println("u.x: " + u.new_x_coord);
									System.out.println("");
								}
								if (Double.isInfinite(fx)) {
									System.out.println("RFx is Infinite");
									System.out.println("distance: " + distance);
									System.out.println("delta: " + delta);
									System.out.println("rfactor: " + rfactor);
									System.out.println("v.x: " + v.new_x_coord);
									System.out.println("u.x: " + u.new_x_coord);
									System.out.println("");
								}
								if (Double.isNaN(fy)) {
									System.out.println("RFy is NaN");
									System.out.println("distance: " + distance);
									System.out.println("delta: " + delta);
									System.out.println("rfactor: " + rfactor);
									System.out.println("v.y " + v.new_y_coord);
									System.out.println("u.y: " + u.new_y_coord);
									System.out.println("");
								}
								if (Double.isInfinite(fy)) {
									System.out.println("RFy is Infinite");
									System.out.println("distance: " + distance);
									System.out.println("delta: " + delta);
									System.out.println("rfactor: " + rfactor);
									System.out.println("v.y: " + v.new_y_coord);
									System.out.println("u.y: " + u.new_y_coord);
									System.out.println("");
								}
							}
						}

					}

					// force between node and edges
					for (int j = 0; j != edges.length; ++j) {
						Edge e = edges[j];

						Point2D.Double fev = forceBetween(v, e.node1, e.node2, range);

						fx += fev.x;
						fy += fev.y;

						// check that values are legitimate and provide useful
						// information if they are not
						if (Double.isNaN(fx)) {
							System.out.println("NEFx is NaN");
							System.out.println("fev.x: " + fev.x);
							System.out.println("");
						}
						if (Double.isInfinite(fx)) {
							System.out.println("NEFx is Infinite");
							System.out.println("fev.x: " + fev.x);
							System.out.println("");
						}
						if (Double.isNaN(fy)) {
							System.out.println("NEFy is NaN");
							System.out.println("fev.y: " + fev.y);
							System.out.println("");
						}
						if (Double.isInfinite(fy)) {
							System.out.println("NEFy is Infinite");
							System.out.println("fev.y: " + fev.y);
							System.out.println("");
						}
					}

					// force between the current nodes connected edges and other
					// nodes
					for (int j = 0; j != nodes.length; ++j) {
						Node u = nodes[j];

						if (u != v) {
							for (int k = 0; k != neighbours.size(); ++k) {
								Node w = (Node) (neighbours.elementAt(k));

								Point2D.Double fev = forceBetween(u, v, w, range);

								fx -= fev.x;
								fy -= fev.y;

								// check that values are legitimate and provide
								// useful information if they are not
								if (Double.isNaN(fx)) {
									System.out.println("NE2Fx is NaN");
									System.out.println("fev.x: " + fev.x);
									System.out.println("");
								}
								if (Double.isInfinite(fx)) {
									System.out.println("NE2Fx is Infinite");
									System.out.println("fev.x: " + fev.x);
									System.out.println("");
								}
								if (Double.isNaN(fy)) {
									System.out.println("NE2Fy is NaN");
									System.out.println("fev.y: " + fev.y);
									System.out.println("");
								}
								if (Double.isInfinite(fy)) {
									System.out.println("NE2Fy is Infinite");
									System.out.println("fev.y: " + fev.y);
									System.out.println("");
								}
							}
						}
					}

					v.force.x = fx;
					v.force.y = fy;

					// computation of the values of the zone of each node
					for (int z = 0; z != 8; ++z) {
						v.zone[z] = Double.POSITIVE_INFINITY;
					}
					for (int j = 0; j != edges.length; ++j) {
						Edge e = edges[j];
						Node a = e.node1;
						Node b = e.node2;

						if (v != a && v != b) {
							double t0 = get_t0(v, a, b);
							// if iv lies on the line ab
							if (t0 >= 0 && t0 <= 1) {
								// find projection node iv
								Point2D.Double iv = virtualNode(v, a, b, t0);
								double distance_viv = Math
										.sqrt((double) ((v.new_x_coord - iv.x) * (v.new_x_coord - iv.x)
												+ (v.new_y_coord - iv.y) * (v.new_y_coord - iv.y)));

								if (distance_viv == 0) {
									// v lies on the line ab but the node v is
									// not equal to the nodes a or b
									// no reason to reduce zones in this case
								} else {

									distance_viv = distance_viv / 3;
									int s = arc(v.new_x_coord, v.new_y_coord, iv.x, iv.y);

									int z = s - 2;
									if (z == -1)
										z = 7;
									if (z == -2)
										z = 6;
									for (int c = 0; c != 5; ++c) {
										double Rv = v.zone[z];
										if (distance_viv < Rv)
											v.zone[z] = distance_viv;

										++z;
										if (z == 8)
											z = 0;
									}

									z = s + 2;
									if (z == 8)
										z = 0;
									if (z == 9)
										z = 1;
									for (int c = 0; c != 5; ++c) {
										double Ra = a.zone[z];
										if (distance_viv < Ra)
											a.zone[z] = distance_viv;

										double Rb = b.zone[z];
										if (distance_viv < Rb)
											b.zone[z] = distance_viv;

										++z;
										if (z == 8)
											z = 0;
									}
								}
							} else {
								double distance_av = Math.sqrt(
										(double) ((a.new_x_coord - v.new_x_coord) * (a.new_x_coord - v.new_x_coord)
												+ (a.new_y_coord - v.new_y_coord) * (a.new_y_coord - v.new_y_coord)));
								double distance_bv = Math.sqrt(
										(double) ((b.new_x_coord - v.new_x_coord) * (b.new_x_coord - v.new_x_coord)
												+ (b.new_y_coord - v.new_y_coord) * (b.new_y_coord - v.new_y_coord)));

								distance_av = distance_av / 3;
								distance_bv = distance_bv / 3;
								double min_dist = distance_av;
								if (distance_bv < distance_av)
									min_dist = distance_bv;

								for (int z = 0; z != 8; ++z) {
									double Rv = v.zone[z];
									if (min_dist < Rv)
										v.zone[z] = min_dist;

									double Ra = a.zone[z];
									if (distance_av < Ra)
										a.zone[z] = distance_av;

									double Rb = b.zone[z];
									if (distance_bv < Rb)
										b.zone[z] = distance_bv;

								}
							}
						}
					}
				}
			}

			// move each node in direction of it's force while preserving
			// edge crossing properties by not moving outside its zone
			for (int i = 0; i != nodes.length; ++i) {
				Node v = nodes[i];
				if (!v.fixed) {
					int z = arc(0.0, 0.0, v.force.x, v.force.y);

					double Rv = v.zone[z];
					double forceMag = Math.sqrt((v.force.x) * (v.force.x) + (v.force.y) * (v.force.y));

					if (forceMag <= Rv) {
						v.new_x_coord += v.force.x;
						v.new_y_coord += v.force.y;
					} else {
						v.new_x_coord += ((Rv / forceMag) * (v.force.x));
						v.new_y_coord += ((Rv / forceMag) * (v.force.y));
					}
				}
			}
		}

		return true;
	}

	// sets up gui
	public Hex(HexEngine e) {
		engine = e;

		MenuFrame f = new MenuFrame(e, this);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		buildUI(f.getContentPane());
		f.setTitle("Hex");
		f.pack();
		f.setSize(new Dimension(1024, 768));
		f.setVisible(true);
	}

	public boolean isExpectingMove() {
		return (hexArea.isExpectingMove() && (!timer.isRunning()));
	}

	public void doMove(Node n) {
		hexArea.setExpectingMove(false);

		if (engine.getGameMode() != HexEngine.HUMAN_HUMAN) {
			hexArea.nodeSelected(n);
			hexArea.saveImage();
			hexArea.repaint();
		}

		engine.doMove(new Coord(10 - (n.identifier / 11), 10 - (n.identifier % 11)));
	}

	@Override
	public void yourTurn(Coord move) {
		hexArea.nodeSelected(move);
	}

	private int playerNumber = -1;

	@Override
	public boolean initGame(int n) {
		playerNumber = n;

		if ((n == HexEngine.PLAYER_ONE) || (engine.getGameMode() != HexEngine.HUMAN_HUMAN)) {
			hex = new Graph(size, 1024, 768);
			hexArea.initGame(hex, n);
		}

		return true;
	}

	@Override
	public void stopGame(int player) {
		hexArea.stopGame(player);
	}

	@Override
	public void youWin(Coord winningMove) {
		hexArea.stopGame(-1);
		if (engine.getGameMode() == HexEngine.HUMAN_HUMAN) {
			JOptionPane.showConfirmDialog(null,
					((engine.getCurrentPlayer() == HexEngine.PLAYER_ONE) ? "Red" : "Blue") + " wins", "Result",
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showConfirmDialog(null, "You win", "Result", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

	@Override
	public void youLose(Coord winningMove) {
		hexArea.nodeSelected(winningMove);
		if (engine.getGameMode() != HexEngine.HUMAN_HUMAN) {
			JOptionPane.showConfirmDialog(null, "You lose", "Result", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void exit() {
		if ((engine.getGameMode() == HexEngine.NETWORK_HUMAN) || (engine.getGameMode() == HexEngine.HUMAN_NETWORK))
			resign();
		System.exit(0);
	}

	public void resign() {
		if (!engine.isGameRunning())
			return;

		if (engine.getGameMode() == HexEngine.HUMAN_HUMAN) {
			playerNumber = engine.getCurrentPlayer(); // For human_human
														// playerNumber is
														// irrelevant as we are
														// both players
		}
		if (playerNumber == HexEngine.PLAYER_ONE)
			engine.doMove(HexEngine.resign_p1);
		else
			engine.doMove(HexEngine.resign_p2);
	}

}
