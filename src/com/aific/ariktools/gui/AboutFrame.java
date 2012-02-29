package com.aific.ariktools.gui;

/*
 * ArikTools
 * Copyright (C) Arik Z.Lakritz, Peter Macko, and David K. Wittenberg
 * 
 * This file is part of ArikTools.
 *
 * ArikTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArikTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ArikTools.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.aific.ariktools.util.Utils;

public class AboutFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 4998050869869126445L;

	protected static final int BORDER_SIZE = 20;
	
	private EventHandler handler;
	
	private JPanel panel;
	
	private JLabel titleLabel;
	private JLabel mainLabel;
	private JButton closeButton;
	
	// Constructor
	public AboutFrame() {
		super("About Arik\'s Stegonagraphy Tools");
		
		handler = new EventHandler();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		getContentPane().add(panel);
		
		titleLabel = new JLabel("Arik\'s Stegonagraphy Tools");
		titleLabel.setFont(new Font("Arial Black", Font.ITALIC, 16));
		titleLabel.setAlignmentX(0.5f);
		panel.add(titleLabel);
		
		panel.add(new JLabel("   "));
		
		mainLabel = new JLabel("Copyright (c) Arik Z. Lakritz, Peter Macko");
		mainLabel.setAlignmentX(0.5f);
		panel.add(mainLabel);
		
		panel.add(new JLabel("   "));
		
		closeButton = new JButton("Close");
		closeButton.setAlignmentX(0.5f);
		closeButton.addActionListener(handler);
		panel.add(closeButton);
		
		MenuFactory.createMenu(this);
		
		pack();
		
		Utils.centerFrame(this);
	}
	
	/**
		* Runs the application
	 */
	public void run() {
		setVisible(true);
	}
	
	private class EventHandler implements ActionListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == closeButton) {
				AboutFrame.this.setVisible(false);
			}
		}
	}
}
