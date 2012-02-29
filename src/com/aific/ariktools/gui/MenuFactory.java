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


import java.awt.event.*;
import javax.swing.*;

public class MenuFactory {

	public MenuFactory(JFrame frame) {
		new GlobalMenu(frame);
	}
	
	public static void createMenu(JFrame frame) {
		new MenuFactory(frame);
	}
	
	private class GlobalMenu {
		
		@SuppressWarnings("unused")
		private JFrame frame;
		private EventHandler handler;
		private JMenuBar mainMenu;
		
		private JMenu firstMenu;
		private JMenuItem aboutMenuItem;
		private JMenuItem configMenuItem;
		private JMenuItem quitMenuItem;
		
		private JMenu inImageMenu;
		private JMenuItem encodeImageMenuItem;
		private JMenuItem decodeImageMenuItem;
		private JMenuItem encodeFileMenuItem;
		private JMenuItem decodeFileMenuItem;
		
		private JMenu advImageMenu;
		private JMenuItem encodeAdvMenuItem;
		private JMenuItem decodeAdvMenuItem;
		
		private JMenu analysisMenu;
		private JMenuItem lowBitsMenuItem;
		private JMenuItem colorAnalysisMenuItem;
		private JMenuItem samplePairAnalysisMenuItem;
		private JMenuItem fridrichGroupAnalysisMenuItem;
		private JMenuItem waveVisualizerMenuItem;
		private JMenuItem filterMenuItem;
		
		private JMenu quickMenu;
		
		public GlobalMenu(JFrame frame) {
			
			this.frame = frame;
			handler = new EventHandler();
			
			mainMenu = new JMenuBar();
			
			if (!System.getProperty("os.name").startsWith("Mac")) {
				quickMenu = new JMenu("Tools");
			}
			else {
				quickMenu = null;
			}
			
			firstMenu = new JMenu("ArikTools");
			
			aboutMenuItem = new JMenuItem("About");
			aboutMenuItem.addActionListener(handler);
			firstMenu.add(aboutMenuItem);
			
			configMenuItem = new JMenuItem("Configuration");
			configMenuItem.addActionListener(handler);
			firstMenu.add(configMenuItem);
			
			firstMenu.addSeparator();
			
			quitMenuItem = new JMenuItem("Quit");
			quitMenuItem.addActionListener(handler);
			firstMenu.add(quitMenuItem);
			
			mainMenu.add(firstMenu);
			
			
			inImageMenu = new JMenu("LSB Steganography");
			
			encodeImageMenuItem = new JMenuItem("Encode an Image");
			encodeImageMenuItem.addActionListener(handler);
			inImageMenu.add(encodeImageMenuItem);
			
			decodeImageMenuItem = new JMenuItem("Decode an Image");
			decodeImageMenuItem.addActionListener(handler);
			inImageMenu.add(decodeImageMenuItem);
			
			encodeFileMenuItem = new JMenuItem("Encode any File");
			encodeFileMenuItem.addActionListener(handler);
			inImageMenu.add(encodeFileMenuItem);
			
			decodeFileMenuItem = new JMenuItem("Decode a File");
			decodeFileMenuItem.addActionListener(handler);
			inImageMenu.add(decodeFileMenuItem);
			
			if (quickMenu != null) quickMenu.add(inImageMenu); else mainMenu.add(inImageMenu);
			
			
			advImageMenu = new JMenu("XLSB Steganography");
			
			encodeAdvMenuItem = new JMenuItem("Encode any File");
			encodeAdvMenuItem.addActionListener(handler);
			advImageMenu.add(encodeAdvMenuItem);
			
			decodeAdvMenuItem = new JMenuItem("Decode a File");
			decodeAdvMenuItem.addActionListener(handler);
			advImageMenu.add(decodeAdvMenuItem);
			
			if (quickMenu != null) quickMenu.add(advImageMenu); else mainMenu.add(advImageMenu);
			
			
			analysisMenu = new JMenu("Analysis");
			
			colorAnalysisMenuItem = new JMenuItem("Lee\'s Color Cubes");
			colorAnalysisMenuItem.addActionListener(handler);
			analysisMenu.add(colorAnalysisMenuItem);
			
			fridrichGroupAnalysisMenuItem = new JMenuItem("Fridrich\'s Groups");
			fridrichGroupAnalysisMenuItem.addActionListener(handler);
			analysisMenu.add(fridrichGroupAnalysisMenuItem);
			
			samplePairAnalysisMenuItem = new JMenuItem("Sample Pairs");
			samplePairAnalysisMenuItem.addActionListener(handler);
			analysisMenu.add(samplePairAnalysisMenuItem);
			
			analysisMenu.addSeparator();
			
			filterMenuItem = new JMenuItem("Image Filters");
			filterMenuItem.addActionListener(handler);
			analysisMenu.add(filterMenuItem);
			
			lowBitsMenuItem = new JMenuItem("Low Order Bits and Noise");
			lowBitsMenuItem.addActionListener(handler);
			analysisMenu.add(lowBitsMenuItem);
			
			waveVisualizerMenuItem = new JMenuItem("Visualize the Signal Wave");
			waveVisualizerMenuItem.addActionListener(handler);
			analysisMenu.add(waveVisualizerMenuItem);
			
			if (quickMenu != null) quickMenu.add(analysisMenu); else mainMenu.add(analysisMenu);
			
			
			if (quickMenu != null) mainMenu.add(quickMenu);
				
			frame.setJMenuBar(mainMenu);
		}
		
		private class EventHandler implements ActionListener {
			
			public EventHandler() {
			}
			
			public void actionPerformed(ActionEvent event) {
				
				if (event.getSource() == encodeImageMenuItem) {
					(new EncoderFrame()).run();
				}
				
				if (event.getSource() == decodeImageMenuItem) {
					(new DecoderFrame()).run();
				}
				
				if (event.getSource() == encodeFileMenuItem) {
					(new FileEncoderFrame()).run();
				}
				
				if (event.getSource() == decodeFileMenuItem) {
					(new FileDecoderFrame()).run();
				}
				
				if (event.getSource() == encodeAdvMenuItem) {
					(new AdvEncoderFrame()).run();
				}
				
				if (event.getSource() == decodeAdvMenuItem) {
					(new AdvDecoderFrame()).run();
				}
				
				if (event.getSource() == lowBitsMenuItem) {
					(new AnalysisFrame()).run();
				}
				
				if (event.getSource() == colorAnalysisMenuItem) {
					(new ColorAnalysisFrame()).run();
				}
				
				if (event.getSource() == samplePairAnalysisMenuItem) {
					(new SamplePairAnalysisFrame()).run();
				}
				
				if (event.getSource() == fridrichGroupAnalysisMenuItem) {
					(new FridrichGroupAnalysisFrame()).run();
				}
				
				if (event.getSource() == waveVisualizerMenuItem) {
					(new WaveVisualizer()).run();
				}
				
				if (event.getSource() == filterMenuItem) {
					(new FilterFrame()).run();
				}
				
				if (event.getSource() == aboutMenuItem) {
					(new AboutFrame()).run();
				}
				
				if (event.getSource() == configMenuItem) {
					(new ConfigFrame()).run();
				}
				
				if (event.getSource() == quitMenuItem) {
					System.gc();
					System.exit(0);
				}
			}
		}
		
	}
	
}
