/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class HexPanel extends JPanel implements MouseListener, ActionListener, HexContestant {
	private int hexDimX = -1;
	private int hexDimY = -1;
	private int offsetY = 0;
	private int offsetX = 0;

	private double hexSideH = 20;
	private double hexSideV = 20;
	private double hexH = Math.sin(Math.toRadians(30)) * hexSideH;
	private double hexR = Math.cos(Math.toRadians(30)) * hexSideV;
	private double hexB = hexSideH + 2 * hexH;
	private double hexA = 2 * hexR;
	private double hexC = hexSideH + hexH;

	private static Color green = new Color(50, 150, 50);
	private static Color red = new Color(150, 50, 50);
	private static Color blue = new Color(50, 50, 150);
	private static Color grey = new Color(50, 50, 50);

	private static Color colors[] = { Color.white, red, blue };

	private boolean expectingMove = false;
	private HexEngine engine;
	private int playerNumber = 0;

	@Override
	public void youLose(Coord last) {
		repaint();
		expectingMove = false;
		swapButton.setEnabled(false);

		if (engine.getGameMode() != HexEngine.HUMAN_HUMAN) {
			JOptionPane.showConfirmDialog(null, "You lose", "Result", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void youWin(Coord last) {
		repaint();
		expectingMove = false;
		swapButton.setEnabled(false);

		if (engine.getGameMode() == HexEngine.HUMAN_HUMAN) {
			JOptionPane.showConfirmDialog(null,
					((engine.getCurrentPlayer() == HexEngine.PLAYER_ONE) ? "Red" : "Blue") + " wins", "Result",
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showConfirmDialog(null, "You win", "Result", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public HexPanel(HexEngine e) {
		engine = e; // new HexEngine(n);
		int n = engine.getBoardSize();

		hexDimX = n;
		hexDimY = n + n / 2;

		addMouseListener(this);

		setPreferredSize(new Dimension((int) (hexDimX * hexC + hexH), (int) (hexDimY * hexA)));
		setSize(new Dimension((int) (hexDimX * hexC + hexH), (int) (hexDimY * hexA)));

		JFrame frame = new JFrame("Hex Game");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		frame.setJMenuBar(buildMenu());

		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
		// initGame();
		expectingMove = false;

	}

	JButton swapButton = new JButton("Swap");

	private JMenuBar buildMenu() {
		//
		// Creating the Main Menu
		//

		JMenuBar menuBar;
		JMenu menu;
		JMenuItem item;

		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Game");
		menu.getAccessibleContext().setAccessibleDescription("File");
		menuBar.add(menu);

		item = new JMenuItem("New against Computer");
		item.setActionCommand("newComp");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("New against Human");
		item.setActionCommand("newHuman");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("New over Network");
		item.setActionCommand("newNetwork");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("Resign");
		item.setActionCommand("resign");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("Exit");
		item.setActionCommand("exit");
		item.addActionListener(this);
		menu.add(item);

		menuBar.add(swapButton);
		swapButton.addActionListener(this);
		swapButton.setEnabled(false);

		return menuBar;
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
			doMove(HexEngine.resign_p1);
		else
			doMove(HexEngine.resign_p2);
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (e.getSource() == swapButton) {
			doMove(HexEngine.swap);
		}
		if (e.getActionCommand().equals("newHuman")) {
			resign();
			engine.initGame(HexEngine.HUMAN_HUMAN);
		} else if (e.getActionCommand().equals("newComp")) {
			resign();
			engine.initGame(HexEngine.HUMAN_COMPUTER);
		} else if (e.getActionCommand().equals("newNetwork")) {
			resign();
			engine.initGame(HexEngine.NETWORK_HUMAN);
		} else if (e.getActionCommand().equals("resign")) {
			resign();
		} else if (e.getActionCommand().equals("exit")) {
			exit();
		}

	}

	private void exit() {
		if ((engine.getGameMode() == HexEngine.NETWORK_HUMAN) || (engine.getGameMode() == HexEngine.HUMAN_NETWORK))
			resign();
		System.exit(0);
	}

	@Override
	public boolean initGame(int n) {
		expectingMove = false;
		swapButton.setEnabled(false);
		playerNumber = n;

		repaint();
		return true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		computeSizes();

		Graphics2D g2d = (Graphics2D) g;

		int h = (int) (hexDimY * hexA);
		int w = (int) (hexDimX * hexC + hexH);

		if (expectingMove)
			g2d.setColor(colors[engine.getCurrentPlayer()]);
		else
			g2d.setColor(grey);

		g2d.fill(getVisibleRect());// 0,0,w,(int)(h+hexR)) ;

		g2d.translate(offsetX, offsetY);

		Polygon poly = new Polygon();

		g2d.setColor(red);

		poly.reset();
		poly.addPoint((int) hexC, 0);
		poly.addPoint((int) hexC, (int) hexR);
		poly.addPoint(w, (int) ((hexDimX / 2) * 2 * hexR + ((hexDimX - 1) & 1) * hexR + hexR));
		poly.addPoint(w, (int) ((hexDimX / 2) * 2 * hexR + ((hexDimX - 1) & 1) * hexR));
		g2d.fill(poly);

		poly.reset();
		poly.addPoint(0, (int) ((hexDimX * 2 - 1) * hexR));
		poly.addPoint(0, (int) (hexDimX * 2 * hexR));
		poly.addPoint((int) (w - (hexC)),
				(int) ((hexDimX / 2) * 2 * hexR + ((hexDimX - 1) & 1) * hexR + (hexDimX * 2) * hexR));
		poly.addPoint((int) (w - (hexC)),
				(int) ((hexDimX / 2) * 2 * hexR + ((hexDimX - 1) & 1) * hexR + (hexDimX * 2 - 1) * hexR));
		g2d.fill(poly);

		g2d.setColor(blue);
		g2d.fillRect(0, 0, (int) hexH, (int) (hexR * 21));
		g2d.fillRect((int) (11 * hexC), (int) (11 * hexR), (int) hexH + 1, (int) (hexR * 21 + 1));

		int x[] = new int[6];
		int y[] = new int[6];
		y[0] = 0;
		x[0] = (int) (hexH);

		y[1] = 0;
		x[1] = (int) hexC;

		y[2] = (int) hexR;
		x[2] = (int) hexB;

		y[3] = (int) hexA + 1;
		x[3] = x[1];

		y[4] = y[3];
		x[4] = x[0];

		y[5] = y[2];
		x[5] = 0;

		for (int i = 0; i < hexDimX; i++) {
			for (int k = 0; k < hexDimX; k++) {
				int pixelY = (int) ((i + k / 2) * 2 * hexR + (k & 1) * hexR);
				int pixelX = (int) (k * hexC);
				poly.reset();
				for (int m = 0; m < 6; m++)
					poly.addPoint(pixelX + x[m], pixelY + y[m]);

				g2d.setColor(colors[engine.getBoard()[k][i]]);
				g2d.fill(poly);
				if (engine.getBoard()[k][i] == HexEngine.EMPTY) {
					g2d.setColor(Color.black);
					g2d.draw(poly);
				}
			}

		}
		g2d.translate(-offsetX, -offsetY);
	}

	public void computeSizes() {
		Rectangle bounds = getVisibleRect();

		int n = engine.getBoardSize();

		hexDimX = n;
		hexDimY = n + n / 2;

		// int h = (int)(hexDimY*2*cos(toRadians(30)) * hexSide +
		// (n&1)*cos(toRadians(30)) * hexSide);
		// int w = (int)(hexDimX*(hexH+hexSide)+hexH);

		// hexSide =20;

		hexSideV = bounds.height / ((hexDimY * 2) * Math.cos(Math.toRadians(30)));
		// = Math.min(hexSide, bounds.height / ((hexDimY * 2) *
		// Math.cos(Math.toRadians(30))));
		hexSideH = bounds.width / (hexDimX * (Math.sin(Math.toRadians(30)) + 1) + Math.sin(Math.toRadians(30)));

		// Math.min(hexSide, bounds.width / (hexDimX * (
		// Math.sin(Math.toRadians(30)) + 1) + Math.sin(Math.toRadians(30))));

		hexH = Math.sin(Math.toRadians(30)) * hexSideH;
		hexR = Math.cos(Math.toRadians(30)) * hexSideV;
		hexB = hexSideH + 2 * hexH;
		hexA = 2 * hexR;
		hexC = hexSideH + hexH;

		int h = (int) (hexDimY * hexA);
		int w = (int) (hexDimX * hexC + hexH);
		offsetY = (bounds.height - h) / 2;
		offsetX = (bounds.width - w) / 2;

	}

	@Override
	public void mousePressed(java.awt.event.MouseEvent e) {
		if (!expectingMove)
			return;

		int x = e.getY() - offsetY;
		int y = e.getX() - offsetX;

		int sectX = (int) (x / (2 * hexR));
		int sectY = (int) (y / hexC);

		int sectPxlX = (int) (x - sectX * 2 * hexR); // x % (int)((2 * hexR));
		int sectPxlY = (int) (y - sectY * hexC); // y % (int)((hexH+hexSide));

		int arrayY = sectY;
		int arrayX = sectX;
		double m = hexH / hexR;

		if ((sectY & 1) == 0) { // then SectTyp := A else SectTyp := B;
			arrayY = sectY;
			arrayX = sectX;

			if (sectPxlY < (hexH - sectPxlX * m)) {
				arrayY = sectY - 1;
				arrayX = sectX - 1;
			}
			if (sectPxlY < (-hexH + sectPxlX * m)) {
				arrayY = sectY - 1;
				arrayX = sectX;
			}
		} else { // type B
			if (sectPxlX >= hexR) {
				if (sectPxlY < (2 * hexH - sectPxlX * m)) {
					arrayY = sectY - 1;
					arrayX = sectX;
				} else {
					arrayY = sectY;
					arrayX = sectX;
				}
			} else {
				if (sectPxlY < (sectPxlX * m)) {
					arrayY = sectY - 1;
					arrayX = sectX;
				} else {
					arrayY = sectY;
					arrayX = sectX - 1;
				}
			}
		}
		arrayX -= arrayY / 2;

		System.err.println("Clicked on: " + arrayY + "," + arrayX);

		if ((arrayX >= 0) && (arrayX < hexDimX) && (arrayY >= 0) && (arrayY < hexDimX)
				&& (engine.getBoard()[arrayY][arrayX] == HexEngine.EMPTY)) {
			doMove(new Coord(arrayY, arrayX));
		} else {
			System.err.println("Invalid move");
		}
	}

	public void doMove(Coord c) {
		if (!(expectingMove || c.equals(HexEngine.resign_p1) || c.equals(HexEngine.resign_p2)))
			return;

		expectingMove = false;
		swapButton.setEnabled(false);

		engine.doMove(c);

		// if(engine.doMove(x,y)){
		// System.out.println("Player " + engine.currentPlayer + " won!");
		// }

		repaint();
	}

	@Override
	public void stopGame(int n) {
		expectingMove = false;
		repaint();
	}

	@Override
	public void yourTurn(Coord c) {
		expectingMove = true;
		repaint();
		if (engine.getNumMoves() == 1) {
			swapButton.setEnabled(true);

			// if(JOptionPane.showConfirmDialog(null, "Do you want to swap?",
			// "Swap",
			// JOptionPane.YES_NO_OPTION) ==
			// JOptionPane.YES_OPTION){

			// doMove(HexEngine.swap);
			// }

		} else
			swapButton.setEnabled(false);

	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent e) {
	}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent e) {
	}

	@Override
	public void mouseEntered(java.awt.event.MouseEvent e) {
	}

	@Override
	public void mouseExited(java.awt.event.MouseEvent e) {
	}

}
