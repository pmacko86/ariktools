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
import javax.swing.event.*;

import com.aific.ariktools.util.Utils;

public class DiffFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = -7064287881129041215L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane sourcePane;
	private JSplitPane resultPane;
	private JSplitPane generalPane;
	
	private ImageIcon coverImage;
	private ImageIcon secondImage;
	
	private JPanel coverPanel;
	private JLabel coverLabel;
	private JLabel coverImageLabel;
	private JScrollPane coverScroller;
	private JButton loadCoverButton;
	
	private JPanel secondPanel;
	private JLabel secondLabel;
	private JLabel secondImageLabel;
	private JScrollPane secondScroller;
	private JButton loadSecondButton;
	
	private BufferedImage finalImage;
	
	private JPanel finalPanel;
	private JLabel finalLabel;
	private JLabel finalImageLabel;
	private JScrollPane finalScroller;
	private JButton finalButton;
	
	private JLabel progressLabel;
	private JProgressBar progress;
	
	private BufferedImage analysisImage;
	private ImageIcon analysisImageIcon;
	
	private JPanel diffSetupPanel;
	private JLabel diffSetupLabel;
	private JTextField diffSetupField;
	private JButton diffSetupButton;
	private double diffScale;
	
	private JPanel analysisPanel;
	private JLabel analysisLabel;
	private JLabel analysisImageLabel;
	private JScrollPane analysisScroller;

	private JButton saveButton;
	private JButton saveDiffButton;

	private JPanel buttonPanel;
	private JPanel resultPanel;

	
	// Constructor
	public DiffFrame() {
		super("Image Comparison");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(800,600));
		getContentPane().add(panel);
		
		
		coverPanel = new JPanel();
		coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.Y_AXIS));
		
		coverLabel = new JLabel("Image");
		coverPanel.add(coverLabel);
		
		coverImageLabel = new JLabel();
		coverImageLabel.setText("(not loaded)");
		
		coverScroller = new JScrollPane(coverImageLabel);
        coverScroller.setPreferredSize(new Dimension(200,200));
		coverPanel.add(coverScroller);
		
		loadCoverButton = new JButton("Load Image");
		loadCoverButton.addActionListener(handler);
		coverPanel.add(loadCoverButton);
		
		
		secondPanel = new JPanel();
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.Y_AXIS));
		
		secondLabel = new JLabel("Image");
		secondPanel.add(secondLabel);
		
		secondImageLabel = new JLabel();
		secondImageLabel.setText("(not loaded)");
		
		secondScroller = new JScrollPane(secondImageLabel);
		secondScroller.setPreferredSize(new Dimension(200,200));
        secondPanel.add(secondScroller);
		
		loadSecondButton = new JButton("Load Image");
		loadSecondButton.addActionListener(handler);
		secondPanel.add(loadSecondButton);
		
		
		finalPanel = new JPanel();
		finalPanel.setLayout(new BoxLayout(finalPanel, BoxLayout.Y_AXIS));
		
		finalLabel = new JLabel("Encoded Image");
		finalPanel.add(finalLabel);
		
		finalImageLabel = new JLabel();
		finalImageLabel.setText("(not generated)");
		
		finalScroller = new JScrollPane(finalImageLabel);
        finalScroller.setPreferredSize(new Dimension(200,200));
		finalPanel.add(finalScroller);

		
		diffSetupPanel = new JPanel();
		diffSetupPanel.setLayout(new BoxLayout(diffSetupPanel, BoxLayout.X_AXIS));
		
		diffSetupLabel = new JLabel("Contrast:   ");
		diffSetupLabel.setAlignmentX(0.5f);
		diffSetupPanel.add(diffSetupLabel);
		
		diffSetupField = new JTextField("60");
		diffSetupField.setAlignmentX(0.5f);
        diffSetupField.getDocument().addDocumentListener(handler);
		diffSetupField.setMaximumSize(new Dimension(200, 22));
		diffSetupPanel.add(diffSetupField);
		diffScale = 60;
		
		diffSetupPanel.add(new JLabel("   "));		
		
		diffSetupButton = new JButton("Update");
		diffSetupButton.addActionListener(handler);
		diffSetupButton.setEnabled(false);
		diffSetupPanel.add(diffSetupButton);

		
		analysisPanel = new JPanel();
		analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
		
		analysisLabel = new JLabel("Analysis");
		analysisPanel.add(analysisLabel);
		
		analysisImageLabel = new JLabel();
		analysisImageLabel.setText("(not generated)");
		
		analysisScroller = new JScrollPane(analysisImageLabel);
        analysisScroller.setPreferredSize(new Dimension(200,200));
		analysisPanel.add(analysisScroller);
		analysisPanel.add(diffSetupPanel);
		
		
        sourcePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coverPanel, secondPanel);
        sourcePane.setOneTouchExpandable(true);
		sourcePane.addPropertyChangeListener(handler);
        sourcePane.setDividerLocation(400);
		
		
        resultPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, finalPanel, analysisPanel);
        resultPane.setOneTouchExpandable(true);
		resultPane.addPropertyChangeListener(handler);
		resultPane.setAlignmentX(0.5f);
        resultPane.setDividerLocation(400);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		finalButton = new JButton("Compare the Images");
		finalButton.addActionListener(handler);
		finalButton.setEnabled(false);
		buttonPanel.add(finalButton);

		buttonPanel.add(new JLabel("   "));		
		
		saveButton = new JButton("Save the Generated Image");
		saveButton.addActionListener(handler);
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		saveDiffButton = new JButton("Save the Difference Image");
		saveDiffButton.addActionListener(handler);
		saveDiffButton.setEnabled(false);
		buttonPanel.add(saveDiffButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		progressLabel = new JLabel("(idle)");
		buttonPanel.add(progressLabel);
		
		buttonPanel.add(new JLabel("   "));		
		
		progress = new JProgressBar();
		buttonPanel.add(progress);
		
		buttonPanel.add(new JLabel("   "));		
		
		
		resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		resultPanel.add(resultPane);
		resultPanel.add(buttonPanel);
		
			
        generalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePane, resultPanel);
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
	
	protected void clearResults() {
		finalImageLabel.setText("(not generated)");
		finalImageLabel.setIcon(null);
		analysisImageLabel.setText("(not generated)");
		analysisImageLabel.setIcon(null);
		analysisImageIcon = null;
		saveButton.setEnabled(false);
		saveDiffButton.setEnabled(false);
	}
	
	private class EventHandler implements ActionListener, PropertyChangeListener, DocumentListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(DiffFrame.this, "Choose the image", true);
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
					JOptionPane.showMessageDialog(null, "Cannot load the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				if (coverImage != null) {
					finalButton.setEnabled(secondImage != null);
				}
				else {
					finalButton.setEnabled(false);
				}
				clearResults();
				return;
			}
			
			if (event.getSource() == loadSecondButton) {
				File f = Utils.chooseImage(DiffFrame.this, "Choose the image", true);
				if (f == null) return;
				try {
					secondImage = new ImageIcon(f.getAbsolutePath());
					if (secondImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					secondImageLabel.setIcon(secondImage);
					secondImageLabel.setText("");
				}
				catch (Exception e) {
					secondImageLabel.setIcon(null);
					secondImageLabel.setText("(not loaded)");
					secondImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				if (secondImage != null) {
					finalButton.setEnabled(coverImage != null);
				}
				else {
					finalButton.setEnabled(false);
				}
				clearResults();
				return;
			}
			
			if (event.getSource() == finalButton) {
				try {
					if (coverImage == null || secondImage == null) throw new Exception("The images are not loaded");

					Thread t = new Thread(new DiffThread());
					t.setDaemon(false);
					t.start();
				}
				catch (Exception e) {
					clearResults();
					JOptionPane.showMessageDialog(null, "Cannot compare the images: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseImage(DiffFrame.this, "Save the generated image", false, true);
				f = Utils.checkLosslessImageExt(DiffFrame.this, f);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(DiffFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), finalImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the generated image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveDiffButton) {
				File f = Utils.chooseImage(DiffFrame.this, "Save the difference image", false, true);
				f = Utils.checkLosslessImageExt(DiffFrame.this, f);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(DiffFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), analysisImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the difference image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == diffSetupButton) {
				try {
					double d = Double.parseDouble(diffSetupField.getText());
					if ((d <= 1.0 / 256.0) || (d >= 256.0)) throw new RuntimeException("The value is out of range");
					diffSetupButton.setEnabled(false);
					diffScale = d;
					
					if ((secondImage != null) && (coverImage != null)) {
						try {
							progressLabel.setText("Preparing statistics");
							progress.setIndeterminate(true);
							progress.setValue(0);
							
							analysisImage = Utils.absoluteDifferenceImage(coverImage, secondImage, diffScale);
							analysisImageIcon = new ImageIcon(analysisImage);
							analysisImageLabel.setIcon(analysisImageIcon);
							analysisImageLabel.setText("");
							
						}
						catch (Throwable e) {
							clearResults();
							JOptionPane.showMessageDialog(null, "Cannot update the difference image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					
						saveDiffButton.setEnabled(analysisImage != null);
				
						progressLabel.setText("(idle)");
						progress.setIndeterminate(false);
						progress.setValue(0);
					}
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
		
		public void propertyChange(PropertyChangeEvent event) {
			
			if (event.getSource() == sourcePane) {
				
				if (event.getPropertyName() == null) return;
				
				if (event.getPropertyName().equals("dividerLocation")) {
					if (resultPane != null) {
						int v = ((Integer)event.getNewValue()).intValue();
						if (resultPane.getDividerLocation() != v) resultPane.setDividerLocation(v);
					}
				}
			}
			
			if (event.getSource() == resultPane) {
				
				if (event.getPropertyName() == null) return;
				
				if (event.getPropertyName().equals("dividerLocation")) {
					if (sourcePane != null) {
						int v = ((Integer)event.getNewValue()).intValue();
						if (sourcePane.getDividerLocation() != v) sourcePane.setDividerLocation(v);
					}
				}
			}
		}
		
		
		public void changedUpdate(DocumentEvent e)
		{
			if (e.getDocument() == diffSetupField.getDocument()) diffSetupButton.setEnabled(true);
		}
		
		public void removeUpdate(DocumentEvent e)
		{
			if (e.getDocument() == diffSetupField.getDocument()) diffSetupButton.setEnabled(true);
		}
		
		public void insertUpdate(DocumentEvent e)
		{
			if (e.getDocument() == diffSetupField.getDocument()) diffSetupButton.setEnabled(true);
		}
	}
	
	
	private class DiffThread implements Runnable {
		
		public DiffThread() {
		}
		
		public void run() {
			finalButton.setEnabled(false);
			loadCoverButton.setEnabled(false);
			loadSecondButton.setEnabled(false);
			saveButton.setEnabled(false);
			saveDiffButton.setEnabled(false);
			
			try {
				
				progressLabel.setText("Preparing statistics");
				progress.setIndeterminate(true);
				progress.setValue(0);
				
				analysisImage = Utils.absoluteDifferenceImage(coverImage, secondImage, diffScale);
				analysisImageIcon = new ImageIcon(analysisImage);
				analysisImageLabel.setIcon(analysisImageIcon);
				analysisImageLabel.setText("");
				
			}
			catch (Throwable e) {
				clearResults();
				JOptionPane.showMessageDialog(null, "Cannot compare the images: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			finalButton.setEnabled(true);
			loadCoverButton.setEnabled(true);
			loadSecondButton.setEnabled(true);
			saveButton.setEnabled(finalImage != null);
			saveDiffButton.setEnabled(analysisImage != null);
			
			progressLabel.setText("(idle)");
			progress.setIndeterminate(false);
			progress.setValue(0);
		}
	}
}
