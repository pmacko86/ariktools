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
import javax.swing.event.*;

import com.aific.ariktools.stego.Decoder;
import com.aific.ariktools.util.Utils;

public class WaveVisualizer extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 2471294499804912018L;
	
	private EventHandler handler;
	private ScrollHandler scrollHandler;
	
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

	private JPanel wavePanel;
	private JLabel waveLabel;
	private BufferedImage waveImage;
	
	private JPanel waveFuncPanel;
	private JTextField posXField;
	private JTextField posYField;
	private JTextField bitsField;
	private JButton directionButton;
	private JCheckBox redBox;
	private JCheckBox greenBox;
	private JCheckBox blueBox;
	private JButton saveButton;
	
	private int posX, posY;
	private int rgb[];
	
	private int columnSpacing = 10;
	private boolean horizontal = true;
	private int bits = 8;
	
	private boolean disableUpdateWavePos = false;
	
	// Constructor
	public WaveVisualizer() {
		super("Wave Visualizer");
		
		handler = new EventHandler();
		scrollHandler = new ScrollHandler();
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setPreferredSize(new Dimension(800,630));
		getContentPane().add(panel);
		
		
		sourcePanel = new JPanel();
		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));
		
		sourceLabel = new JLabel("Source Image");
		sourcePanel.add(sourceLabel);
		
		sourceImageLabel = new JLabel();
		sourceImageLabel.setText("(not loaded)");
		sourceImageLabel.addMouseListener(handler);
		sourceImageLabel.addMouseMotionListener(handler);
		
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
		lowBitsImageLabel.addMouseListener(handler);
		lowBitsImageLabel.addMouseMotionListener(handler);
		
		lowBitsScroller = new JScrollPane(lowBitsImageLabel);
        lowBitsScroller.setPreferredSize(new Dimension(200,200));
		lowBitsPanel.add(lowBitsScroller);
		
		lowBitsButton = new JButton("Save as Image");
		lowBitsButton.addActionListener(handler);
		lowBitsButton.setEnabled(false);
		lowBitsPanel.add(lowBitsButton);
        
        wavePanel = new JPanel();
        wavePanel.setLayout(new BorderLayout());
        
        waveImage = new BufferedImage(760, 300, BufferedImage.TYPE_INT_RGB);
        waveLabel = new JLabel(new ImageIcon(waveImage));
		waveLabel.addMouseListener(scrollHandler);
		waveLabel.addMouseMotionListener(scrollHandler);
        wavePanel.add(waveLabel, BorderLayout.CENTER);
        
        waveFuncPanel = new JPanel();
        wavePanel.add(waveFuncPanel, BorderLayout.SOUTH);
        
        posXField = new JTextField("  n/a ");
        posXField.addActionListener(handler);
        posXField.getDocument().addDocumentListener(handler);
        waveFuncPanel.add(posXField);
        
        waveFuncPanel.add(new JLabel(" : "));
        
        posYField = new JTextField(" n/a ");
        posYField.addActionListener(handler);
        posYField.getDocument().addDocumentListener(handler);
        waveFuncPanel.add(posYField);
        
        directionButton = new JButton(horizontal ? "Horizontal" : "Vertical");
        directionButton.setMnemonic(KeyEvent.VK_T);
        directionButton.addActionListener(handler);
        waveFuncPanel.add(directionButton);

        waveFuncPanel.add(new JLabel(" | "));
        
        redBox = new JCheckBox("Red");
        redBox.setMnemonic(KeyEvent.VK_R);
        redBox.addActionListener(handler);
        redBox.setSelected(true);
        waveFuncPanel.add(redBox);
        
        greenBox = new JCheckBox("Green");
        greenBox.setMnemonic(KeyEvent.VK_G);
        greenBox.addActionListener(handler);
        greenBox.setSelected(true);
        waveFuncPanel.add(greenBox);
        
        blueBox = new JCheckBox("Blue");
        blueBox.setMnemonic(KeyEvent.VK_B);
        blueBox.addActionListener(handler);
        blueBox.setSelected(true);
        waveFuncPanel.add(blueBox);

        waveFuncPanel.add(new JLabel(" Display Bits:"));
        
        bitsField = new JTextField(" n/a ");
        bitsField.addActionListener(handler);
        bitsField.getDocument().addDocumentListener(handler);
        waveFuncPanel.add(bitsField);

        waveFuncPanel.add(new JLabel(" | "));
        
        saveButton = new JButton("Save as Image");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.addActionListener(handler);
        saveButton.setEnabled(false);
        waveFuncPanel.add(saveButton);
		
        sourcePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourcePanel, lowBitsPanel);
        sourcePane.setOneTouchExpandable(true);
        sourcePane.setDividerLocation(400);
			
        generalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePane, wavePanel);
        generalPane.setOneTouchExpandable(true);
        generalPane.setDividerLocation(280);
        
        clearResults();
		
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
		
		rgb = null;
		
		lowBitsImageLabel.setIcon(null);
		lowBitsImageLabel.setText("(not generated)");
		lowBitsImageIcon = null;
		lowBitsButton.setEnabled(false);

		saveButton.setEnabled(false);
		
		posX = 0;
		posY = 0;
		
		paintWave();
	}
	
	private void grabRGB() {
		
		rgb = new int[sourceImage.getIconWidth() * sourceImage.getIconHeight()];
		PixelGrabber pg = new PixelGrabber(sourceImage.getImage(), 0, 0, sourceImage.getIconWidth(),
				sourceImage.getIconHeight(), rgb, 0, sourceImage.getIconWidth());
		
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for pixels!");
		}
		
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			throw new RuntimeException("Image fetch aborted or errored");
		}
		
		for (int i = 0; i < rgb.length; i++) rgb[i] &= 0xffffff;
	}
	
	protected void updateWavePos() {
		if (disableUpdateWavePos) return;
		try {
			posX = Integer.parseInt(posXField.getText());
		}
		catch (Exception e) {
		}
		try {
			posY = Integer.parseInt(posYField.getText());
		}
		catch (Exception e) {
		}
		try {
			bits = Integer.parseInt(bitsField.getText());
		}
		catch (Exception e) {
		}
		validateWavePos();
		paintWave();
	}
	
	protected void validateWavePos() {
		if (posX < 0) posX = 0;
		if (posY < 0) posY = 0;
		if (sourceImage != null) {
			int w = sourceImage.getIconWidth();
			int h = sourceImage.getIconHeight();
			if (posX >= w) posX = w - 1;
			if (posY >= h) posY = h - 1;
		}
		if (bits < 0) bits = 0;
		if (bits > 8) bits = 8;
	}
	
	protected void paintWave() {

		Graphics G = waveImage.getGraphics();
		G.setColor(Color.WHITE);
		G.fillRect(0, 0, waveImage.getWidth(), waveImage.getHeight());

		int hbase = 15;
		int iwidth = waveImage.getWidth() - 18;
		int wheight = waveImage.getHeight() - 31;
		int wbase = waveImage.getHeight() - 10;

		G.setColor(Color.LIGHT_GRAY);
		G.drawLine(hbase + 1, wbase - wheight - 2, hbase + iwidth, wbase - wheight - 2);
		//G.drawLine(hbase + iwidth, wbase - wheight - 2, hbase + iwidth - 4, wbase - wheight - 2 - 3);
		//G.drawLine(hbase + iwidth, wbase - wheight - 2, hbase + iwidth - 4, wbase - wheight - 2 + 3);
		G.drawLine(hbase + iwidth, wbase - 1, hbase + iwidth, wbase - wheight - 2);
		
		G.setColor(Color.BLACK);
		G.drawLine(hbase, wbase, hbase, 1);
		G.drawLine(hbase, 1, hbase - 3, 5);
		G.drawLine(hbase, 1, hbase + 3, 5);
		G.drawLine(hbase, wbase, hbase + iwidth, wbase);
		G.drawLine(hbase + iwidth, wbase, hbase + iwidth - 4, wbase - 3);
		G.drawLine(hbase + iwidth, wbase, hbase + iwidth - 4, wbase + 3);
		
		if ((sourceImage == null) || (rgb == null)) return;
		
		int size = (waveImage.getWidth() - 20) / columnSpacing;
		if (horizontal) {
			size = Math.min(size, sourceImage.getIconWidth());
		}
		else {
			size = Math.min(size, sourceImage.getIconHeight());
		}

		int pX = posX;
		int pY = posY;
		if (horizontal) pX -= size / 2; else pY -= size / 2;
		
		if (pX <  0) pX = 0;
		if (pY <  0) pY = 0;
		if (pX >= sourceImage.getIconWidth() ) pX = sourceImage.getIconWidth()  - (horizontal ? size : 1);
		if (pY >= sourceImage.getIconHeight()) pY = sourceImage.getIconHeight() - (horizontal ? 1 : size);
		
		G.setColor(Color.BLACK);
		G.drawString(coverFile.getName() + ": " + (horizontal ? "Horizontal" : "Vertical") + " Wave from "
				+ pX + " : " + pY + " to " + (horizontal ? "" + (pX + size - 1) + " : " + pY :
					"" + pX + " : " + (pY + size - 1)) + (bits < 8 ? " (showing " + bits
							+ " least significant bit" + (bits == 1 ? "" : "s") + ")" : ""), 30, 15);
		
		int dX = hbase + 1 - columnSpacing / 2;
		int p  = sourceImage.getIconWidth() * pY + pX;
		int step = horizontal ? 1 : sourceImage.getIconWidth();
		double hscale = wheight / (double)(Math.pow(2, bits) - 1);
		int mask = (int)Math.round(Math.pow(2, bits)) - 1;
		
		int rp = 0, gp = 0, bp = 0, dp = 0;
		
		boolean br = redBox.isSelected();
		boolean bg = greenBox.isSelected();
		boolean bb = blueBox.isSelected();
		
		for (int i = 0; i < size; i++, p += step, dX += columnSpacing) {
			
			int c = 0;
			try {
				c = rgb[p];
			}
			catch (IndexOutOfBoundsException e) {
				continue;
			}
			int r = (c >> 16) & 0xff;
			int g = (c >> 8) & 0xff;
			int b = c & 0xff;
			
			r &= mask;
			g &= mask;
			b &= mask;
			
			int ry = wbase - (int)Math.round(hscale * r) - 1;
			int gy = wbase - (int)Math.round(hscale * g) - 1;
			int by = wbase - (int)Math.round(hscale * b) - 1;
			
			if (i > 0) {
				if (br) {
					G.setColor(Color.RED);
					G.drawLine(dp + columnSpacing / 2, rp, dX + columnSpacing / 2, ry);
				}
				if (bg) {
					G.setColor(Color.GREEN.darker());
					G.drawLine(dp + columnSpacing / 2, gp, dX + columnSpacing / 2, gy);
				}
				if (bb) {
					G.setColor(Color.BLUE);
					G.drawLine(dp + columnSpacing / 2, bp, dX + columnSpacing / 2, by);
				}
			}
			
			rp = ry;
			gp = gy;
			bp = by;
			dp = dX;
		}
		
		waveLabel.repaint();
	}
	
	private class EventHandler implements ActionListener, MouseListener, MouseMotionListener, DocumentListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == loadCoverButton) {
				File f = Utils.chooseImage(WaveVisualizer.this, "Choose the source image", true);
				if (f == null) return;
				try {
					sourceImage = new ImageIcon(f.getAbsolutePath());
					if (sourceImage.getIconWidth() < 0) throw new Exception("Unrecognized image format");
					sourceImageLabel.setIcon(sourceImage);
					sourceImageLabel.setText("");
					coverFile = f;
					
					clearResults();
					grabRGB();
					
					posX = sourceImage.getIconWidth() / 2;
					posY = sourceImage.getIconHeight() / 2;
					posXField.setText("" + posX);
					posYField.setText("" + posY);
			        bitsField.setText("" + bits);
					
					lowBitsButton.setEnabled(false);
					loadCoverButton.setEnabled(false);
					
					lowBitsImage = Decoder.decode(sourceImage);
					lowBitsImageIcon = new ImageIcon(lowBitsImage);
					lowBitsImageLabel.setIcon(lowBitsImageIcon);
					lowBitsImageLabel.setText("");
					
					paintWave();
				}
				catch (Throwable e) {
					e.printStackTrace();
					sourceImageLabel.setIcon(null);
					sourceImageLabel.setText("(not loaded)");
					sourceImage = null;
					JOptionPane.showMessageDialog(null, "Cannot load the source image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				loadCoverButton.setEnabled(true);
				lowBitsButton.setEnabled(lowBitsImage != null);
		        saveButton.setEnabled(rgb != null);
				return;
			}
			
			if (event.getSource() == lowBitsButton) {
				File f = Utils.chooseImage(WaveVisualizer.this, "Save the lower bits as an image", false);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(WaveVisualizer.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), lowBitsImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the low-bits image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == saveButton) {
				File f = Utils.chooseImage(WaveVisualizer.this, "Save the signal wave as an image", false);
				if (f == null) return;
				if (f.exists()) {
					if (!Utils.shouldOverwrite(WaveVisualizer.this, f.getName())) return;
				}
				try {
					Utils.saveImage(f.getAbsolutePath(), waveImage);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot save the siganl wave: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				return;
			}
			
			if (event.getSource() == directionButton) {
				horizontal = !horizontal;
				directionButton.setText(horizontal ? "Horizontal" : "Vertical");
				paintWave();
			}
			
			if ((event.getSource() == posXField) || (event.getSource() == posYField) || (event.getSource() == bitsField)) {
				updateWavePos();
			}
			
			if ((event.getSource() == redBox) || (event.getSource() == greenBox) || (event.getSource() == blueBox)) {
				paintWave();
			}
		}
		
		public void mouseClicked(MouseEvent e)
		{
		}
		
		public void mouseReleased(MouseEvent e)
		{
		}
		
		public void mousePressed(MouseEvent e)
		{
			disableUpdateWavePos = true;
			posX = e.getX();
			posY = e.getY();
			validateWavePos();
			posXField.setText("" + posX);
			posYField.setText("" + posY);
			disableUpdateWavePos = false;
			paintWave();
		}
		
		public void mouseMoved(MouseEvent e)
		{
		}
		
		public void mouseDragged(MouseEvent e)
		{
			mousePressed(e);
		}
		
		public void mouseEntered(MouseEvent e)
		{
		}
		
		public void mouseExited(MouseEvent e)
		{
		}
		
		public void changedUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
		
		public void removeUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
		
		public void insertUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
	}
	
	private class ScrollHandler implements MouseListener, MouseMotionListener {
		
		@SuppressWarnings("unused")
		private int prevX, prevY;
		private double dX, dY;
		
		public ScrollHandler() {
			prevX = prevY = 0;
			dX = dY = 0;
		}
		
		public void mouseClicked(MouseEvent e)
		{
		}
		
		public void mouseReleased(MouseEvent e)
		{
		}
		
		public void mousePressed(MouseEvent e)
		{
			prevX = e.getX();
			prevY = e.getY();
			dX = posX;
			dY = posY;
		}
		
		public void mouseMoved(MouseEvent e)
		{
		}
		
		public void mouseDragged(MouseEvent e)
		{
			disableUpdateWavePos = true;
			int pX = e.getX();
			int pY = e.getY();
			if (horizontal) {
				dX -= (pX - prevX) / (double) columnSpacing;
				posX = (int)Math.round(dX);
			}
			else {
				dY -= (pX - prevX) / (double) columnSpacing;
				posY = (int)Math.round(dY);
			}
			prevX = pX;
			prevY = pY;
			validateWavePos();
			posXField.setText("" + posX);
			posYField.setText("" + posY);
			disableUpdateWavePos = false;
			paintWave();
		}
		
		public void mouseEntered(MouseEvent e)
		{
		}
		
		public void mouseExited(MouseEvent e)
		{
		}
		
		@SuppressWarnings("unused")
		public void changedUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
		
		@SuppressWarnings("unused")
		public void removeUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
		
		@SuppressWarnings("unused")
		public void insertUpdate(DocumentEvent e)
		{
			updateWavePos();
		}
	}
}
