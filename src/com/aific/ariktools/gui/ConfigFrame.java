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

import com.aific.ariktools.analysis.RandomnessTest;
import com.aific.ariktools.stego.AdvEncoder;
import com.aific.ariktools.util.Utils;

public class ConfigFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = -6178351624010539900L;

	protected static final int BORDER_SIZE = 10;
	
	private EventHandler handler;
	
	private JPanel panel;
	
	private JLabel titleLabel;
	
	private JLabel generalLabel;
	private JCheckBox approximateRandomnessBox;

	private JLabel xlsbLabel;
	private JPanel lsbBitsPanel;
	private JLabel lsbBitsLabel;
	private JTextField lsbBitsField;
	private JCheckBox useGaussianNormBox;
	
	private JButton applyButton;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel buttonPanel;
	
	// Constructor
	public ConfigFrame() {
		super("Configuration");
		
		handler = new EventHandler();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		getContentPane().add(panel);
		
		titleLabel = new JLabel("Configuration");
		titleLabel.setFont(new Font("Arial Black", Font.ITALIC, 16));
		titleLabel.setAlignmentX(0.5f);
		panel.add(titleLabel);
		
		
		panel.add(new JLabel("   "));
		
		generalLabel = new JLabel("General Settings");
		generalLabel.setFont(generalLabel.getFont().deriveFont(Font.BOLD));
		generalLabel.setAlignmentX(0.5f);
		panel.add(generalLabel);
		
		approximateRandomnessBox = new JCheckBox("Approximate randomness testing", RandomnessTest.approximate);
		approximateRandomnessBox.setAlignmentX(0.5f);
		panel.add(approximateRandomnessBox);
		

		panel.add(new JLabel("   "));
		
		xlsbLabel = new JLabel("XLSB Steganography");
		xlsbLabel.setFont(generalLabel.getFont().deriveFont(Font.BOLD));
		xlsbLabel.setAlignmentX(0.5f);
		panel.add(xlsbLabel);
		
		panel.add(new JLabel("   "));
		
		lsbBitsPanel = new JPanel();
		lsbBitsPanel.setLayout(new BoxLayout(lsbBitsPanel, BoxLayout.X_AXIS));
		lsbBitsLabel = new JLabel("LSB Bits: ");
		lsbBitsPanel.add(lsbBitsLabel);
		lsbBitsField = new JTextField("" + AdvEncoder.getLSBBits());
		lsbBitsPanel.add(lsbBitsField);
		panel.add(lsbBitsPanel);
		
		useGaussianNormBox = new JCheckBox("Normalize towards the Gaussian mean", AdvEncoder.useGaussianNorm);
		useGaussianNormBox.setAlignmentX(0.5f);
		panel.add(useGaussianNormBox);
		
		panel.add(new JLabel("   "));

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		applyButton = new JButton("Apply");
		applyButton.setAlignmentX(0.5f);
		applyButton.addActionListener(handler);
		buttonPanel.add(applyButton);
		
		okButton = new JButton("OK");
		okButton.setAlignmentX(0.5f);
		okButton.addActionListener(handler);
		buttonPanel.add(okButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setAlignmentX(0.5f);
		cancelButton.addActionListener(handler);
		buttonPanel.add(cancelButton);
		
		panel.add(buttonPanel);
		
		
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
	
	
	/**
	 * Apply the changes
	 */
	protected void apply() {

		RandomnessTest.approximate = approximateRandomnessBox.isSelected();
		
		try {
			int i = Integer.parseInt(lsbBitsField.getText());
			AdvEncoder.setLSBBits(i);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Invalid number of LSB bits");
		}
		
		AdvEncoder.useGaussianNorm = useGaussianNormBox.isSelected();
	}
	
	private class EventHandler implements ActionListener {
		
		public EventHandler() {
		}
		
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == applyButton) {
				apply();
			}
			
			if (event.getSource() == okButton) {
				apply();
				setVisible(false);
			}
			
			if (event.getSource() == cancelButton) {
				setVisible(false);
			}
		}
	}
}
