/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package org.carsten;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/*
 * This class adds the menus and handles events triggered by those menus
 */
public class MenuFrame extends JFrame implements ActionListener {
	// Graph hex;
	// HexArea area;

	private HexEngine engine;
	private Hex controller;

	// creates the menu items
	public MenuFrame(HexEngine e, Hex c) {
		engine = e;
		controller = c;
		JMenuBar menuBar;
		JMenu menu;
		JMenu subMenu;
		JMenu subsubMenu;
		JMenuItem menuItem;
		ButtonGroup group;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		// Create the menu bar.
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Build the game menu.
		menu = new JMenu("Game");
		// menu.setMnemonic(KeyEvent.VK_G);
		menu.getAccessibleContext().setAccessibleDescription("Game");
		menuBar.add(menu);

		subMenu = new JMenu("New");
		subMenu.getAccessibleContext().setAccessibleDescription("New");
		menu.add(subMenu);

		JMenuItem item = new JMenuItem("Against Computer");
		item.setActionCommand("newComp");
		item.addActionListener(this);
		subMenu.add(item);

		subsubMenu = new JMenu("Against Human");
		subMenu.getAccessibleContext().setAccessibleDescription("Against Human");
		subMenu.add(subsubMenu);

		item = new JMenuItem("7 x 7");
		item.setActionCommand("newHuman 7x7");
		item.addActionListener(this);
		subsubMenu.add(item);

		item = new JMenuItem("9 x 9");
		item.setActionCommand("newHuman 9x9");
		item.addActionListener(this);
		subsubMenu.add(item);

		item = new JMenuItem("11 x 11");
		item.setActionCommand("newHuman 11x11");
		item.addActionListener(this);
		subsubMenu.add(item);

		item = new JMenuItem("Over Network");
		item.setActionCommand("newNetwork");
		item.addActionListener(this);
		subMenu.add(item);

		item = new JMenuItem("Resign");
		item.setActionCommand("resign");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("Exit");
		item.setActionCommand("exit");
		item.addActionListener(this);
		menu.add(item);

		// Options
		menu = new JMenu("Options");
		menu.getAccessibleContext().setAccessibleDescription("Options");
		menuBar.add(menu);

		subMenu = new JMenu("Swap Rule");
		subMenu.getAccessibleContext().setAccessibleDescription("Swap Rule");
		menu.add(subMenu);

		group = new ButtonGroup();
		item = new JRadioButtonMenuItem("On");
		item.setSelected(true);
		item.setActionCommand("sron");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Off");
		item.setActionCommand("sroff");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		subMenu = new JMenu("View");
		subMenu.getAccessibleContext().setAccessibleDescription("View");
		menu.add(subMenu);

		group = new ButtonGroup();
		item = new JRadioButtonMenuItem("Neutral");
		item.setSelected(true);
		item.setActionCommand("pneutral");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Red");
		item.setActionCommand("pred");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Blue");
		item.setActionCommand("pblue");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Player");
		item.setActionCommand("pplayer");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		subMenu.addSeparator();
		group = new ButtonGroup();
		item = new JRadioButtonMenuItem("Nodes On");
		item.setSelected(true);
		item.setActionCommand("non");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Nodes Off");
		item.setActionCommand("noff");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		subMenu = new JMenu("Algorithm");
		subMenu.getAccessibleContext().setAccessibleDescription("Algorithm");
		menu.add(subMenu);

		group = new ButtonGroup();
		item = new JRadioButtonMenuItem("None");
		item.setActionCommand("anone");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Tutte");
		item.setActionCommand("atutte");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Force-Directed PrEd");
		item.setActionCommand("afdpred");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		item = new JRadioButtonMenuItem("Tutte + Force-Directed PrEd");
		item.setSelected(true);
		item.setActionCommand("atfdpred");
		item.addActionListener(this);
		group.add(item);
		subMenu.add(item);

		/*
		 * // Help menu = new JMenu("Help");
		 * menu.getAccessibleContext().setAccessibleDescription("Help");
		 * menuBar.add(menu);
		 * 
		 * item = new JMenuItem("Help"); item.setActionCommand("help");
		 * item.addActionListener(this); menu.add(item);
		 * 
		 * item = new JMenuItem("About"); item.setActionCommand("about");
		 * item.addActionListener(this); menu.add(item);
		 */

	}

	// Handles menu selections
	@Override
	public void actionPerformed(ActionEvent e) {
		// game
		if (e.getActionCommand().equals("newHuman 7x7")) {
			controller.size = 7;
			engine.initGame(HexEngine.HUMAN_HUMAN);
		} else if (e.getActionCommand().equals("newHuman 9x9")) {
			controller.size = 9;
			engine.initGame(HexEngine.HUMAN_HUMAN);
		} else if (e.getActionCommand().equals("newHuman 11x11")) {
			controller.size = 11;
			engine.initGame(HexEngine.HUMAN_HUMAN);
		}

		else if (e.getActionCommand().equals("newComp")) {
			controller.size = 11;
			engine.initGame(HexEngine.HUMAN_COMPUTER);
		}

		else if (e.getActionCommand().equals("newNetwork")) {
			controller.size = 11;
			engine.initGame(HexEngine.NETWORK_HUMAN);
		}

		else if (e.getActionCommand().equals("exit")) {
			controller.exit();
		} else if (e.getActionCommand().equals("resign")) {
			controller.resign();
		}

		// options
		else if (e.getActionCommand().equals("sron")) {
			controller.hexArea.swapRule = true;
		} else if (e.getActionCommand().equals("sroff")) {
			controller.hexArea.swapRule = false;
		}

		else if (e.getActionCommand().equals("pneutral")) {
			controller.hexArea.view = "neutral";
			controller.hexArea.repaint();
		} else if (e.getActionCommand().equals("pred")) {
			controller.hexArea.view = "red";
			controller.hexArea.repaint();
		} else if (e.getActionCommand().equals("pblue")) {
			controller.hexArea.view = "blue";
			controller.hexArea.repaint();
		} else if (e.getActionCommand().equals("pplayer")) {
			controller.hexArea.view = "player";
			controller.hexArea.repaint();
		}

		else if (e.getActionCommand().equals("non")) {
			controller.hexArea.nodesOn = true;
			controller.hexArea.repaint();
		} else if (e.getActionCommand().equals("noff")) {
			controller.hexArea.nodesOn = false;
			controller.hexArea.repaint();
		}

		else if (e.getActionCommand().equals("anone")) {
			controller.setAlgorithm("none");
		} else if (e.getActionCommand().equals("atutte")) {
			controller.setAlgorithm("tutte");
		} else if (e.getActionCommand().equals("afdpred")) {
			controller.setAlgorithm("fdpred");
		} else if (e.getActionCommand().equals("atfdpred")) {
			controller.setAlgorithm("tfdpred");
		}

		/*
		 * // help else if(e.getActionCommand().equals("help")) {
		 * System.err.println("Help"); } else
		 * if(e.getActionCommand().equals("about")) {
		 * System.err.println("About"); }
		 */
	}
}
