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

public class SamplePairAnalysis {
	
	private ImageIcon cover;
	
	private int lsbBits = 1;
	
	private int[] rgb;
	
	private int width;
	private int height;
	
	private JProgressBar progress;
	private JLabel label;
	
	private boolean[] hasValues;
	private double[] messageLength;
	private double[] errorBound;
	
	
	public SamplePairAnalysis(ImageIcon cover) {
		this.cover = cover;
		if (cover == null) throw new IllegalArgumentException("The image is not loaded");
		
		width = cover.getIconWidth();
		height = cover.getIconHeight();
		if ((width <= 0) || (height <= 0)) throw new IllegalArgumentException("The image is not properly loaded");
		
		progress = null;
		label = null;
		
		hasValues = new boolean[3];
		messageLength = new double[3];
		errorBound = new double[3];
		
		for (int i = 0; i < 3; i++) hasValues[i] = false;
	}
	
	
	public void setProgressBar(JProgressBar progress) {
		this.progress = progress;
	}
	
	public void setProgressLabel(JLabel label) {
		this.label = label;
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

		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setMaximum(128 * 3);
			progress.setValue(0);
		}
		
		SamplePairHelper[] sph = new SamplePairHelper[3];
		
		for (int i = 0; i < 3; i++) {
			sph[i] = new SamplePairHelper(0, 127, i);
			sph[i].start();
		}
		
		for (int i = 0; i < 3; i++) {
			sph[i].join();
		}

		if (progress != null) {
			progress.setIndeterminate(true);
		}
		
		// Save the results
				
		for (int i = 0; i < 3; i++) {
			if (sph[i].hasFailed()) continue;
			hasValues[i] = true;
			messageLength[i] = sph[i].getMessageLength();
			errorBound[i] = sph[i].getErrorBound();
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
	
	
	public double getErrorBound(int component) {
		return errorBound[component];
	}
	
	
	public String generateReport(String coverFileName) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>\n");
		sb.append("<head><title>Sample Pair Analysis of " + coverFileName + "</title></head>\n");
		sb.append("<body><center>\n");
		
		sb.append("<h1>Sample Pair Analysis of " + coverFileName + "</h1>\n");
		sb.append("<p>" + cover.getIconWidth() + " x " + cover.getIconHeight() + "</p>\n");
		sb.append("<p></p>");
		sb.append("<table>\n");
		sb.append("<tr><td><b>Component&nbsp;</b></td><td><b>Message Length&nbsp;</b></td><td><b>Error Bound</b></td></tr>\n");
		
		double sumLength = 0, sumError = 0; int count = 0;
		for (int i = 2; i >= 0; i--) {
			sb.append("<tr><td><b>");
			if (i == 2) sb.append("Red");
			if (i == 1) sb.append("Green");
			if (i == 0) sb.append("Blue");
			sb.append("</b></td>");
			if (hasValues[i]) {
				sb.append("<td align=\"center\">" + (Math.round(messageLength[i] * 10000) / 100.0) + " %</td>");
				sb.append("<td align=\"center\">&plusmn; " + (Math.round(errorBound[i] * 10000) / 100.0) + " %</td>");
				sumLength += messageLength[i];
				sumError += errorBound[i];
				count++;
			}
			else {
				sb.append("<td align=\"center\">N/A</td><td align=\"center\">N/A</td>");
			}
			sb.append("</tr>\n");
		}
		
		if (count > 0) {
			sb.append("<tr><td><b>Average</b></td>");
			sb.append("<td align=\"center\">" + (Math.round((sumLength / count) * 10000) / 100.0) + " %</td>");
			sb.append("<td align=\"center\">&plusmn; " + (Math.round((sumError / count) * 10000) / 100.0) + " %</td>");
			sb.append("</tr>\n");
		}
		
		sb.append("</table>\n");
		
		sb.append("</center></body></html>\n");
		
		return sb.toString();
	}
	
	
	public String generateLineReport() {
		StringBuffer sb = new StringBuffer();
		
		double sumLength = 0, sumError = 0; int count = 0;
		for (int i = 2; i >= 0; i--) {
			if (hasValues[i]) {
				sb.append("  <td align=\"center\">&nbsp;" + (Math.round(messageLength[i] * 10000) / 100.0) + "&nbsp;");
				sb.append("&plusmn;&nbsp;" + (Math.round(errorBound[i] * 10000) / 100.0) + "&nbsp;%&nbsp;</td>\n");
				sumLength += messageLength[i];
				sumError += errorBound[i];
				count++;
			}
			else {
				sb.append("  <td align=\"center\">N/A</td>\n");
			}
		}
		
		if (count > 0) {
			sb.append("  <td align=\"center\">&nbsp;<b>" + (Math.round((sumLength / count) * 10000) / 100.0) + "&nbsp;");
			sb.append("&plusmn;&nbsp;" + (Math.round((sumError / count) * 10000) / 100.0) + "&nbsp;%</b>&nbsp;</td>\n");
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
	
	private class SamplePairHelper extends Thread {
		
		@SuppressWarnings("unused")
		private int m, mi, mj, m1, m2m, m2m2, m2s1, m2m1;
		private int wave, waveshift;	// {-1, 0, 1, 2} (an index of the color component, use -1 for intensity)
		
		private int Cm, Cm1, D2m, D2m2, X2m1, Y2m1;
		
		private String error;
		
		private double messageLength, errorBound;
		
		
		public SamplePairHelper(int mi, int mj, int wave) {
			
			this.mi = mi;
			this.mj = mj;
			this.wave = wave;
			
			waveshift = wave * 8;
			
			error = null;
		}
		
		
		private void prepare(int m) {
			this.m = m;
			
			m1   =     m + 1;
			m2m  = 2 * m    ;
			m2m1 = 2 * m + 1;
			m2s1 = 2 * m - 1;
			m2m2 = 2 * m + 2;
			
			if (m == 0) m2s1 = m2m1;
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
		
		
		public double getErrorBound() {
			return errorBound;
		}
		

		public void run() {
			
			// Initialize
			
			error = null;
			
			Cm = Cm1 = D2m = D2m2 = X2m1 = Y2m1 = 0;
			
			for (int i = mi; i <= mj; i++) {
				
				if (progress != null) {
					progress.setValue(progress.getValue() + 1);
				}
				
				prepare(i);
				
				// Categorize horizontal pairs
				
				for (int y = 0; y < height; y++) {
					for (int x = 1; x < width; x++) {
						categorize(x - 1, y, x, y);
					}
				}
				
				// Categorize vertical pairs
				
				for (int y = 1; y < height; y++) {
					for (int x = 0; x < width; x++) {
						categorize(x, y - 1, x, y);
					}
				}
				
				if ((m == 0) ? (2 * Cm <= Cm1) : (Cm <= Cm1)) return;
				
			}
			
			// Solve the quadratic formula
			
			double a = (Cm - Cm1) / 4.0;
			double b = - ((D2m - D2m2) / 2.0 + Y2m1 - X2m1);
			double c = Y2m1 - X2m1;
			
			if (m == 0) {
				a = (2 * Cm - Cm1) / 4.0;
				b = - (D2m - D2m2 / 2.0 + Y2m1 - X2m1);
			}
			
			double D = b * b - 4 * a * c;
			
			if (D < 0) { error = "The image cannot be analyzed"; return; }
			
			double r1 = (-b + Math.sqrt(D)) / (2 * a);
			double r2 = (-b - Math.sqrt(D)) / (2 * a);
			
			double r = Math.min(r1, r2);
			
			if (r < 0) { error = "The image cannot be analyzed"; return; }
			
			// Determine the error bound
			
			double e = Math.abs(X2m1 - Y2m1) / (double)Math.abs(X2m1 + Y2m1);
			
			// Save the results
			
			messageLength = r;
			errorBound = e;
		}
		
		
		public void categorize(int x1, int y1, int x2, int y2) {
			
			// Fetch the RGB values
			
			int c1 = rgb[width * y1 + x1];
			int c2 = rgb[width * y2 + x2];
			
			// Choose the desired wave
			
			if (wave < 0) {
				c1 = ((c1) & 0xff + (c1 >> 8) & 0xff + (c1 >> 16) & 0xff) / 3;
				c2 = ((c2) & 0xff + (c2 >> 8) & 0xff + (c2 >> 16) & 0xff) / 3;
			}
			else if (waveshift != 0) {
				c1 = (c1 >> waveshift) & 0xff;
				c2 = (c2 >> waveshift) & 0xff;
			}
			else {
				c1 &= 0xff;
				c2 &= 0xff;
			}
			
			// Find the differences
			
			int d = c1 - c2;
			if (d < 0) d = -d;
			
			int cd = (c1 >> lsbBits) - (c2 >> lsbBits);
			if (cd < 0) cd = -cd;
			
			// Categorize
			
			if ( d == m2m ) D2m++;
			if ( d == m2m2) D2m2++;
			if (cd == m   ) Cm++;
			if (cd == m1  ) Cm1++;
			
			if ((d == m2m1) && (cd == m1)) X2m1++;
			if ((d == m2m1) && (cd == m )) Y2m1++;
		}
	}
	
}
