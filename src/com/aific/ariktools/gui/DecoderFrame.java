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
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import com.aific.ariktools.stego.Decoder;
import com.aific.ariktools.util.Utils;

public class DecoderFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 3060005427459621892L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane generalPane;
	
	private ImageIcon coverImage;
	
	private JPanel coverPanel;
	private JLabel coverLabel;
	private JLabel coverImageLabel;
	private JScrollPane coverScroller;
	private JButton loadCoverButton;
	
	private BufferedImage recoveredImage;
	private ImageIcon recoveredImageIcon;
	
	private JPanel recoveredPanel;
	private JLabel recoveredLabel;
	private JLabel recoveredImageLabel;
	private JScrollPane recoveredScroller;
	
	private JButton recoverButton;
	private JButton saveButton;

	private JPanel bottomPanel;
	private JPanel buttonPanel;
	
	
	// Constructor
	public DecoderFrame() {
		super("Decode / Analyze Low Order Bits");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(400,600));
		getContentPane().add(panel);
		
		
		coverPanel = new JPanel();
		coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.Y_AXIS));
		
		coverLabel = new JLabel("Cover Image");
		coverPanel.add(coverLabel);
		
		coverImageLabel = new JLabel();
		coverImageLabel.setText("(not loaded)");
		
		coverScroller = new JScrollPane(coverImageLabel);
        coverScroller.setPreferredSize(new Dimension(200,200));
		coverPanel.add(coverScroller);
		
		loadCoverButton = new JButton("Load Cover Image");
		loadCoverButton.addActionListener(handler);
		coverPanel.add(loadCoverButton);
		
		
		recoveredPanel = new JPanel();
		recoveredPanel.setLayout(new BoxLayout(recoveredPanel, BoxLayout.Y_AXIS));
		recoveredPanel.setAlignmentX(0.5f);
		
		recoveredLabel = new JLabel("Hidden Image");
		recoveredPanel.add(recoveredLabel);
		
		recoveredImageLabel = new JLabel();
		recoveredImageLabel.setText("(not loaded)");
		
		recoveredScroller = new JScrollPane(recoveredImageLabel);
        recoveredScroller.setPreferredSize(new Dimension(200,200));
		recoveredPanel.add(recoveredScroller);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setAlignmentX(0.5f);
		
		recoverButton = new JButton("Decode");
		recoverButton.addActionListener(handler);
		recoverButton.setEnabled(false);
		buttonPanel.add(recoverButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		saveButton = new JButton("Save Decoded Image");
		saveButton.addActionListener(handler);
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);

		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(recoveredPanel);
		bottomPanel.add(buttonPanel);
		
		
        generalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, coverPanel, bottomPanel);
        generalPane.setOneTouchExpandable(true);
        generalPane.setDividerLocation(250);
		
		panel.add(generalPane);
		
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
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(DecoderFrame.this, "Choose the cover image", true);
				if (f == null) return;
				try {
					coverImage = new ImageIcon(f.getAbsolutePath());
					if (coverImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					coverImageLabel.setIcon(coverImage);
					coverImageLabel.setText("");
				}
				catch (Exception e) {
					coverImageLabel.setIcon(null);
					coverImageLabel.setText("(not loaded)");
					coverImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the cover image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				recoverButton.setEnabled(coverImage != null);
				return;
			}
			
			if (event.getSource() == recoverButton) {
				try {
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					recoveredImage = Decoder.decode(coverImage);
					recoveredImageIcon = new ImageIcon(recoveredImage);
					recoveredImageLabel.setIcon(recoveredImageIcon);
					recoveredImageLabel.setText("");
					saveButton.setEnabled(true);
				}
				catch (Exception e) {
					recoveredImageLabel.setIcon(null);
					recoveredImageLabel.setText("");
					recoveredImageIcon = null;
					saveButton.setEnabled(false);
					JOptionPane.showMessageDialog(null, "Cannot decode the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseImage(DecoderFrame.this, "Save the decoded image", false);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(DecoderFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), recoveredImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the decoded image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
	}
}
