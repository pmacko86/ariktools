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

import com.aific.ariktools.analysis.FridrichGroupAnalysis;
import com.aific.ariktools.stego.Decoder;
import com.aific.ariktools.util.Utils;

public class FridrichGroupAnalysisFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = -8621635506759627281L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane sourcePane;
	private JSplitPane generalPane;
	
	private File coverFile;
	private ImageIcon sourceImage;
	
	private JPanel sourcePanel;
	private JLabel sourceLabel;
	private JLabel sourceImageLabel;
	private JScrollPane sourceScroller;
	private JButton loadCoverButton;
	
	private BufferedImage lowBitsImage;
	private ImageIcon lowBitsImageIcon;
	
	private JPanel lowBitsPanel;
	private JLabel lowBitsLabel;
	private JLabel lowBitsImageLabel;
	private JScrollPane lowBitsScroller;
	private JButton lowBitsButton;
	
	private String report;
	
	private JPanel randomnessPanel;
	private JLabel randomnessLabel;
	private JLabel randomnessImageLabel;
	private JScrollPane randomnessScroller;
	private JButton randomnessButton;

	private JButton saveButton;
	
	private JProgressBar progress;
	private JLabel progressLabel;

	private JPanel buttonPanel;
	private JPanel encodePanel;

	
	// Constructor
	public FridrichGroupAnalysisFrame() {
		super("Fridrich\'s Group Analysis");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(800,600));
		getContentPane().add(panel);
		
		
		sourcePanel = new JPanel();
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));
		
		sourceLabel = new JLabel("Source Image");
		sourcePanel.add(sourceLabel);
		
		sourceImageLabel = new JLabel();
		sourceImageLabel.setText("(not loaded)");
		
		sourceScroller = new JScrollPane(sourceImageLabel);
        sourceScroller.setPreferredSize(new Dimension(200,200));
		sourcePanel.add(sourceScroller);
		
		loadCoverButton = new JButton("Load Source Image");
		loadCoverButton.addActionListener(handler);
		sourcePanel.add(loadCoverButton);
		
		
		lowBitsPanel = new JPanel();
		lowBitsPanel.setLayout(new BoxLayout(lowBitsPanel, BoxLayout.Y_AXIS));
		
		lowBitsLabel = new JLabel("Low Order Bits");
		lowBitsPanel.add(lowBitsLabel);
		
		lowBitsImageLabel = new JLabel();
		lowBitsImageLabel.setText("(not generated)");
		
		lowBitsScroller = new JScrollPane(lowBitsImageLabel);
        lowBitsScroller.setPreferredSize(new Dimension(200,200));
		lowBitsPanel.add(lowBitsScroller);
		
		lowBitsButton = new JButton("Save as Image");
		lowBitsButton.addActionListener(handler);
		lowBitsButton.setEnabled(false);
		lowBitsPanel.add(lowBitsButton);
		
		
		randomnessPanel = new JPanel();
		randomnessPanel.setLayout(new BoxLayout(randomnessPanel, BoxLayout.Y_AXIS));
		
		randomnessLabel = new JLabel("Analysis Report");
		randomnessPanel.add(randomnessLabel);
		
		randomnessImageLabel = new JLabel("", JLabel.CENTER);
		randomnessImageLabel.setText("(not generated)");
		
		randomnessScroller = new JScrollPane(randomnessImageLabel);
        randomnessScroller.setPreferredSize(new Dimension(200,200));
		randomnessPanel.add(randomnessScroller);
		
		
        sourcePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourcePanel, lowBitsPanel);
        sourcePane.setOneTouchExpandable(true);
        sourcePane.setDividerLocation(400);
		
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		randomnessButton = new JButton("Analyze the Groups");
		randomnessButton.addActionListener(handler);
		randomnessButton.setEnabled(false);
		buttonPanel.add(randomnessButton);
		
		buttonPanel.add(new JLabel("   "));		
		
		saveButton = new JButton("Save the Results");
		saveButton.addActionListener(handler);
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		
		buttonPanel.add(new JLabel("   "));		

		progressLabel = new JLabel("(idle)");
		buttonPanel.add(progressLabel);
		
		buttonPanel.add(new JLabel("   "));		
		
		progress = new JProgressBar();
		buttonPanel.add(progress);

		
		encodePanel = new JPanel();
		encodePanel.setLayout(new BoxLayout(encodePanel, BoxLayout.Y_AXIS));
		encodePanel.add(randomnessPanel);
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
	
	protected void clearResults() {
		lowBitsImageLabel.setIcon(null);
		lowBitsImageLabel.setText("(not generated)");
		lowBitsImageIcon = null;
		randomnessImageLabel.setText("(not generated)");
		randomnessImageLabel.setIcon(null);
		saveButton.setEnabled(false);
		lowBitsButton.setEnabled(false);
		report = null;
	}
	
	private class EventHandler implements ActionListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(FridrichGroupAnalysisFrame.this, "Choose the source image", true);
				if (f == null) return;
				try {
					sourceImage = new ImageIcon(f.getAbsolutePath());
					if (sourceImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					sourceImageLabel.setIcon(sourceImage);
					sourceImageLabel.setText("");
					clearResults();
					coverFile = f;
				}
				catch (Exception e) {
					sourceImageLabel.setIcon(null);
					sourceImageLabel.setText("(not loaded)");
					sourceImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the source image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				randomnessButton.setEnabled(sourceImage != null);
				lowBitsButton.setEnabled(lowBitsImage != null);
				return;
			}
			
			if (event.getSource() == lowBitsButton) {
				File f = Utils.chooseImage(FridrichGroupAnalysisFrame.this, "Save the lower bits as an image", false);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(FridrichGroupAnalysisFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), lowBitsImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the low-bits image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == randomnessButton) {
				try {
					if (sourceImage == null) throw new Exception("Source image is not loaded");
					Thread t = new Thread(new AnalysisThread(sourceImage));
					t.setDaemon(false);
					t.start();
				}
				catch (Exception e) {
					clearResults();
					JOptionPane.showMessageDialog(null, "Cannot encode the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseFile(FridrichGroupAnalysisFrame.this, "Save the report", false);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(FridrichGroupAnalysisFrame.this, f.getName())) return;
				}
				try {
					Utils.saveStringFile(f.getAbsolutePath(), report);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
		}
		
	}
	
	
	private class AnalysisThread implements Runnable {
		
		private ImageIcon sourceImage;
		
		public AnalysisThread(ImageIcon sourceImage) {
			this.sourceImage = sourceImage;
		}
		
		public void run() {
			randomnessButton.setEnabled(false);
			lowBitsButton.setEnabled(false);
			loadCoverButton.setEnabled(false);
			saveButton.setEnabled(false);

			try {
				progress.setIndeterminate(true);
				
				lowBitsImage = Decoder.decode(sourceImage);
				lowBitsImageIcon = new ImageIcon(lowBitsImage);
				lowBitsImageLabel.setIcon(lowBitsImageIcon);
				lowBitsImageLabel.setText("");
				
				FridrichGroupAnalysis fga = new FridrichGroupAnalysis(sourceImage);
				fga.setProgressBar(progress);
				fga.setProgressLabel(progressLabel);
				fga.analyze();

				progress.setIndeterminate(true);
				
				report = fga.generateReport(coverFile.getName());
				randomnessImageLabel.setText(report);
			}
			catch (Exception e) {
				clearResults();
				JOptionPane.showMessageDialog(null, "Cannot analyze the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (OutOfMemoryError e) {
				clearResults();
				JOptionPane.showMessageDialog(null, "Cannot analyze the image: Insufficient memory", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (Throwable e) {
				clearResults();
				JOptionPane.showMessageDialog(null, "Cannot analyze the image: "
					+ (e.getMessage() == null ? e.getClass().toString() : e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			
			loadCoverButton.setEnabled(true);
			randomnessButton.setEnabled(sourceImage != null);
			lowBitsButton.setEnabled(lowBitsImage != null);
			saveButton.setEnabled(report != null);
			
			progress.setIndeterminate(false);
			progressLabel.setText("(idle)");
			progress.setValue(0);
		}
	}
}
