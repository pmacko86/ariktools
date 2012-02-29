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
import java.beans.*;
import java.io.*;
import javax.swing.*;

import com.aific.ariktools.stego.Decoder;
import com.aific.ariktools.stego.Encoder;
import com.aific.ariktools.util.Utils;

public class EncoderFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = -6474449352892160282L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane sourcePane;
	private JSplitPane encodePane;
	private JSplitPane generalPane;
	
	private ImageIcon coverImage;
	
	private JPanel coverPanel;
	private JLabel coverLabel;
	private JLabel coverImageLabel;
	private JScrollPane coverScroller;
	private JButton loadCoverButton;
	
	private ImageIcon hideImage;
	
	private JPanel hidePanel;
	private JLabel hideLabel;
	private JLabel hideImageLabel;
	private JScrollPane hideScroller;
	private JButton loadHideButton;
	
	private BufferedImage finalImage;
	private ImageIcon finalImageIcon;
	
	private JPanel finalPanel;
	private JLabel finalLabel;
	private JLabel finalImageLabel;
	private JScrollPane finalScroller;
	private JButton finalButton;
	
	private BufferedImage recoveredImage;
	private ImageIcon recoveredImageIcon;
	
	private JPanel recoveredPanel;
	private JLabel recoveredLabel;
	private JLabel recoveredImageLabel;
	private JScrollPane recoveredScroller;

	private JButton saveButton;

	private JPanel buttonPanel;
	private JPanel encodePanel;

	
	// Constructor
	public EncoderFrame() {
		super("Encode Image");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(800,600));
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
		
		
		hidePanel = new JPanel();
		hidePanel.setLayout(new BoxLayout(hidePanel, BoxLayout.Y_AXIS));
		
		hideLabel = new JLabel("Image to Hide");
		hidePanel.add(hideLabel);
		
		hideImageLabel = new JLabel();
		hideImageLabel.setText("(not loaded)");
		
		hideScroller = new JScrollPane(hideImageLabel);
        hideScroller.setPreferredSize(new Dimension(200,200));
		hidePanel.add(hideScroller);
		
		loadHideButton = new JButton("Load Hidden Image");
		loadHideButton.addActionListener(handler);
		hidePanel.add(loadHideButton);
		
		
		finalPanel = new JPanel();
		finalPanel.setLayout(new BoxLayout(finalPanel, BoxLayout.Y_AXIS));
		
		finalLabel = new JLabel("Encoded Image");
		finalPanel.add(finalLabel);
		
		finalImageLabel = new JLabel();
		finalImageLabel.setText("(not generated)");
		
		finalScroller = new JScrollPane(finalImageLabel);
        finalScroller.setPreferredSize(new Dimension(200,200));
		finalPanel.add(finalScroller);
		
		
		recoveredPanel = new JPanel();
		recoveredPanel.setLayout(new BoxLayout(recoveredPanel, BoxLayout.Y_AXIS));
		
		recoveredLabel = new JLabel("Hidden Image");
		recoveredPanel.add(recoveredLabel);
		
		recoveredImageLabel = new JLabel();
		recoveredImageLabel.setText("(not generated)");
		
		recoveredScroller = new JScrollPane(recoveredImageLabel);
        recoveredScroller.setPreferredSize(new Dimension(200,200));
		recoveredPanel.add(recoveredScroller);
		
		
        sourcePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coverPanel, hidePanel);
        sourcePane.setOneTouchExpandable(true);
		sourcePane.addPropertyChangeListener(handler);
        sourcePane.setDividerLocation(400);
		
		
        encodePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, finalPanel, recoveredPanel);
        encodePane.setOneTouchExpandable(true);
		encodePane.addPropertyChangeListener(handler);
		encodePane.setAlignmentX(0.5f);
        encodePane.setDividerLocation(400);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		finalButton = new JButton("Generate Encoded Image");
		finalButton.addActionListener(handler);
		finalButton.setEnabled(false);
		buttonPanel.add(finalButton);

		buttonPanel.add(new JLabel("   "));		
		
		saveButton = new JButton("Save Encoded Image");
		saveButton.addActionListener(handler);
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		
		
		encodePanel = new JPanel();
		encodePanel.setLayout(new BoxLayout(encodePanel, BoxLayout.Y_AXIS));
		encodePanel.add(encodePane);
		encodePanel.add(buttonPanel);
		
			
        generalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePane, encodePanel);
        generalPane.setOneTouchExpandable(true);
        generalPane.setDividerLocation(300);
		
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
	
	private class EventHandler implements ActionListener, PropertyChangeListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(EncoderFrame.this, "Choose the cover image", true);
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
				if (hideImage != null && coverImage != null) {
					finalButton.setEnabled((hideImage.getIconWidth() <= coverImage.getIconWidth()) && (hideImage.getIconHeight() <= coverImage.getIconHeight()));
				}
				else {
					finalButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == loadHideButton) {
				File f = Utils.chooseImage(EncoderFrame.this, "Choose the image to hide", true);
				if (f == null) return;
				try {
					hideImage = new ImageIcon(f.getAbsolutePath());
					if (hideImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					hideImageLabel.setIcon(hideImage);
					hideImageLabel.setText("");
				}
				catch (Exception e) {
					hideImageLabel.setIcon(null);
					hideImageLabel.setText("(not loaded)");
					hideImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the secret image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				if (hideImage != null && coverImage != null) {
					finalButton.setEnabled((hideImage.getIconWidth() <= coverImage.getIconWidth()) && (hideImage.getIconHeight() <= coverImage.getIconHeight()));
				}
				else {
					finalButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == finalButton) {
				try {
					if (hideImage == null) throw new Exception("Secret image is not loaded");
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					if ((hideImage.getIconWidth() > coverImage.getIconWidth()) || (hideImage.getIconHeight() > coverImage.getIconHeight()))
						throw new Exception("The dimensions of the cover image do not match the size of the secret image.");
					finalImage = Encoder.encode(coverImage, hideImage);
					finalImageIcon = new ImageIcon(finalImage);
					finalImageLabel.setIcon(finalImageIcon);
					finalImageLabel.setText("");
					recoveredImage = Decoder.decode(finalImageIcon);
					recoveredImageIcon = new ImageIcon(recoveredImage);
					recoveredImageLabel.setIcon(recoveredImageIcon);
					recoveredImageLabel.setText("");
					saveButton.setEnabled(true);
				}
				catch (Exception e) {
					finalImageLabel.setText("(not generated)");
					finalImageLabel.setIcon(null);
					recoveredImageLabel.setText("(not generated)");
					recoveredImageLabel.setIcon(null);
					finalImageIcon = null;
					recoveredImageIcon = null;
					saveButton.setEnabled(false);
					JOptionPane.showMessageDialog(null, "Cannot encode the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseImage(EncoderFrame.this, "Save the encoded image", false, true);
				f = Utils.checkLosslessImageExt(EncoderFrame.this, f);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(EncoderFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), finalImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the encoded image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
		
		public void propertyChange(PropertyChangeEvent event) {
			
			if (event.getSource() == sourcePane) {
				
				if (event.getPropertyName() == null) return;
				
				if (event.getPropertyName().equals("dividerLocation")) {
					if (encodePane != null) {
						int v = ((Integer)event.getNewValue()).intValue();
						if (encodePane.getDividerLocation() != v) encodePane.setDividerLocation(v);
					}
				}
			}
			
			if (event.getSource() == encodePane) {
				
				if (event.getPropertyName() == null) return;
				
				if (event.getPropertyName().equals("dividerLocation")) {
					if (sourcePane != null) {
						int v = ((Integer)event.getNewValue()).intValue();
						if (sourcePane.getDividerLocation() != v) sourcePane.setDividerLocation(v);
					}
				}
			}
		}
		
	}
}
