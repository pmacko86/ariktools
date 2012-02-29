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
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.aific.ariktools.util.Utils;

public class ColorAnalysis {
	
	public static final int MAX_COLOR_CUBE_DELTA = 11;
	
	public static boolean useLargeArrays = true;
	
	private ImageIcon cover;
	
	private int[] rgb;
	private int[] rgb_sorted;
	private boolean[][][] colorSpace;
	
	private int width;
	private int height;
	
	private int colors;
	private int countMSB[];
	
	private int leftCubes[][];
	private int rightCubes[][];
	private double chiSquareCubes[];
	
	private JProgressBar progress;
	private JLabel label;
	
	public ColorAnalysis(ImageIcon cover) {
		this.cover = cover;
		if (cover == null) throw new IllegalArgumentException("The image is not loaded");
		
		width = cover.getIconWidth();
		height = cover.getIconHeight();
		if ((width <= 0) || (height <= 0)) throw new IllegalArgumentException("The image is not properly loaded");
		
		progress = null;
		label = null;
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
	
	public void analyze() throws IOException {
		
		if (label != null) label.setText("Starting");
		if (progress != null) progress.setIndeterminate(true);
		
		rgb = grabRGB();
		rgb_sorted = sortRGB();
		
		if (label != null) label.setText("Counting colors");
		
		colors = countColors();
		
		countMSB = new int[8];
		for (int i = 0; i < 8; i++) countMSB[i] = countMSB(i + 1);

		if (label != null) label.setText("Counting cubes");
		
		countCubes();		

		if (label != null) label.setText("(idle)");
	}
	
	
	/*
	 * Retrieving results
	 */
	 
	public int getNumberOfColors() {
		return colors;
	}
	 
	public int getNumberOfMSBs(int bits) {
		return countMSB[bits - 1];
	}
	
	public int getLeftColorCubes(int delta, int complexity) {
		return leftCubes[(delta - 1) / 2][complexity - 1];
	}
	
	public int getRightColorCubes(int delta, int complexity) {
		return rightCubes[(delta - 1) / 2][complexity - 1];
	}
	
	public double getChiSquareCubes(int delta) {
		return chiSquareCubes[(delta - 1) / 2];
	}
	
	public String generateReport(String coverFileName) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>\n");
		sb.append("<head><title>Color Analysis of " + coverFileName + "</title></head>\n");
		sb.append("<body><center>\n");
		
		sb.append("<h1>Color Analysis of " + coverFileName + "</h1>\n");
		sb.append("<p>" + cover.getIconWidth() + " x " + cover.getIconHeight() + "</p>\n");
		sb.append("<p></p>\n");
		
		sb.append("<h2>Number of Colors</h2>\n");
		sb.append("<p>Unique colors: " + getNumberOfColors() + "</p>\n");
		sb.append("<p></p><table>\n");
		sb.append("<tr><td>Unique 1 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(1) + "</td></tr>\n");
		sb.append("<tr><td>Unique 2 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(2) + "</td></tr>\n");
		sb.append("<tr><td>Unique 3 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(3) + "</td></tr>\n");
		sb.append("<tr><td>Unique 4 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(4) + "</td></tr>\n");
		sb.append("<tr><td>Unique 5 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(5) + "</td></tr>\n");
		sb.append("<tr><td>Unique 6 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(6) + "</td></tr>\n");
		sb.append("<tr><td>Unique 7 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(7) + "</td></tr>\n");
		sb.append("<tr><td>Unique 8 bit MSBs:&nbsp;</td><td>" + getNumberOfMSBs(8) + "</td></tr>\n");
		sb.append("</table><p></p>\n");
		sb.append("<p>Average LSB values per 6 bit MSB: " + (Math.round(getNumberOfMSBs(8) / (double) getNumberOfMSBs(6) * 100) / 100.0) + "</p>\n");
		sb.append("<p></p>\n");
		
		sb.append("<h2>Lee\'s Color Cubes</h2>\n");
		sb.append("<p></p><table>\n");
		sb.append("<tr><td><b>Delta</b></td>\n");
		sb.append("<td><b>Chi-Square</b></td>\n");
		for (int i = 8; i >= 1; i--) {
			sb.append("<td align=\"center\"><b>" + i + "</b></td>\n");
		}
		sb.append("</tr>");
		for (int delta = 1; delta <= ColorAnalysis.MAX_COLOR_CUBE_DELTA; delta += 2) {
			sb.append("<tr><td align=\"center\">" + delta + "</td>");
			sb.append("<td align=\"center\">" + (Math.round(getChiSquareCubes(delta) * 10000) / 10000.0) + "</td>");
			for (int i = 8; i >= 1; i--) {
				sb.append("<td align=\"center\">" + getLeftColorCubes(delta, i));
				sb.append(" : " + getRightColorCubes(delta, i) + "</td>");
			}
			sb.append("</tr>\n");
		}
		sb.append("</table><p></p>\n");
		
		sb.append("</center></body></html>\n");
		
		return sb.toString();
	}
	
	public String generateLineReport() {
		StringBuffer sb = new StringBuffer();
		
		double sum = 0;
		int count = 0;
		
		sb.append("  <td align=\"center\">&nbsp;<b>" + (Math.round(getNumberOfMSBs(8) / (double) getNumberOfMSBs(6) * 100) / 100.0) + "</b>&nbsp;</td>\n");
		for (int delta = 1; delta <= ColorAnalysis.MAX_COLOR_CUBE_DELTA; delta += 2) {
			sb.append("  <td align=\"center\">&nbsp;" + (Math.round(getChiSquareCubes(delta) * 100) / 100.0) + "&nbsp;</td>\n");
			sum += getChiSquareCubes(delta);
			count++;
		}

		sb.append("  <td align=\"center\">&nbsp;<b>" + (Math.round((sum / count) * 100) / 100.0) + "</b>&nbsp;</td>\n");
		
		return sb.toString();
	}
	
	public static String generateLineReportHeader() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("  <td align=\"center\"><b>#&nbsp;LSB&nbsp;2</b></td>\n");
		
		for (int delta = 1; delta <= ColorAnalysis.MAX_COLOR_CUBE_DELTA; delta += 2) {
			sb.append("  <td align=\"center\"><b>" + delta + "</b></td>\n");
		}
		
		sb.append("  <td><b>Average</b></td>\n");
		
		return sb.toString();
	}
	
	public static int getLineReportColumns() {
		int count = 0;
		for (int delta = 1; delta <= ColorAnalysis.MAX_COLOR_CUBE_DELTA; delta += 2) count++;
		return count + 2;
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
	
	private int[] sortRGB() {
	
		if (progress != null) progress.setIndeterminate(true);
		
		System.gc();
		
		int[] rgb_sorted = new int[rgb.length];
		
		System.arraycopy(rgb, 0, rgb_sorted, 0, rgb.length);
		
		Arrays.sort(rgb_sorted);
		
		return rgb_sorted;
	}
	
	private int countColors() {
		
		if (progress != null) progress.setIndeterminate(true);

		System.gc();

		int count = 1;
		int last = rgb_sorted[0];
		
		for (int i = 1; i < rgb_sorted.length; i++) {
			if (last != rgb_sorted[i]) {
				last = rgb_sorted[i];
				count++;
			}
		}
		
		return count;
	}
	
	private int countMSB(int bits) {
		
		if (progress != null) progress.setIndeterminate(true);

		System.gc();

		// Build a mask

		int mask = 0;
		
		for (int i = 1; i <= bits; i++) {
			mask |= 1 << ( 8 - i);
			mask |= 1 << (16 - i);
			mask |= 1 << (24 - i);
		}
		
		// Count
		
		int n[] = new int[rgb.length];
		for (int i = 1; i < rgb.length; i++) {
			n[i] = rgb[i] & mask;
		}
		
		Arrays.sort(n);

		int count = 1;
		int last = n[0];
		
		for (int i = 1; i < n.length; i++) {
			if (last != n[i]) {
				last = n[i];
				count++;
			}
		}
		
		return count;
	}
	
	
	private void countCubes() {
		
		if (useLargeArrays) {
			colorSpace = new boolean[256][256][256];
			
			for (int r = 0; r < 256; r++) {
				for (int g = 0; g < 256; g++) {
					for (int b = 0; b < 256; b++) {
						colorSpace[r][g][b] = false;
					}
				}
			}
			
			for (int i = 0; i < rgb.length; i++) {
				int pixel = rgb[i];
				int red   = (pixel >> 16) & 0xff;
				int green = (pixel >>  8) & 0xff;
				int blue  = (pixel      ) & 0xff;
				colorSpace[red][green][blue] = true;
			}
		}
		
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setMaximum((MAX_COLOR_CUBE_DELTA - 1) * 64 + 128);
			progress.setValue(0);
		}
		leftCubes  = new int[(MAX_COLOR_CUBE_DELTA - 1) / 2 + 1][8];
		rightCubes = new int[(MAX_COLOR_CUBE_DELTA - 1) / 2 + 1][8];
		chiSquareCubes = new double[(MAX_COLOR_CUBE_DELTA - 1) / 2 + 1];

		CubeThread[] threads = new CubeThread[(MAX_COLOR_CUBE_DELTA - 1) / 2 + 1];
		
		for (int i = 0, c = 1; c <= MAX_COLOR_CUBE_DELTA; i++, c += 2) {
			threads[i] = new CubeThread(c);
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	private class CubeThread extends Thread {
	
		private int cube;
		
		public CubeThread(int cube) {
			this.cube = cube;
		}
		
		public void run() {
			countCubes(cube);
		}
		
	}
	
	private void countCubes(int delta) {
		
		int index = (delta - 1) / 2;
		
		for (int i = 0; i < 8; i++) {
			leftCubes[index][i] = 0;
			rightCubes[index][i] = 0;
		}
		
		int n, st;
		int max = 255 - delta;
		
		if (colorSpace == null) {
				
			for (int r = 0; r < 256; r += 2) {
				if (progress != null) progress.setValue(progress.getValue() + 1);
				for (int g = 0; g < 256; g += 2) {
					for (int b = 0; b < 256; b += 2) {
		
						st = (Arrays.binarySearch(rgb_sorted, r | (g << 8) | (b << 16)) >= 0) ? 1 : 0;
						
						if ((r >= delta) && (g >= delta) & (b >= delta)) {
							n = st;
							if (Arrays.binarySearch(rgb_sorted, (r - delta) | ((g        ) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r - delta) | ((g - delta) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r - delta) | ((g        ) << 8) | ((b - delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r - delta) | ((g - delta) << 8) | ((b - delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g - delta) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g        ) << 8) | ((b - delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g - delta) << 8) | ((b - delta) << 16)) >= 0) n++;
							if (n >= 1) leftCubes[index][n - 1]++;
						}
						
						if ((r <= max) && (g <= max) & (b <= max)) {
							n = st;
							if (Arrays.binarySearch(rgb_sorted, (r + delta) | ((g        ) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r + delta) | ((g + delta) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r + delta) | ((g        ) << 8) | ((b + delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r + delta) | ((g + delta) << 8) | ((b + delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g + delta) << 8) | ((b        ) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g        ) << 8) | ((b + delta) << 16)) >= 0) n++;
							if (Arrays.binarySearch(rgb_sorted, (r        ) | ((g + delta) << 8) | ((b + delta) << 16)) >= 0) n++;
							if (n >= 1) rightCubes[index][n - 1]++;
						}
					}
				}
			}
			
		}
		else {
			for (int r = 0; r < 256; r += 2) {
				if (progress != null) progress.setValue(progress.getValue() + 1);
				for (int g = 0; g < 256; g += 2) {
					for (int b = 0; b < 256; b += 2) {
						
						st = colorSpace[r][g][b] ? 1 : 0;
						
						if ((r >= delta) && (g >= delta) & (b >= delta)) {
							n = st;
							if (colorSpace[r - delta][g        ][b        ]) n++;
							if (colorSpace[r - delta][g - delta][b        ]) n++;
							if (colorSpace[r - delta][g        ][b - delta]) n++;
							if (colorSpace[r - delta][g - delta][b - delta]) n++;
							if (colorSpace[r        ][g - delta][b        ]) n++;
							if (colorSpace[r        ][g        ][b - delta]) n++;
							if (colorSpace[r        ][g - delta][b - delta]) n++;
							if (n >= 1) leftCubes[index][n - 1]++;
						}
						
						if ((r <= max) && (g <= max) & (b <= max)) {
							n = st;
							if (colorSpace[r + delta][g        ][b        ]) n++;
							if (colorSpace[r + delta][g + delta][b        ]) n++;
							if (colorSpace[r + delta][g        ][b + delta]) n++;
							if (colorSpace[r + delta][g + delta][b + delta]) n++;
							if (colorSpace[r        ][g + delta][b        ]) n++;
							if (colorSpace[r        ][g        ][b + delta]) n++;
							if (colorSpace[r        ][g + delta][b + delta]) n++;
							if (n >= 1) rightCubes[index][n - 1]++;
						}
					}
				}
			}
		}
		
		// Calculate chi-square statistics
		
		double chiSquare = 0;
		
		for (int i = 0; i < 8; i++) {
			double Yn = (leftCubes[index][i] + rightCubes[index][i]) / 2.0;
			if (Yn > 0.0000001) chiSquare += Utils.sqr(leftCubes[index][i] - Yn) / Yn;
		}
		
		chiSquareCubes[index] = chiSquare;
	}
}
