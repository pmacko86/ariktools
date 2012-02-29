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
import java.io.*;
import javax.swing.*;

import com.aific.ariktools.stego.AdvDecoder;
import com.aific.ariktools.util.Utils;

public class AdvDecoderFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 6375093780625323692L;

	private EventHandler handler;
	
	private JPanel panel;
	
	private ImageIcon coverImage;
	private long coverCapacity;
	
	private JPanel coverPanel;
	private JLabel coverLabel;
	private JLabel coverImageLabel;
	private JScrollPane coverScroller;
	private JButton loadCoverButton;
	
	private JButton decodeButton;
	
	private JLabel progressLabel;
	private JProgressBar progress;
	
	private JPanel sourcePanel;
	private JPanel buttonPanel;
	
	
	// Constructor
	public AdvDecoderFrame() {
		super("Decode a File");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(400,300));
		getContentPane().add(panel);
		
		
		coverPanel = new JPanel();
		coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.Y_AXIS));
		coverPanel.setAlignmentX(0.5f);
		
		coverLabel = new JLabel("Cover Image");
		coverPanel.add(coverLabel);
		
		coverImageLabel = new JLabel();
		coverImageLabel.setText("(not loaded)");
		
		coverScroller = new JScrollPane(coverImageLabel);
        coverScroller.setPreferredSize(new Dimension(200,200));
		coverPanel.add(coverScroller);
		
		loadCoverButton = new JButton("Load Cover Image");
		loadCoverButton.addActionListener(handler);

		
		decodeButton = new JButton("Decode the File");
		decodeButton.addActionListener(handler);
		decodeButton.setEnabled(false);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(loadCoverButton);
		buttonPanel.add(new JLabel("   "));		
		buttonPanel.add(decodeButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		progressLabel = new JLabel("(idle)");
		buttonPanel.add(progressLabel);
		
		buttonPanel.add(new JLabel("   "));		
		
		progress = new JProgressBar();
		buttonPanel.add(progress);
		
		buttonPanel.add(new JLabel("   "));		
		
		
		sourcePanel = new JPanel();
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));
		sourcePanel.add(coverPanel);
		sourcePanel.add(buttonPanel);
		
		
		panel.add(sourcePanel);
		
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
				File f = Utils.chooseImage(AdvDecoderFrame.this, "Choose the cover image", true);
				if (f == null) return;
				try {
					coverImage = new ImageIcon(f.getAbsolutePath());
					if (coverImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					coverCapacity = 3 * coverImage.getIconWidth() * coverImage.getIconHeight() / 4 - 3;
					if (coverCapacity >= 16 * 1048576) coverCapacity = 16 * 1048576 - 1;
					coverImageLabel.setIcon(coverImage);
					coverImageLabel.setText("");
				}
				catch (Exception e) {
					coverImageLabel.setIcon(null);
					coverImageLabel.setText("(not loaded)");
					coverImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the cover image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				decodeButton.setEnabled(coverImage != null);
				return;
			}
			
			if (event.getSource() == decodeButton) {
				try {
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					File f = Utils.chooseFile(AdvDecoderFrame.this, "Save the encoded file", false);
					if (f == null) return;
					if (f.exists()) {
						if (!Utils.shouldOverwrite(AdvDecoderFrame.this, f.getName())) return;
					}
					Thread t = new Thread(new DecodeThread(coverImage, f));
					t.setDaemon(false);
					t.start();
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot decode the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
	}
	
	
	private class DecodeThread implements Runnable {
		
		private ImageIcon sourceImage;
		private File outputFile;
		
		public DecodeThread(ImageIcon sourceImage, File outputFile) {
			this.sourceImage = sourceImage;
			this.outputFile = outputFile;
		}
		
		public void run() {
			decodeButton.setEnabled(false);
			loadCoverButton.setEnabled(false);
			
			try {
				AdvDecoder decoder = new AdvDecoder(sourceImage, outputFile);
				decoder.setProgressBar(progress);
				decoder.setProgressLabel(progressLabel);
				
				decoder.decode();
			}
			catch (Throwable e) {
				JOptionPane.showMessageDialog(null, "Cannot decode the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				if (e.getMessage() == null) e.printStackTrace();
				else if ((!e.getMessage().startsWith("No secret data")) && (!e.getMessage().startsWith("Could not decode"))) e.printStackTrace();
			}
			
			decodeButton.setEnabled(true);
			loadCoverButton.setEnabled(true);
			
			progressLabel.setText("(idle)");
			progress.setIndeterminate(false);
			progress.setValue(0);
		}
	}
}
