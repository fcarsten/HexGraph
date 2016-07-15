/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

// The panel on which the graph is drawn. When a node is selected it is handled by this class.
public class HexArea extends JPanel {
	Hex controller;
	private boolean expectingMove = false;
	private boolean gameOver = false;

	public Graph hex;
	public String view = "neutral";
	public Color currentColour = Color.red;

	public boolean swap = true;
	public boolean toBeMerged = false;
	public Node firstSelected = null;
	public boolean edgeToBeRemoved = false;

	public boolean rearrange_nodes = false;
	private int playerNumber;

	public boolean swapRule = true;
	public boolean nodesOn = true;

	public boolean saveImages = false;
	public int imageNumber = 1;

	public void nodeSelected(Coord c) {
		if (c.i == -1) {
			setExpectingMove(true);
		} else
			nodeSelected((10 - c.i) * 11 + (10 - c.j));
	}

	public void stopGame(int n) {
		expectingMove = false;
		gameOver = true;
	}

	public void initGame(Graph h, int n) {
		hex = h;
		playerNumber = n;
		gameOver = false;

		if (swapRule)
			swap = true;
		toBeMerged = false;
		firstSelected = null;
		edgeToBeRemoved = false;
		currentColour = Color.red;

		saveImage();
		repaint();

	}

	public HexArea(Hex controller) {
		this.controller = controller;

		Border raisedBevel = BorderFactory.createRaisedBevelBorder();
		Border loweredBevel = BorderFactory.createLoweredBevelBorder();
		Border compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);
		setBorder(compound);

		addMouseListener(new HexMouseAdapter(controller));
	}

	public boolean isExpectingMove() {
		return expectingMove;
	}

	// uses an int rather than a node object to identify the selected node,
	// makes networking easier
	public void nodeSelected(int node) {
		// find the node that corresponds to the given int
		expectingMove = true;

		Vector<Node> nodes = hex.nodes;
		for (int i = 0; i != nodes.size(); ++i) {
			Node current = (Node) (nodes.elementAt(i));
			if (current.identifier == node) {
				nodeSelected(current);
				break;
			}
		}
	}

	// directly uses the node object that was selected
	// merges nodes if necessary
	public void nodeSelected(Node current) {
		if (current.colour != Color.black) {
			toBeMerged = false;
			edgeToBeRemoved = false;
			current.colour = Color.black;
			int currenti = current.identifier / hex.size;
			int currentj = current.identifier % hex.size;
			nodeSelected(currentj * hex.size + currenti);
			return;
		}
		current.colour = currentColour;
		saveImage();
		repaint();
		if (currentColour == Color.red) {
			if (swap == true)
				firstSelected = current;

			currentColour = Color.blue;
		} else {
			currentColour = Color.red;
			if (swap == true) {
				swap = false;
				if (toBeMerged) {
					// merge it
					firstSelected.to_be_deleted = true;

					Vector<Node> neighbours = firstSelected.neighbours;
					for (int i = 0; i != neighbours.size(); ++i) {
						Node n = (Node) (neighbours.elementAt(i));
						if (n.fixed && (n.colour == firstSelected.colour)) {
							firstSelected.new_x_coord = n.x_coord;
							firstSelected.new_y_coord = n.y_coord;

							Vector<Node> fixedNeighbours = n.neighbours;
							Vector<Node> newNeighbours = n.new_neighbours;
							for (int j = 0; j != neighbours.size(); ++j) {
								Node new_n = (Node) (neighbours.elementAt(j));

								if (!(fixedNeighbours.contains(new_n))) {
									if (new_n != n) {
										if (firstSelected.colour == Color.red) {
											if (new_n.colour != Color.blue) {
												newNeighbours.add(new_n);
											}
										}
									}
								}
							}
						}
					}
				}

				if (edgeToBeRemoved) {
					Vector<Node> neighbours = firstSelected.neighbours;
					for (int i = 0; i != neighbours.size(); ++i) {
						Node n = (Node) (neighbours.elementAt(i));
						if (n.colour == Color.blue) {
							neighbours.remove(n);
							--i;
							n.neighbours.remove(firstSelected);
							hex.removeEdge(n, firstSelected);
						}
					}
				}
			}
		}

		Point2D.Double location = new Point2D.Double(current.x_coord, current.y_coord);
		int numberOfNodesMerged = 1;

		Vector<Node> neighbours = current.neighbours;
		for (int j = 0; j != neighbours.size(); ++j) {
			Node n = (Node) (neighbours.elementAt(j));
			if (n.colour == current.colour) {
				location.x += n.x_coord;
				location.y += n.y_coord;
				if (current.fixed && n.fixed) {
					// winner = true;
					continue;
				} else {
					// merge
					if (swap == true) {
						toBeMerged = true;
						return;
					} else {
						++numberOfNodesMerged;
						n.to_be_deleted = true;
					}
				}
				if (n.fixed) {
					current.fixed = true;
					current.nodeRadius = n.nodeRadius;
					current.new_x_coord = n.x_coord;
					current.new_y_coord = n.y_coord;
				}

				Vector<Node> n_neighbours = n.neighbours;
				Vector<Node> new_neighbours = current.new_neighbours;
				for (int k = 0; k != n_neighbours.size(); ++k) {
					Node new_n = (Node) (n_neighbours.elementAt(k));

					if (!(neighbours.contains(new_n))) {
						if (new_n != current) {
							new_neighbours.add(new_n);
						}
					}
				}
			} else {
				if (n.colour != Color.black) {
					if (swap == false) {
						neighbours.remove(n);
						--j;
						n.neighbours.remove(current);
						hex.removeEdge(n, current);
					} else {
						edgeToBeRemoved = true;
					}
				}
			}
		}

		if (!current.fixed) {
			current.new_x_coord = location.x / numberOfNodesMerged;
			current.new_y_coord = location.y / numberOfNodesMerged;
		}

		if (numberOfNodesMerged > 1) {
			for (int j = 0; j != neighbours.size(); ++j) {
				Node n = (Node) (neighbours.elementAt(j));
				if (n.to_be_deleted) {
					n.new_x_coord = current.new_x_coord;
					n.new_y_coord = current.new_y_coord;
				}
			}

			if (toBeMerged)
				toBeMerged = false;

			rearrange_nodes = true;
			controller.startAnimation();
		} else {
			if (swap == false && toBeMerged) {
				toBeMerged = false;
				rearrange_nodes = true;
				controller.startAnimation();
			}
		}
		saveImage();
		repaint();

	}

	public void setExpectingMove(boolean b) {
		expectingMove = b;
	}

	public void saveImage() {
		// if (saveImages)
		// {
		// OutputStream os = null;
		// try
		// {
		// os = new FileOutputStream("pics\\" + Integer.toString(imageNumber) +
		// ".jpg");
		// // saveImage(ImageCodec.createImageEncoder("jpeg", os, null));
		// ImageEncoder tie = ImageCodec.createImageEncoder("jpeg", os, null);
		// BufferedImage bi = new BufferedImage(1024, 768,
		// BufferedImage.TYPE_3BYTE_BGR);

		// Graphics2D tmpG = bi.createGraphics();
		// paintComponent(tmpG);

		// tie.encode(bi);
		// }
		// catch (java.io.IOException except)
		// {
		// System.err.println("Couldn save file " + except.getMessage());
		// }
		// finally
		// {
		// try
		// {
		// if(os != null)
		// {
		// os.flush();
		// os.close();
		// ++imageNumber;
		// }
		// }
		// catch (java.io.IOException except){}
		// }
		// }
	}

	// displays the graph on the screen
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // paint background

		if (hex == null)
			return;

		Color perspective = Color.black;
		if (view.equals("red"))
			perspective = Color.red;
		else if (view.equals("blue")) {
			perspective = Color.blue;
		} else if (view.equals("player")) {
			if (!controller.timer.isRunning() || rearrange_nodes == false) {
				perspective = currentColour;
			} else {
				if (currentColour == Color.red)
					perspective = Color.blue;
				else
					perspective = Color.red;
			}
		}

		Vector<Node> nodes = hex.nodes;

		// draw edges
		for (int i = 0; i != nodes.size(); ++i) {
			Node current = (Node) (nodes.elementAt(i));
			Vector<Node> neighbours = current.neighbours;
			for (int j = 0; j != neighbours.size(); ++j) {
				Node n = (Node) (neighbours.elementAt(j));

				g.setColor(Color.black);
				if (perspective == Color.blue) {
					if (n.colour == Color.blue || current.colour == Color.blue)
						g.setColor(Color.blue);
					if (n.colour == Color.red || current.colour == Color.red)
						continue;
				}
				if (perspective == Color.red) {
					if (n.colour == Color.red || current.colour == Color.red)
						g.setColor(Color.red);
					if (n.colour == Color.blue || current.colour == Color.blue)
						continue;
				}
				if (perspective == Color.black) {
					if (n.colour == Color.blue || current.colour == Color.blue)
						g.setColor(Color.blue);
					if (n.colour == Color.red || current.colour == Color.red)
						g.setColor(Color.red);
					if ((n.colour == Color.red && current.colour == Color.blue)
							|| (n.colour == Color.blue && current.colour == Color.red))
						g.setColor(Color.black);
				}

				g.drawLine((int) current.temp_x_coord, (int) current.temp_y_coord, (int) n.temp_x_coord,
						(int) n.temp_y_coord);
			}
		}

		// draw nodes
		if (nodesOn) {
			for (int i = 0; i != nodes.size(); ++i) {
				Node current = (Node) (nodes.elementAt(i));

				if (perspective == Color.blue && current.colour == Color.red) {
					if (!swap || (current.fixed || controller.timer.isRunning()))
						continue;
				}

				if (perspective == Color.red && current.colour == Color.blue)
					continue;

				g.setColor(current.colour);
				double radius = current.nodeRadius;
				if (current.fixed) {
					if ((current.x_coord != current.new_x_coord || current.y_coord != current.new_y_coord)) {
						radius /= 2;
					}
				}

				g.fillOval((int) (current.temp_x_coord - radius), (int) (current.temp_y_coord - radius),
						(int) (radius * 2), (int) (radius * 2));
			}

		}

		// testing edges
		/*
		 * Vector edges = hex.edges; for (int i = 0; i != edges.size(); ++i) {
		 * Edge e = (Edge)(edges.elementAt(i)); Node current = e.node1; Node n =
		 * e.node2; g.setColor(Color.yellow);
		 * g.drawLine((int)current.temp_x_coord, (int)current.temp_y_coord,
		 * (int)n.temp_x_coord, (int)n.temp_y_coord); }
		 */

		// set label
		if (controller.engine.getGameMode() == HexEngine.HUMAN_HUMAN) {

			String labelText = null;

			if (controller.currentFrame != controller.numberFrames || rearrange_nodes) {
				if (rearrange_nodes) {
					labelText = "Merging Nodes";
				} else {
					labelText = "Moving Nodes";
				}
			} else {
				if (expectingMove) {
					if (currentColour == Color.red)
						labelText = "Player 1's Turn";
					else {
						labelText = "Player 2's Turn";
						if (swap) {
							labelText = "Player 2's Turn, swap available";
						}
					}
				} else {
					if (gameOver)
						labelText = "Game Over";
				}
			}

			if (labelText != null && !(labelText.equals(controller.label.getText()))) {
				controller.label.setText(labelText);
			}
		} else {
			controller.label.setText("");
		}
	}
}
