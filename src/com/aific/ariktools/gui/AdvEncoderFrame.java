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

import com.aific.ariktools.stego.AdvEncoder;
import com.aific.ariktools.stego.Decoder;
import com.aific.ariktools.util.Utils;

public class AdvEncoderFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = -7127553095071700938L;

	private EventHandler handler;
	
	private JPanel panel;
	private JSplitPane sourcePane;
	private JSplitPane encodePane;
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
	
	private BufferedImage lowBitsImage;
	private ImageIcon lowBitsImageIcon;
	
	private JPanel lowBitsPanel;
	private JLabel lowBitsLabel;
	private JLabel lowBitsImageLabel;
	private JScrollPane lowBitsScroller;
	private JButton lowBitGenerateButton;
	
	private BufferedImage finalImage;
	private ImageIcon finalImageIcon;
	
	private JPanel finalPanel;
	private JLabel finalLabel;
	private JLabel finalImageLabel;
	private JScrollPane finalScroller;
	private JButton finalButton;
	
	private BufferedImage lowBit2Image;
	private ImageIcon lowBit2ImageIcon;
	
	private BufferedImage lowBit2DPImage;
	private ImageIcon lowBit2DPImageIcon;
	
	private JPanel lowBit2Panel;
	private JLabel lowBit2Label;
	private JLabel lowBit2ImageLabel;
	private JScrollPane lowBit2Scroller;

	private JPanel statPanel;
	private JLabel statLabel;
	private JButton statButton;
	
	private JPanel statMainPanel;

	private JButton saveButton;
	
	private JLabel progressLabel;
	private JProgressBar progress;

	private JPanel buttonPanel;
	private JPanel encodePanel;
	
	private boolean showDataPlacement;

	
	// Constructor
	public AdvEncoderFrame() {
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
		
		
		sourcePanel = new JPanel();
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));
		sourcePanel.add(coverPanel);
		sourcePanel.add(filePanel);
		sourcePanel.add(sourceButtonPanel);
		
		
		lowBitsPanel = new JPanel();
		lowBitsPanel.setLayout(new BoxLayout(lowBitsPanel, BoxLayout.Y_AXIS));
		
		lowBitsLabel = new JLabel("Low Order Bits");
		lowBitsPanel.add(lowBitsLabel);
		
		lowBitsImageLabel = new JLabel();
		lowBitsImageLabel.setText("(not generated)");
		
		lowBitsScroller = new JScrollPane(lowBitsImageLabel);
        lowBitsScroller.setPreferredSize(new Dimension(200,200));
		lowBitsPanel.add(lowBitsScroller);
		
		lowBitGenerateButton = new JButton("Generate");
		lowBitGenerateButton.addActionListener(handler);
		lowBitGenerateButton.setEnabled(false);
		lowBitsPanel.add(lowBitGenerateButton);
		
		
		finalPanel = new JPanel();
		finalPanel.setLayout(new BoxLayout(finalPanel, BoxLayout.Y_AXIS));
		
		finalLabel = new JLabel("Encoded Image");
		finalPanel.add(finalLabel);
		
		finalImageLabel = new JLabel();
		finalImageLabel.setText("(not generated)");
		
		finalScroller = new JScrollPane(finalImageLabel);
        finalScroller.setPreferredSize(new Dimension(200,200));
		finalPanel.add(finalScroller);
		
		
		lowBit2Panel = new JPanel();
		lowBit2Panel.setLayout(new BoxLayout(lowBit2Panel, BoxLayout.Y_AXIS));
		
		lowBit2Label = new JLabel("Low Order Bits");
		lowBit2Panel.add(lowBit2Label);
		
		lowBit2ImageLabel = new JLabel();
		lowBit2ImageLabel.setText("(not generated)");
		
		lowBit2Scroller = new JScrollPane(lowBit2ImageLabel);
        lowBit2Scroller.setPreferredSize(new Dimension(200,200));
		lowBit2Panel.add(lowBit2Scroller);
		
		
		statMainPanel = new JPanel();
		statMainPanel.setLayout(new BoxLayout(statMainPanel, BoxLayout.Y_AXIS));
		
		statPanel = new JPanel();
		statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.X_AXIS));
		
		statLabel = new JLabel("N/A  ");
		statLabel.setAlignmentX(0.5f);
		statPanel.add(statLabel);
		
		statButton = new JButton("Show Data Placement");
		statButton.addActionListener(handler);
		statButton.setEnabled(false);
		showDataPlacement = false;
		statPanel.add(statButton);
		
		statMainPanel.add(lowBit2Panel);
		statMainPanel.add(statPanel);
		
		
        sourcePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourcePanel, lowBitsPanel);
        sourcePane.setOneTouchExpandable(true);
		sourcePane.addPropertyChangeListener(handler);
        sourcePane.setDividerLocation(400);
		
		
        encodePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, finalPanel, statMainPanel);
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
		
		buttonPanel.add(new JLabel("   "));		

		progressLabel = new JLabel("(idle)");
		buttonPanel.add(progressLabel);
		
		buttonPanel.add(new JLabel("   "));		
		
		progress = new JProgressBar();
		buttonPanel.add(progress);
		
		buttonPanel.add(new JLabel("   "));		
		
		
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
	
	private String getCapacityString() {
		String cover = (coverCapacity < 0) ? "---" : "" + Math.round((10 * coverCapacity / 1024.0) / 10.0) + " KB";
		String hide  = (hideSize      < 0) ? "---" : "" + Math.round((10 *      hideSize / 1024.0) / 10.0) + " KB";
		return hide + " / " + cover;
	}
	
	protected void clearResults() {
		finalImageLabel.setText("(not generated)");
		finalImageLabel.setIcon(null);
		lowBit2ImageLabel.setText("(not generated)");
		lowBit2ImageLabel.setIcon(null);
		finalImageIcon = null;
		lowBit2ImageIcon = null;
		statLabel.setText("N/A  ");
		saveButton.setEnabled(false);
		statButton.setEnabled(false);
	}
	
	private class EventHandler implements ActionListener, PropertyChangeListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(AdvEncoderFrame.this, "Choose the cover image", true);
				if (f == null) return;
				try {
					coverImage = new ImageIcon(f.getAbsolutePath());
					if (coverImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					coverCapacity = AdvEncoder.coverCapacity(coverImage);
					coverImageLabel.setIcon(coverImage);
					coverImageLabel.setText("");
					lowBitsImageLabel.setIcon(null);
					lowBitsImageLabel.setText("(not generated)");
					lowBitsImage = null;
				}
				catch (Exception e) {
					coverImageLabel.setIcon(null);
					coverImageLabel.setText("(not loaded)");
					coverImage = null;
					lowBitsImageLabel.setIcon(null);
					lowBitsImageLabel.setText("(not generated)");
					lowBitsImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the cover image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				clearResults();
				fileCapacityLabel.setText("Capacity: " + getCapacityString());
				lowBitGenerateButton.setEnabled(coverImage != null);
				if (coverImage != null && hideFile != null) {
					finalButton.setEnabled(hideSize <= coverCapacity);
				}
				else {
					finalButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == loadFileButton) {
				File f = Utils.chooseFile(AdvEncoderFrame.this, "Choose the file to hide", true);
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
				clearResults();
				fileCapacityLabel.setText("Capacity: " + getCapacityString());
				if (coverImage != null && hideFile != null) {
					finalButton.setEnabled(hideSize <= coverCapacity);
				}
				else {
					finalButton.setEnabled(false);
				}
				return;
			}
			
			if (event.getSource() == lowBitGenerateButton) {
				try {
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					lowBitsImage = Decoder.decode(coverImage);
					lowBitsImageIcon = new ImageIcon(lowBitsImage);
					lowBitsImageLabel.setIcon(lowBitsImageIcon);
					lowBitsImageLabel.setText("");
				}
				catch (Exception e) {
					lowBitsImageLabel.setIcon(null);
					lowBitsImageLabel.setText("(not generated)");
					lowBitsImage = null;
					JOptionPane.showMessageDialog(null, "Cannot generate the map of low order bits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == finalButton) {
				try {
					if (hideFile == null) throw new Exception("Secret file is not loaded");
					if (coverImage == null) throw new Exception("Cover image is not loaded");
					if (!hideFile.exists()) throw new Exception("The secret file does not exist");
					if (hideSize > coverCapacity) throw new Exception("The secret file is bigger than the cover capacity");
					Thread t = new Thread(new EncodeThread(coverImage, hideFile));
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
				File f = Utils.chooseImage(AdvEncoderFrame.this, "Save the encoded image", false, true);
				f = Utils.checkLosslessImageExt(AdvEncoderFrame.this, f);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(AdvEncoderFrame.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), finalImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the encoded image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == statButton) {
				if (showDataPlacement) {
					showDataPlacement = false;
					statButton.setText("Show Data Placement");
				}
				else {
					showDataPlacement = true;
					statButton.setText("Hide Data Placement");
				}
				lowBit2ImageLabel.setIcon(showDataPlacement ? lowBit2DPImageIcon : lowBit2ImageIcon);
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
	
	
	private class EncodeThread implements Runnable {
		
		private ImageIcon sourceImage;
		private File hideFile;
		
		public EncodeThread(ImageIcon sourceImage, File hideFile) {
			this.sourceImage = sourceImage;
			this.hideFile = hideFile;
		}
		
		public void run() {
			finalButton.setEnabled(false);
			lowBitGenerateButton.setEnabled(false);
			loadCoverButton.setEnabled(false);
			loadFileButton.setEnabled(false);
			saveButton.setEnabled(false);
			statButton.setEnabled(false);
			
			try {
				AdvEncoder encoder = new AdvEncoder(sourceImage, hideFile);
				encoder.setProgressBar(progress);
				encoder.setProgressLabel(progressLabel);
				
				finalImage = encoder.encode();
				finalImageIcon = new ImageIcon(finalImage);
				finalImageLabel.setIcon(finalImageIcon);
				finalImageLabel.setText("");
				
				progressLabel.setText("Preparing statistics");

				lowBit2Image = Decoder.decode(finalImageIcon);
				lowBit2ImageIcon = new ImageIcon(lowBit2Image);
				lowBit2DPImage = encoder.plotDataPlacement();
				lowBit2DPImageIcon = new ImageIcon(lowBit2DPImage);
				lowBit2ImageLabel.setIcon(showDataPlacement ? lowBit2DPImageIcon : lowBit2ImageIcon);
				lowBit2ImageLabel.setText("");

				lowBitsImage = Decoder.decode(coverImage);
				lowBitsImageIcon = new ImageIcon(lowBitsImage);
				lowBitsImageLabel.setIcon(lowBitsImageIcon);
				lowBitsImageLabel.setText("");
				
				statLabel.setText("" + (Math.round(AdvEncoder.FLOAT_TO_INT * encoder.getLeastRandom()) / AdvEncoder.FLOAT_TO_INT)
								  + " / " + (Math.round(AdvEncoder.FLOAT_TO_INT * encoder.getDataPlacement()) / AdvEncoder.FLOAT_TO_INT) + "  ");
			}
			catch (Throwable e) {
				clearResults();
				JOptionPane.showMessageDialog(null, "Cannot encode the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			
			finalButton.setEnabled(true);
			lowBitGenerateButton.setEnabled(true);
			loadCoverButton.setEnabled(true);
			loadFileButton.setEnabled(true);
			saveButton.setEnabled(finalImage != null);
			statButton.setEnabled(finalImage != null);
			
			progressLabel.setText("(idle)");
			progress.setIndeterminate(false);
			progress.setValue(0);
		}
	}
}
