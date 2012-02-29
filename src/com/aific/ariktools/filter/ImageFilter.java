package com.aific.ariktools.filter;

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

import com.aific.ariktools.util.Utils;

public abstract class ImageFilter {

	
	/**
	 * Create an instance of the filter
	 */
	protected ImageFilter() {
	}
	
	
	/**
	 * Apply a filter to an image icon
	 *
	 * @param icon the image icon
	 * @param progress the progress bar to use (use null to disable)
	 * @return a buffered image with the filter applied
	 */
	public BufferedImage apply(ImageIcon icon, JProgressBar progress) {
		
		int[] newSize = { 0, 0 };
		int[] org = Utils.grabRGB(icon);
		int[] rgb = apply(org, icon.getIconWidth(), icon.getIconHeight(), progress, newSize);
		
		if (rgb == null) throw new RuntimeException("The image filter failed");
		
		return Utils.plotRGB(rgb, newSize[0], newSize[1]);
	}
	
	
	/**
	 * Apply a filter to an image icon
	 *
	 * @param icon the image icon
	 * @return a buffered image with the filter applied
	 */
	public BufferedImage apply(ImageIcon icon) {
		return apply(icon, null);
	}
	
	
	/**
	 * Apply a filter to an image icon, preserving the original image size
	 *
	 * @param rgb the RGB array of the original image
	 * @param width the width of the image
	 * @param height the height of the image
	 * @param progress the progress bar to use (use null to disable)
	 * @return the RGB values of the image
	 */
	public int[] applyRGB(int[] rgb, int width, int height, JProgressBar progress) {
		int[] r = apply(rgb, width, height, progress, null);
		if (r == null) throw new RuntimeException("The image filter failed");
		return r;
	}
	
	
	/**
	 * Apply a filter to an image icon, preserving the original image size
	 *
	 * @param icon the image icon
	 * @param progress the progress bar to use (use null to disable)
	 * @return the RGB values of the image
	 */
	public int[] applyRGB(ImageIcon icon, JProgressBar progress) {
		return applyRGB(Utils.grabRGB(icon), icon.getIconWidth(), icon.getIconHeight(), progress);
	}
	
	
	/**
	 * Apply a filter to an RGB array
	 *
	 * @param rgb the RGB array
	 * @param width the image width
	 * @param height the image height
	 * @param progress the progress bar to use (use null to disable)
	 * @param newSize the at least 2 element array to write the new size of the image (use null to force preserving the size)
	 * @return a new RGB array (does not need to have the same size as the original), or null if error occured
	 */
	protected int[] apply(int[] rgb, int width, int height, JProgressBar progress, int[] newSize) {
		
		if (rgb == null) throw new NullPointerException();
		
		int[][] p = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				p[x][y] = rgb[width * y + x];
			}
		}
		
		int[][] r = filter(p, width, height, Utils.isGrayscale(rgb), progress);

		int n[] = null;
		int fwidth  = r.length;
		int fheight = r[0].length;
		
		if (newSize == null) {
			n = new int[width * height];
			int offsetX = (width  - fwidth ) / 2;
			int offsetY = (height - fheight) / 2;
			
			for (int y = 0; y < fheight; y++) {
				for (int x = 0; x < fwidth; x++) n[width * (offsetY + y) + offsetX + x] = r[x][y];
				for (int x = 0; x < offsetX; x++) n[width * (offsetY + y) + x] = n[width * (offsetY + y) + offsetX];
				for (int x = fwidth; x < width; x++) n[width * (offsetY + y) + x] = n[width * (offsetY + y) + offsetX + fwidth - 1];
			}
			for (int y = 0; y < offsetY; y++) for (int x = 0; x < width; x++) n[width * y + x] = n[width * offsetY + x];
			for (int y = fheight; y < height; y++) for (int x = 0; x < width; x++) n[width * y + x] = n[width * (offsetY + fheight - 1) + x];
		}
		else {
			n = new int[fwidth * fheight];
			
			for (int x = 0; x < fwidth; x++) {
				for (int y = 0; y < fheight; y++) {
					n[fwidth * y + x] = r[x][y];
				}
			}
			
			newSize[0] = fwidth;
			newSize[1] = fheight;
		}
		
		return n;
	}
	
	
	/**
	 * Apply a filter to an RGB array
	 * 
	 * @param rgb the 2-D RGB array (first index = x, second index = y)
	 * @param width the image width
	 * @param height the image height
	 * @param grayscale whether the image is grayscale
	 * @param progress the progress bar to use (use null to disable)
	 * @return a new RGB array (does not need to have the same size as the original), or null if error occured
	 */
	protected abstract int[][] filter(int[][] rgb, int width, int height, boolean grayscale, JProgressBar progress);
	
}
