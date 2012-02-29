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

public class ConvolutionFilter extends ImageFilter {
	
	protected double[][] kernel;
	protected double kernelSum;
	
	protected int kernelWidth;
	protected int kernelHeight;
	
	
	/**
	 * Create an instance of the filter
	 *
	 * @param kernel the convolution kernel 
	 */
	public ConvolutionFilter(double[][] kernel) {
		
		this.kernel = new double[kernel.length][kernel[0].length];
		
		kernelWidth = kernel.length;
		kernelHeight = kernel[0].length;
		kernelSum = 0;
		
		if ((kernelWidth < 1) || (kernelHeight < 1)) throw new RuntimeException("The kernel is too small");
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[0].length; y++) {
				this.kernel[x][y] = kernel[x][y];
				kernelSum += this.kernel[x][y];
			}
		}
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
	protected int[][] filter(int[][] rgb, int width, int height, boolean grayscale, JProgressBar progress) {
		
		int newWidth  = width  - kernelWidth  + 1;
		int newHeight = height - kernelHeight + 1;
		
		if ((newWidth < 1) || (newHeight < 1)) throw new RuntimeException("The image is too small");
		
		int[][] out = new int[newWidth][newHeight];
		
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setMaximum(newWidth);
			progress.setValue(0);
		}
		
		for (int x = 0; x < newWidth; x++) {
			
			if (progress != null) progress.setValue(x);

			for (int y = 0; y < newHeight; y++) {
				
				double R = 0, G = 0, B = 0;
				
				for (int ax = 0; ax < kernelWidth; ax++) {
					for (int ay = 0; ay < kernelHeight; ay++) {
						
						int c = rgb[x + ax][y + ay];
						int r = (c >> 16) & 0xff;
						int g = (c >>  8) & 0xff;
						int b = (c      ) & 0xff;
						double k = kernel[ax][ay];
						
						R += k * r;
						G += k * g;
						B += k * b;
					}
				}
				
				int r = Math.round(Math.max(Math.min(Math.round(R), 255), 0));
				int g = Math.round(Math.max(Math.min(Math.round(G), 255), 0));
				int b = Math.round(Math.max(Math.min(Math.round(B), 255), 0));
				
				out[x][y] = (r << 16) | (g << 8) | b;
			}
		}
		
		return out;
	}
	
	
	/**
	 * Crop an image to a size that it would get if it had the filter appied to it
	 *
	 * @param icon the image icon
	 * @return cropped buffered image
	 */
	public BufferedImage cropByKernel(ImageIcon icon) {

		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		
		int newWidth  = width  - kernelWidth  + 1;
		int newHeight = height - kernelHeight + 1;
		
		if ((newWidth < 1) || (newHeight < 1)) throw new RuntimeException("The image is too small");
		
		int[] rgb = Utils.grabRGB(icon);
		int[] out = new int[newHeight * newWidth];
		
		int dx = kernelWidth / 2;
		int dy = kernelHeight / 2;
		
		for (int y = 0; y < newHeight; y++) {
			int b_out = newWidth * y;
			int b_rgb = width * (y + dy);
			for (int x = 0; x < newWidth; x++) {
				out[b_out + x] = rgb[b_rgb + x + dx];
			}
		}
		
		return Utils.plotRGB(out, newWidth, newHeight);
	}
	
	
	/**
	 * Normalize the kernel linearly in place
	 *
	 * @param kernel the kernel to normalize
	 */
	public static void normalizeKernel(double[][] kernel) {
		
		int kernelWidth = kernel.length;
		int kernelHeight = kernel[0].length;
		double kernelSum = 0;
		
		if ((kernelWidth < 1) || (kernelHeight < 1)) throw new RuntimeException("The kernel is too small");
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[0].length; y++) {
				kernelSum += kernel[x][y];
			}
		}
		
		double k = 1.0 / kernelSum;
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[0].length; y++) {
				kernel[x][y] *= k;
			}
		}
	}
	
	
	/**
	 * Normalize the kernel quadratically in place
	 *
	 * @param kernel the kernel to normalize
	 */
	public static void normalizeKernelQuad(double[][] kernel) {
		
		int kernelWidth = kernel.length;
		int kernelHeight = kernel[0].length;
		double kernelSum = 0;
		
		if ((kernelWidth < 1) || (kernelHeight < 1)) throw new RuntimeException("The kernel is too small");
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[0].length; y++) {
				kernelSum += Utils.sqr(kernel[x][y]);
			}
		}
		
		double k = 1.0 / Math.sqrt(kernelSum);
		
		for (int x = 0; x < kernel.length; x++) {
			for (int y = 0; y < kernel[0].length; y++) {
				kernel[x][y] *= k;
			}
		}
	}
	
	
	/**
	 * Create a kernel for Gaussian Smoothing
	 *
	 * @param sigma the standard deviation
	 * @return the kernel
	 */
	public static double[][] createKernel_GaussianSmoothing(double sigma) {
		
		if (sigma <= 0) throw new RuntimeException("The standard deviation has to be strictly positive");
		int d = (int) Math.round(sigma * 3);
		
		double[][] kernel = new double[2 * d + 1][2 * d + 1];
		double c = 1.0 / (2 * Math.PI * Utils.sqr(sigma));
		
		for (int x = -d; x <= d; x++) {
			for (int y = -d; y <= d; y++) {
				kernel[d + x][d + y] = c * Math.exp(- (Utils.sqr(x) + Utils.sqr(y)) / (2 * Utils.sqr(sigma)));
			}
		}
		
		normalizeKernel(kernel);
		
		return kernel;
	}
	
	
	/**
	 * Apply Gaussian Smoothing
	 *
	 * @param icon the image icon
	 * @param sigma the standard deviation
	 * @return the smoothed image
	 */
	public static BufferedImage smoothe(ImageIcon icon, double sigma) {
		return (new ConvolutionFilter(createKernel_GaussianSmoothing(sigma))).apply(icon);
	}
}
