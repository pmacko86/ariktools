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

public class MainFrame extends JFrame implements Runnable {

	private static final long serialVersionUID = 1120386322063101203L;

	protected static final int BORDER_SIZE = 20;
	
	private EventHandler handler;

	private JPanel panel;
	
	private JLabel titleLabel;
	
	private JLabel inImageLabel;
	private JButton encodeImageButton;
	private JButton decodeImageButton;
	private JButton encodeFileButton;
	private JButton decodeFileButton;
	
	private JLabel advLabel;
	private JButton advEncodeButton;
	private JButton advDecodeButton;
	
	private JLabel stegoanalysisLabel;
	private JButton analysisColorButton;
	private JButton analysisSamplePairsButton;
	private JButton analysisFridrichGroupButton;

	private JLabel analysisLabel;
	private JButton analysisButton;
	private JButton diffButton;
	private JButton filterButton;
	private JButton waveVisualizerButton;
	
	private JButton configButton;
	private JButton quitButton;
	
	private JLabel copyrightLabel;
	
	// Constructor
	public MainFrame() {
		super("Arik\'s Stegonagraphy Tools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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

		inImageLabel = new JLabel("Simple Hiding in a Cover Image");
		inImageLabel.setAlignmentX(0.5f);
		//panel.add(inImageLabel);
		
		encodeImageButton = new JButton("Encode an Image");
		encodeImageButton.setAlignmentX(0.5f);
		encodeImageButton.addActionListener(handler);
		//panel.add(encodeImageButton);
		
		decodeImageButton = new JButton("Decode an Image");
		decodeImageButton.setAlignmentX(0.5f);
		decodeImageButton.addActionListener(handler);
		//panel.add(decodeImageButton);
		
		encodeFileButton = new JButton("Encode any File");
		encodeFileButton.setAlignmentX(0.5f);
		encodeFileButton.addActionListener(handler);
		//panel.add(encodeFileButton);
		
		decodeFileButton = new JButton("Decode a File");
		decodeFileButton.setAlignmentX(0.5f);
		decodeFileButton.addActionListener(handler);
		//panel.add(decodeFileButton);
		
		//panel.add(new JLabel("   "));
		
		advLabel = new JLabel("XLSB Steganography");
		advLabel.setAlignmentX(0.5f);
		panel.add(advLabel);
		
		advEncodeButton = new JButton("Encode any File");
		advEncodeButton.setAlignmentX(0.5f);
		advEncodeButton.addActionListener(handler);
		panel.add(advEncodeButton);
		
		advDecodeButton = new JButton("Decode a File");
		advDecodeButton.setAlignmentX(0.5f);
		advDecodeButton.addActionListener(handler);
		panel.add(advDecodeButton);
		
		panel.add(new JLabel("   "));
		
		stegoanalysisLabel = new JLabel("Automatized Steganalysis");
		stegoanalysisLabel.setAlignmentX(0.5f);
		panel.add(stegoanalysisLabel);
		
		analysisFridrichGroupButton = new JButton("Analyze Fridrich\'s Groups");
		analysisFridrichGroupButton.setAlignmentX(0.5f);
		analysisFridrichGroupButton.addActionListener(handler);
		panel.add(analysisFridrichGroupButton);
		
		analysisColorButton = new JButton("Analyze Lee\'s Color Cubes");
		analysisColorButton.setAlignmentX(0.5f);
		analysisColorButton.addActionListener(handler);
		panel.add(analysisColorButton);
		
		analysisSamplePairsButton = new JButton("Analyze Sample Pairs");
		analysisSamplePairsButton.setAlignmentX(0.5f);
		analysisSamplePairsButton.addActionListener(handler);
		panel.add(analysisSamplePairsButton);
		
		panel.add(new JLabel("   "));
		
		analysisLabel = new JLabel("Analysis by Manual Inspection");
		analysisLabel.setAlignmentX(0.5f);
		panel.add(analysisLabel);
		
		diffButton = new JButton("Image Comparison");
		diffButton.setAlignmentX(0.5f);
		diffButton.addActionListener(handler);
		panel.add(diffButton);
		
		filterButton = new JButton("Image Filters");
		filterButton.setAlignmentX(0.5f);
		filterButton.addActionListener(handler);
		panel.add(filterButton);
		
		analysisButton = new JButton("Low Order Bits and Noise");
		analysisButton.setAlignmentX(0.5f);
		analysisButton.addActionListener(handler);
		panel.add(analysisButton);
		
		waveVisualizerButton = new JButton("Wave Visualizer");
		waveVisualizerButton.setAlignmentX(0.5f);
		waveVisualizerButton.addActionListener(handler);
		panel.add(waveVisualizerButton);
		
		panel.add(new JLabel("   "));
		
		configButton = new JButton("Configuration");
		configButton.setAlignmentX(0.5f);
		configButton.addActionListener(handler);
		panel.add(configButton);
		
		quitButton = new JButton("Quit");
		quitButton.setAlignmentX(0.5f);
		quitButton.addActionListener(handler);
		panel.add(quitButton);
		
		panel.add(new JLabel("   "));
		
		copyrightLabel = new JLabel("<html><body>Copyright (c) Arik Z. Lakritz, Peter Macko,<br/>"
				+ "and David K. Wittenberg</body></html>");
		copyrightLabel.setFont(new Font("Arial", 0, 11));
		copyrightLabel.setAlignmentX(0.5f);
		panel.add(copyrightLabel);
		

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
			
			if (event.getSource() == encodeImageButton) {
				(new EncoderFrame()).run();
			}
			
			if (event.getSource() == decodeImageButton) {
				(new DecoderFrame()).run();
			}
			
			if (event.getSource() == encodeFileButton) {
				(new FileEncoderFrame()).run();
			}
			
			if (event.getSource() == decodeFileButton) {
				(new FileDecoderFrame()).run();
			}
			
			if (event.getSource() == advEncodeButton) {
				(new AdvEncoderFrame()).run();
			}
			
			if (event.getSource() == advDecodeButton) {
				(new AdvDecoderFrame()).run();
			}
			
			if (event.getSource() == analysisButton) {
				(new AnalysisFrame()).run();
			}
			
			if (event.getSource() == analysisColorButton) {
				(new ColorAnalysisFrame()).run();
			}
			
			if (event.getSource() == analysisSamplePairsButton) {
				(new SamplePairAnalysisFrame()).run();
			}
			
			if (event.getSource() == analysisFridrichGroupButton) {
				(new FridrichGroupAnalysisFrame()).run();
			}
			
			if (event.getSource() == filterButton) {
				(new FilterFrame()).run();
			}
			
			if (event.getSource() == diffButton) {
				(new DiffFrame()).run();
			}
			
			if (event.getSource() == waveVisualizerButton) {
				(new WaveVisualizer()).run();
			}
			
			if (event.getSource() == configButton) {
				(new ConfigFrame()).run();
			}
			
			if (event.getSource() == quitButton) {
				System.gc();
				System.exit(0);
			}
		}
	}
}
