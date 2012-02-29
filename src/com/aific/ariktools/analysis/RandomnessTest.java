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


import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

import com.aific.ariktools.stream.BitStream;
import com.aific.ariktools.stream.IconBitStream;
import com.aific.ariktools.stream.RandomBitStream;
import com.aific.ariktools.util.Utils;

public class RandomnessTest {
	
	public static boolean approximate = true;
	public static int threads = 4;
	
	private BitStream stream;
	
	private float onesFrequency;
	
	protected float frequencyTest;
	protected float runsTest;
	
	public RandomnessTest(BitStream stream) {
		this.stream = stream;
	}
	
	/*
	 * Image randomness tests
	 *
	 * Return values:
	 *   float, range 0 - 1
	 *   closer to 0 means more random
	 */
	
	public static float[][] test(ImageIcon icon) {
		return test(icon, null);
	}
	
	public static float[][] test(ImageIcon icon, JProgressBar progress) {
		
		return test(icon, progress, new IconBitStream(icon));
	}
		
	public static float[][] test(ImageIcon icon, JProgressBar progress, IconBitStream stream) {
		
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setMinimum(0);
			progress.setMaximum(height);
			progress.setValue(0);
		}
		
		float[][] scores = new float[width][height];
		
		if (approximate) {
			int rangeWidth = stream.getRangeWidth();
			int rangeHeight = stream.getRangeHeight();
			
			for (int y = rangeHeight / 2; y < height; y += rangeHeight) {
				if (progress != null) progress.setValue(y);
				
				for (int x = rangeWidth / 2; x < width; x += rangeWidth) {
					
					stream.preparePixel(x, y);
					float score = test(stream);
					
					for (int i = x - rangeWidth / 2; i < x - rangeWidth / 2 + rangeWidth; i++) {
						if (i >= width) continue;
						for (int j = y - rangeHeight / 2; j < y - rangeHeight / 2 + rangeHeight; j++) {
							if (j >= height) continue;
							scores[i][j] = score;
						}
					}
				}
			}
		}
		else {
			
			if (threads <= 1) {
				for (int y = 0; y < height; y++) {
					if (progress != null) progress.setValue(y);

					for (int x = 0; x < width; x++) {
						stream.preparePixel(x, y);
						scores[x][y] = test(stream);
					}
				}
			}
			else {
				
				int step = height / threads;
				if (height % threads != 0) step++;
				if (step <= 1) step++;
				
				RandomnessImageIconThread[] helperThreads = new RandomnessImageIconThread[threads];
				
				int y = 0;
				
				for (int i = 0; i < threads; i++, y += step) {
					
					if (y >= height) {
						helperThreads[i] = null;
						continue;
					}
					
					helperThreads[i] = new RandomnessImageIconThread(icon, y, y + step, progress, scores);
					helperThreads[i].start();
				}
				
				for (int i = 0; i < threads; i++) {
					try {
						if (helperThreads[i] != null) helperThreads[i].join();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		if (progress != null) {
			progress.setValue(height);
		}
		
		return scores;
	}
	
	
	private static class RandomnessImageIconThread extends Thread {
		
		private int width, from, to;
		private JProgressBar progress;
		private IconBitStream stream;
		private float[][] scores;
		
		public RandomnessImageIconThread(ImageIcon icon, int from, int to, JProgressBar progress, float[][] scores) {
			this.from = Math.max(0, from);
			this.to   = Math.min(to, icon.getIconHeight());
			width = icon.getIconWidth();
			stream = new IconBitStream(icon);
			this.scores = scores;
			this.progress = progress;
		}
		
		public void run() {
			for (int y = from; y < to; y++) {
				if (progress != null) progress.setValue(progress.getValue() + 1);

				for (int x = 0; x < width; x++) {
					stream.preparePixel(x, y);
					scores[x][y] = test(stream);
				}
			}
		}
	}
	
	
	public static BufferedImage plotScores(float[][] scores) {
		
		BufferedImage bi = new BufferedImage(scores.length, scores[0].length, BufferedImage.TYPE_INT_RGB);
		
		for (int y = 0; y < scores[0].length; y++) {
			for (int x = 0; x < scores.length; x++) {
				int score = 255 - Math.round(scores[x][y] * 255);
				if (score < 0) score = 0;
				if (score > 255) score = 255;
				bi.setRGB(x, y, (Math.round(0.75f * score) << 16) + (Math.round(0.75f * score) << 8) + score);
			}
		}
		
		return bi;
	}
	
	
	/*
	 * Bit stream tests
	 *
	 * Return values:
	 *   float, range 0 - 1
	 *   closer to 0 means more random
	 */
	
	public static float test(BitStream stream) {
		return (new RandomnessTest(stream)).test();
	}
	
	public float test() {
		frequencyTest = frequencyTest();
		runsTest = runsTest();
		return 0.65f * frequencyTest + 0.35f * runsTest;
	}

	
	/*
	 * Individual tests
	 */
	
	
	protected float frequencyTest() {
		int ones = 0;
		stream.reset();
		while (stream.hasNext()) if (stream.next() == 1) ones++;
		onesFrequency = ones / (float)stream.getLimit();
		return 2 * Math.abs(0.5f - onesFrequency);
	}
	
	
	protected float runsTest() {
		stream.reset();
		int n = stream.getLimit();
		if (n < 2) return 0;
		
		byte last = stream.next();
		int V = 1;
		
		while (stream.hasNext()) {
			byte b = stream.next();
			if (b != last) V++;
			last = b;
		}
		
		double d = onesFrequency * (1 - onesFrequency);
		double Pv = Utils.erfc(Math.abs(V - 2 * n * d) / (2 * Math.sqrt(2.0 * n) * d));
		
		return Pv < 0.01 ? 1.0f : 0.0f;
	}
	
	
	/*
	 * Testing
	 */
	
	public static void main(String[] a) {
		RandomBitStream rbs = new RandomBitStream(new java.util.Random(), 100);
		rbs.setLimit(100);
		RandomnessTest r = new RandomnessTest(rbs);
		float result = r.test();
		System.out.println("Frequency Test: " + r.frequencyTest);
		System.out.println("Runs Test: " + r.runsTest);
		System.out.println("Result: " + result);
	}
	
}
