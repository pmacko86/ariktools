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

import com.aific.ariktools.stego.Encoder;
import com.aific.ariktools.util.Utils;

public class FileEncoderFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 387793240660682474L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane generalPane;
	
	private ImageIcon coverImage;
	private long coverCapacity;
	
	private JPanel coverPanel;
	private JLabel coverLabel;
	private JLabel coverImageLabel;
	private JScrollPane coverScroller;
	private JButton loadCoverButton;
	
	private File hideFile;
	private long hideSize;
	
	private JPanel filePanel;
	private JLabel fileLabel;
	private JLabel fileCapacityLabel;
	private JButton loadFileButton;

	private JPanel sourcePanel;
	private JPanel sourceButtonPanel;
	
	private BufferedImage encodedImage;
	private ImageIcon encodedImageIcon;
	
	private JPanel encodedPanel;
	private JLabel encodedLabel;
	private JLabel encodedImageLabel;
	private JScrollPane encodedScroller;
	
	private JButton encodeButton;
	private JButton saveButton;
	
	private JPanel bottomPanel;
	private JPanel buttonPanel;
	
	
	// Constructor
	public FileEncoderFrame() {
		super("Encode a File");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(400,600));
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
		
		
		filePanel = new JPanel();
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
		filePanel.setAlignmentX(0.5f);
		
		fileLabel = new JLabel("Secret File: (not loaded)");
		filePanel.add(fileLabel);
		
		fileCapacityLabel = new JLabel("Capacity: --- / ---");
		filePanel.add(fileCapacityLabel);
		
		loadFileButton = new JButton("Load Secret File");
		loadFileButton.addActionListener(handler);
		
		
		sourceButtonPanel = new JPanel();
		sourceButtonPanel.setLayout(new BoxLayout(sourceButtonPanel, BoxLayout.X_AXIS));
		sourceButtonPanel.add(loadCoverButton);
		sourceButtonPanel.add(new JLabel("   "));		
		sourceButtonPanel.add(loadFileButton);
		
		
		encodedPanel = new JPanel();
		encodedPanel.setLayout(new BoxLayout(encodedPanel, BoxLayout.Y_AXIS));
		encodedPanel.setAlignmentX(0.5f);
		
		encodedLabel = new JLabel("Encoded Image");
		encodedPanel.add(encodedLabel);
		
		encodedImageLabel = new JLabel();
		encodedImageLabel.setText("(not generated)");
		
		encodedScroller = new JScrollPane(encodedImageLabel);
        encodedScroller.setPreferredSize(new Dimension(200,200));
		encodedPanel.add(encodedScroller);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setAlignmentX(0.5f);
		
		encodeButton = new JButton("Encode");
		encodeButton.addActionListener(handler);
		encodeButton.setEnabled(false);
		buttonPanel.add(encodeButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		saveButton = new JButton("Save Encoded Image");
		saveButton.addActionListener(handler);
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(encodedPanel);
		bottomPanel.add(buttonPanel);
		
		
		sourcePanel = new JPanel();
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));
		sourcePanel.add(coverPanel);
		sourcePanel.add(filePanel);
		sourcePanel.add(sourceButtonPanel);
		
		
        generalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePanel, bottomPanel);
        generalPane.setOneTouchExpandable(true);
        generalPane.setDividerLocation(300);
		
		panel.add(generalPane);
		
		MenuFactory.createMenu(this);
		
		
		coverCapacity = -1;
		hideSize = -1;
				
		
		pack();
		
		Utils.centerFrame(this);
	}
	
	/**
		* Runs the application
	 */
	public void run() {
		setVisible(true);
	}
	
	private String getCapacityString() {
		String cover = (coverCapacity < 0) ? "---" : "" + Math.round((10 * coverCapacity / 1024.0) / 10.0) + " KB";
		String hide  = (hideSize      < 0) ? "---" : "" + Math.round((10 *      hideSize / 1024.0) / 10.0) + " KB";
		return hide + " / " + cover;
	}
	
	private class EventHandler implements ActionListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(FileEncoderFrame.this, "Choose the cover image", true);
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
					coverCapacity = -1;
					JOptionPane.showMessageDialog(null, "Cannot load the cover image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				fileCapacityLabel.setText("Capacity: " + getCapacityString());
				if (coverImage != null && hideFile != null) {
					encodeButton.setEnabled(hideSize <= coverCapacity);
				}
				else {
					encodeButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == loadFileButton) {
				File f = Utils.chooseFile(FileEncoderFrame.this, "Choose the file to hide", true);
				if (f == null) return;
				try {
					if (!f.exists()) throw new Exception("The file does not exist");
					hideFile = f;
					hideSize = f.length();
					fileLabel.setText("Secret File: " + f.getName());
				}
				catch (Exception e) {
					fileLabel.setText("Secret File: (not loaded)");
					hideFile = null;
					hideSize = -1;
					JOptionPane.showMessageDialog(null, "Cannot load the secret file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				fileCapacityLabel.setText("Capacity: " + getCapacityString());
				if (coverImage != null && hideFile != null) {
					encodeButton.setEnabled(hideSize <= coverCapacity);
				}
				else {
					encodeButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == encodeButton) {
				try {
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					if (hideFile == null) throw new Exception("Secret file is not loaded");
					if (!hideFile.exists()) throw new Exception("The secret file does not exist");
					if (hideSize > coverCapacity) throw new Exception("The secret file is bigger than the cover capacity");
					encodedImage = Encoder.encode(coverImage, hideFile);
					encodedImageIcon = new ImageIcon(encodedImage);
					encodedImageLabel.setIcon(encodedImageIcon);
					encodedImageLabel.setText("");
					saveButton.setEnabled(true);
				}
				catch (Exception e) {
					encodedImageLabel.setIcon(null);
					encodedImageLabel.setText("");
					encodedImageIcon = null;
					saveButton.setEnabled(false);
					JOptionPane.showMessageDialog(null, "Cannot encode the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseImage(FileEncoderFrame.this, "Save the encoded image", false, true);
				f = Utils.checkLosslessImageExt(FileEncoderFrame.this, f);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(FileEncoderFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), encodedImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the encoded image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
	}
}
