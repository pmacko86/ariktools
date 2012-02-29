package com.aific.ariktools.analysis;

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


import java.awt.image.*;
import javax.swing.*;

public class FridrichGroupAnalysis {
	
	private ImageIcon cover;
	
	public static final int[] DEFAULT_MASK = {0, 1, 1, 0};
	
	private int[] rgb;
	
	private int width;
	private int height;
	
	private JProgressBar progress;
	private JLabel label;
	
	private int[] mask, inverseMask;
	
	private boolean[] hasValues;
	private double[] messageLength;
	
	
	public FridrichGroupAnalysis(ImageIcon cover) {
		this.cover = cover;
		if (cover == null) throw new IllegalArgumentException("The image is not loaded");
		
		width = cover.getIconWidth();
		height = cover.getIconHeight();
		if ((width <= 0) || (height <= 0)) throw new IllegalArgumentException("The image is not properly loaded");
		
		progress = null;
		label = null;
		
		hasValues = new boolean[3];
		messageLength = new double[3];
		
		for (int i = 0; i < 3; i++) hasValues[i] = false;
		
		setMask(DEFAULT_MASK);
	}
	
	
	public void setProgressBar(JProgressBar progress) {
		this.progress = progress;
	}
	
	public void setProgressLabel(JLabel label) {
		this.label = label;
	}
	
	
	public void setMask(int[] newMask) {
		
		if (newMask.length < 2) return;
		
		mask = new int[newMask.length];
		inverseMask = new int[newMask.length];

		for (int i = 0; i < mask.length; i++) {
			mask[i] = newMask[i];
			inverseMask[i] = -newMask[i];
		}
	}
	
	
	/*
	 * Analyzer
	 */
	
	public void analyze() throws Exception {
		
		if (label != null) label.setText("Analyzing");
		if (progress != null) progress.setIndeterminate(true);
		
		// Initialize
		
		rgb = grabRGB();
		
		for (int i = 0; i < 3; i++) hasValues[i] = false;
		
		// Do the analysis
		
		FridrichGroupHelper[] fgh = new FridrichGroupHelper[3];
		
		for (int i = 0; i < 3; i++) {
			fgh[i] = new FridrichGroupHelper(i);
			fgh[i].start();
		}
		
		for (int i = 0; i < 3; i++) {
			fgh[i].join();
		}

		if (progress != null) {
			progress.setIndeterminate(true);
		}
		
		// Save the results
				
		for (int i = 0; i < 3; i++) {
			if (fgh[i].hasFailed()) continue;
			hasValues[i] = true;
			messageLength[i] = fgh[i].getMessageLength();
		}

		if (label != null) label.setText("(idle)");
	}
	
	
	/*
	 * Retrieving results
	 */
	
	
	public boolean hasValues(int component) {
		return hasValues[component];
	}
	
	
	public double getMessageLength(int component) {
		return messageLength[component];
	}
	
	
	public String generateReport(String coverFileName) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>\n");
		sb.append("<head><title>Fridrich\'s Group Analysis of " + coverFileName + "</title></head>\n");
		sb.append("<body><center>\n");
		
		sb.append("<h1>Fridrich\'s Group Analysis of " + coverFileName + "</h1>\n");
		sb.append("<p>" + cover.getIconWidth() + " x " + cover.getIconHeight() + "</p>\n");
		sb.append("<p></p>\n");
		
		sb.append("<h3>Mask: [");
		sb.append(mask[0]);
		for (int i = 1; i < mask.length; i++) {
			sb.append(", ");
			sb.append(mask[i]);
		}
		sb.append("]</h3>\n");
		
		sb.append("<p></p>\n");
		sb.append("<table>\n");
		sb.append("<tr><td><b>Component&nbsp;</b></td><td><b>Message Length&nbsp;</b></td></tr>\n");
		
		double sumLength = 0; int count = 0;
		for (int i = 2; i >= 0; i--) {
			sb.append("<tr><td><b>");
			if (i == 2) sb.append("Red");
			if (i == 1) sb.append("Green");
			if (i == 0) sb.append("Blue");
			sb.append("</b></td>");
			if (hasValues[i]) {
				sb.append("<td align=\"center\">" + (Math.round(messageLength[i] * 10000) / 100.0) + " %</td>");
				sumLength += messageLength[i];
				count++;
			}
			else {
				sb.append("<td align=\"center\">N/A</td>");
			}
			sb.append("</tr>\n");
		}
		
		if (count > 0) {
			sb.append("<tr><td><b>Average</b></td>");
			sb.append("<td align=\"center\">" + (Math.round((sumLength / count) * 10000) / 100.0) + " %</td>");
			sb.append("</tr>\n");
		}
		
		sb.append("</table>\n");
		
		sb.append("</center></body></html>\n");
		
		return sb.toString();
	}
	
	
	public String generateLineReport() {
		StringBuffer sb = new StringBuffer();
		
		double sumLength = 0; int count = 0;
		for (int i = 2; i >= 0; i--) {
			if (hasValues[i]) {
				sb.append("  <td align=\"center\">&nbsp;" + (Math.round(messageLength[i] * 10000) / 100.0) + "&nbsp;");
				sb.append("%&nbsp;</td>\n");
				sumLength += messageLength[i];
				count++;
			}
			else {
				sb.append("  <td align=\"center\">N/A</td>\n");
			}
		}
		
		if (count > 0) {
			sb.append("  <td align=\"center\">&nbsp;<b>" + (Math.round((sumLength / count) * 10000) / 100.0) + "&nbsp;");
			sb.append("%</b>&nbsp;</td>\n");
		}
		else {
			sb.append("  <td align=\"center\">N/A</td>\n");
		}
		
		return sb.toString();
	}
	
	
	public static String generateLineReportHeader() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("  <td align=\"center\"><b>Red</b></td>\n");
		sb.append("  <td align=\"center\"><b>Green</b></td>\n");
		sb.append("  <td align=\"center\"><b>Blue</b></td>\n");
		sb.append("  <td align=\"center\"><b>Average</b></td>\n");
		
		return sb.toString();
	}
	
	
	public static int getLineReportColumns() {
		return 4;
	}
	
	
	/*
	 * Helper functions
	 */
	
	private int[] grabRGB() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		int[] rgb = new int[width * height];
		PixelGrabber pg = new PixelGrabber(cover.getImage(), 0, 0, width, height, rgb, 0, width);
		
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for pixels!");
		}
		
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			throw new RuntimeException("Image fetch aborted or errored");
		}
		
		for (int i = 0; i < rgb.length; i++) rgb[i] &= 0xffffff;
				
		return rgb;
	}
	
	
	/*
	 * Helper class
	 */
	
	private class FridrichGroupHelper extends Thread {
		
		private int wave, waveshift;	// {-1, 0, 1, 2} (an index of the color component, use -1 for intensity)
		
		private String error;
		
		private int Rm, Rmi, Sm, Smi, Rm1, Rmi1, Sm1, Smi1;
		
		private double messageLength;
		
		
		public FridrichGroupHelper(int wave) {
			
			this.wave = wave;
			
			waveshift = wave * 8;
			
			error = null;
		}
		
		
		public boolean hasFailed() {
			return error != null;
		}
		
		
		@SuppressWarnings("unused")
		public String getErrorMessage() {
			return error;
		}
		
		
		public double getMessageLength() {
			return messageLength;
		}
		

		public void run() {
			
			// Initialize
			
			error = null;
			
			int ml = mask.length;
			
			int[] points = new int[mask.length];
			
			Rm = Rmi = Sm = Smi = Rm1 = Rmi1 = Sm1 = Smi1 = 0;
			
			// Categorize horizontal pairs
				
			for (int y = 0; y < height; y++) {
				for (int x = ml - 1; x < width; x++) {
					for (int i = 0; i < points.length; i++) {
						points[i] = value(x - i, y);
					}
					categorize(points);
				}
			}
			
			// Categorize vertical pairs
			
			for (int y = ml - 1; y < height; y++) {
				for (int x = 0; x < width; x++) {
					for (int i = 0; i < points.length; i++) {
						points[i] = value(x, y - i);
					}
					categorize(points);
				}
			}
			
			// Solve the quadratic formula
			
			int d0 = Rm - Sm;
			int d1 = Rm1 - Sm1;
			int d0i = Rmi - Smi;
			int d1i = Rmi1 - Smi1;
			
			double a = 2 * (d1 + d0);
			double b = d0i - d1i - d1 - 3 * d0;
			double c = d0 - d0i;
			
			double D = b * b - 4 * a * c;
			
			if (D < 0) { error = "The image cannot be analyzed"; return; }
			
			double r1 = (-b + Math.sqrt(D)) / (2 * a);
			double r2 = (-b - Math.sqrt(D)) / (2 * a);
			
			double x = Math.min(r1, r2); //r1 < 0 ? r2 : (r2 < 0 ? r1 : Math.min(r1, r2));
			
			// Determine the message length
			
			double p = x / (x - 0.5);
			
			if (p < 0) { error = "The image cannot be analyzed"; return; }
			
			// Save the result
			
			messageLength = x / (x - 0.5);
		}
		
		
		private int value(int x, int y) {
			
			// Fetch the RGB values
			
			int c = rgb[width * y + x];
			
			// Choose the desired wave
			
			if (wave < 0) {
				c = ((c) & 0xff + (c >> 8) & 0xff + (c >> 16) & 0xff) / 3;
			}
			else if (waveshift != 0) {
				c = (c >> waveshift) & 0xff;
			}
			else {
				c &= 0xff;
			}
			
			return c;
		}
		
		
		private int[] apply(int[] group, int[] mask) {

			for (int i = 0; i < group.length; i++) {
				
				if (mask[i] == 0) {
					// nothing to do
				}
				else if (mask[i] > 0) {
					group[i] = group[i] ^ mask[i];
				}
				else if (mask[i] < 0) {
					group[i] = ((group[i] + 1) ^ (-mask[i])) - 1;
				}
			}
			
			return group;
		}
		
		
		private int[] flipLSB(int[] group) {
			
			for (int i = 0; i < group.length; i++) {
				group[i] ^= 1;
			}
			
			return group;
		}
		
		
		private int[] clone(int[] group) {
			
			int[] output = new int[group.length];
			
			for (int i = 0; i < group.length; i++) {
				output[i] = group[i];
			}
			return output;
		}
		
		
		private int discriminator(int[] group) {
			
			int d = 0;
			
			for (int i = 1; i < group.length; i++) {
				d += Math.abs(group[i - 1] - group[i]);
			}
			return d;
		}
		
		
		private void categorize(int[] group) {
			
			int[] G    = group;
			int[] G1   = flipLSB(clone(G));
			
			int dG    = discriminator(group);
			int dG1   = discriminator(flipLSB(clone(G)));
			int dGm   = discriminator(apply(clone(G), mask));
			int dGmi  = discriminator(apply(clone(G), inverseMask));
			int dGm1  = discriminator(apply(clone(G1), mask));
			int dGmi1 = discriminator(apply(clone(G1), inverseMask));
			
			if (dGm > dG) Rm++;
			if (dGm < dG) Sm++;
			
			if (dGmi > dG) Rmi++;
			if (dGmi < dG) Smi++;
			
			if (dGm1 > dG1) Rm1++;
			if (dGm1 < dG1) Sm1++;
			
			if (dGmi1 > dG1) Rmi1++;
			if (dGmi1 < dG1) Smi1++;
		}
	}
	
}
