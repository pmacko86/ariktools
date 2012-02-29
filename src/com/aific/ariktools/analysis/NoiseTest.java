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

import com.aific.ariktools.filter.ConvolutionFilter;
import com.aific.ariktools.util.Utils;


/**
 * Test for Gaussian noise
 *
 * @author Peter Macko
 */
public class NoiseTest {
	
	public static final double GAUSSIAN_SIGMA = 0.5;
	
	private double[][] filterKernel;
	private ConvolutionFilter filter;
	
	
	/**
	 * Create an instance of the class NoiseTest
	 */
	public NoiseTest()
	{
		filterKernel = ConvolutionFilter.createKernel_GaussianSmoothing(GAUSSIAN_SIGMA);
		filter = new ConvolutionFilter(filterKernel);
	}
	
	
	/**
	 * Perform the noise test
	 * 
	 * @param icon the image icon
	 * @return the scores
	 */
	public float[][] test(ImageIcon icon)
	{
		return test(icon, null);
	}
		
	
	/**
	 * Perform the noise test
	 * 
	 * @param icon the image icon
	 * @param progress the progress bar to use
	 * @return the scores
	 */
	public float[][] test(ImageIcon icon, JProgressBar progress)
	{
		BufferedImage filteredImage = filter.apply(icon, progress);
		ImageIcon filteredImageIcon = new ImageIcon(filteredImage);
		ImageIcon croppedImageIcon = new ImageIcon(filter.cropByKernel(icon));
		
		int width  = icon.getIconWidth();
		int height = icon.getIconHeight();
		
		int fwidth  = filteredImageIcon.getIconWidth();
		int fheight = filteredImageIcon.getIconHeight();
		int offset  = (icon.getIconWidth() - filteredImageIcon.getIconWidth()) / 2;
		
		int[] rgb1 = Utils.grabRGB(filteredImageIcon);
		int[] rgb2 = Utils.grabRGB(croppedImageIcon);
		float[][] r = new float[width][height];
		
		int i = 0;
		if (progress != null) progress.setIndeterminate(true);
		
		for (int y = 0; y < fheight; y++) {
			
			for (int x = 0; x < fwidth; x++) {
				
				int c1 = rgb1[i];
				int r1 = (c1 >> 16) & 0xff;
				int g1 = (c1 >>  8) & 0xff;
				int b1 = (c1      ) & 0xff;
				
				int c2 = rgb2[i];
				int r2 = (c2 >> 16) & 0xff;
				int g2 = (c2 >>  8) & 0xff;
				int b2 = (c2      ) & 0xff;
				
				i++;

				r[offset + x][offset + y] = (float) Math.sqrt(Utils.sqr(r1 - r2) + Utils.sqr(g1 - g2) + Utils.sqr(b1 - b2));
			}
			
			for (int x = 0; x < offset; x++) r[x][offset + y] = r[offset][offset + y];
			for (int x = fwidth; x < width; x++) r[x][offset + y] = r[offset + fwidth - 1][offset + y];
		}
		
		for (int y = 0; y < offset; y++) for (int x = 0; x < width; x++) r[x][y] = r[x][offset];
		for (int y = fheight; y < height; y++) for (int x = 0; x < width; x++) r[x][y] = r[x][offset + fheight - 1];
		
		return r;
	}
	
	
	/**
	 * Plot the scores from a noise test
	 *
	 * @param scores the array of scores
	 * @return the buffered image of plotted scores
	 */
	public static BufferedImage plotScores(float[][] scores) {
		
		BufferedImage bi = new BufferedImage(scores.length, scores[0].length, BufferedImage.TYPE_INT_RGB);
		
		for (int y = 0; y < scores[0].length; y++) {
			for (int x = 0; x < scores.length; x++) {
				int score = Math.round(scores[x][y] / 8 * 255);
				if (score < 0) score = 0;
				if (score > 255) score = 255;
				bi.setRGB(x, y, (Math.round(0.75f * score) << 16) + (Math.round(0.75f * score) << 0) + (score << 8));
			}
		}
		
		return bi;
	}
	
}
