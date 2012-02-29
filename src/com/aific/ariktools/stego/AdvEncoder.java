package com.aific.ariktools.stego;

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
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.aific.ariktools.analysis.NoiseTest;
import com.aific.ariktools.analysis.RandomnessTest;
import com.aific.ariktools.filter.ConvolutionFilter;
import com.aific.ariktools.stream.AppendBitStream;
import com.aific.ariktools.stream.BitStream;
import com.aific.ariktools.stream.DataBitStream;
import com.aific.ariktools.stream.FileBitStream;
import com.aific.ariktools.stream.IconBitStream;
import com.aific.ariktools.stream.RandomBitStream;
import com.aific.ariktools.stream.XORBitStream;
import com.aific.ariktools.util.Utils;

public class AdvEncoder {
	
	public static final int defaultTileWidth = 9;
	public static final int defaultTileHeight = 9;
	public static final float defaultGap = 2;
	public static final float defaultRandomnessThreshold = 0.005f;
	public static final float defaultGrayscaleWidthFactor = 2;
	public static final float defaultGrayscaleHeightFactor = 2;
	public static final float defaultLSB1WidthFactor = 2;
	public static final float defaultLSB1HeightFactor = 2;
	public static final float defaultNoiseScale = 0.05f;
	
	public static final float FLOAT_TO_INT = 100000.0f;
	
	public static boolean useGaussianNorm = true;
	
	private static int lsbBits = 2;
	
	private int lsbBitsCMax;
	private int lsbBitsMask;
	private int lsbBitsNorm;
	
	private ImageIcon cover;
	private File file;
	
	private int[] rgb;

	private int coverWidth;
	private int coverHeight;
	private int tilesWidth;
	private int tilesHeight;

	private int fileSize;
	private int fileTiles;
	
	private int tileWidth;
	private int tileHeight;
	private int tileBits;
	private int tileBytes;
	private int tileCapacity;
	private int coverCapacity;
	private int coverTiles;
	
	private boolean grayscale;
	private int dataPlanes;
	
	private JProgressBar progress;
	private JLabel label;
	
	private Random seedRandom;
	private Random paddingRandom;
	private long randomSeed;
	private byte[] randomSeedBytes;
	
	private float[][] randomness;
	private float[] randomnessFlat;
	private int[] gaussianBase;
	
	private BitStream[] fileStreams;
	private BitStream[] streams;
	
	private float[] randomnessStreams;
	private float leastRandom;
	private int leastRandomInt;
	
	private float dataPlacement;
	private boolean[][] dataPlaced;
	private float randomnessThreshold;
	
	public AdvEncoder(ImageIcon cover, File file) {
		this.cover = cover;
		this.file = file;
		
		if (file == null) throw new IllegalArgumentException("Secret file is not loaded");
		if (cover == null) throw new IllegalArgumentException("Cover image is not loaded");
		if (!file.exists()) throw new IllegalArgumentException("The secret file does not exist");
		if (file.length() >= 16 * 1048576) throw new IllegalArgumentException("The secret file is way too big");
		
		grayscale = Utils.isGrayscale(cover);
		dataPlanes = grayscale ? 1 : 3;
		
		tileWidth = defaultTileWidth;
		tileHeight = defaultTileHeight;
		
		if (grayscale) {
			tileWidth *= defaultGrayscaleWidthFactor;
			tileHeight *= defaultGrayscaleHeightFactor;
		}
		
		if (lsbBits == 1) {
			tileWidth *= defaultLSB1WidthFactor;
			tileHeight *= defaultLSB1HeightFactor;
		}
		
		lsbBitsCMax = 1;
		for (int i = 0; i < lsbBits; i++) lsbBitsCMax *= 2;
		
		lsbBitsMask = lsbBitsCMax - 1;
		lsbBitsNorm = lsbBitsCMax / 2;
		
		coverWidth = cover.getIconWidth();
		coverHeight = cover.getIconHeight();
		tilesWidth = coverWidth / tileWidth;
		tilesHeight = coverHeight / tileHeight;
		if ((tilesWidth <= 0) || (tilesHeight <= 0)) throw new IllegalArgumentException("The cover image is too small");

		tileBytes = dataPlanes * tileWidth * tileHeight * lsbBits / 8;
		tileCapacity = tileBytes - 3 - 16;
		tileBits = lsbBits * dataPlanes * tileWidth * tileHeight;
		fileSize = (int)file.length();
		fileTiles = fileSize / tileCapacity + (fileSize % tileCapacity == 0 ? 0 : 1);
		
		coverTiles = tilesWidth * tilesHeight;
		coverCapacity = (coverTiles - 1) * tileCapacity;
		if (coverCapacity >= 16 * 1048576) coverCapacity = 16 * 1048576 - 1;
		if ((fileSize > coverCapacity) || (fileTiles > coverTiles - 1)) throw new IllegalArgumentException("The secret file is too big");
		
		paddingRandom = new Random();
		randomnessThreshold = defaultRandomnessThreshold;
		
		progress = null;
		label = null;
	}
	
	
	public void setProgressBar(JProgressBar progress) {
		this.progress = progress;
	}
	
	public void setProgressLabel(JLabel label) {
		this.label = label;
	}
	
	
	public static int getLSBBits() {
		return lsbBits;
	}
		
	public static void setLSBBits(int bits) {
		if ((bits <= 0) || (bits > 8)) return;
		lsbBits = bits;
	}
	
	
	/*
	 * Accessors for statistics
	 */
	
	public float getDataPlacement() {
		return dataPlacement;
	}
	
	public float getLeastRandom() {
		return leastRandom;
	}
	
	public BufferedImage plotDataPlacement() {
		BufferedImage newImage = Decoder.decode(cover);
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				if (!dataPlaced[x][y]) continue;
				
				for (int yi = 0; yi < tileHeight; yi++) {
					for (int xi = 0; xi < tileWidth; xi++) {
						newImage.setRGB(x * tileWidth + xi, y * tileHeight + yi, 0x8888ff);
					}
				}
			}
		}
		
		return newImage;
	}
	
	
	/*
	 * Encoder
	 */
	
	public BufferedImage encode() throws IOException {

		if (label != null) label.setText("Starting");
		if (progress != null) progress.setIndeterminate(true);
			
		//BufferedImage newImage = new BufferedImage(coverWidth, coverHeight, grayscale ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB);
		BufferedImage newImage = new BufferedImage(coverWidth, coverHeight, BufferedImage.TYPE_INT_RGB);
		
		randomSeedBytes = new byte[3];
		(new Random()).nextBytes(randomSeedBytes);
		randomSeed = (Utils.byte2int(randomSeedBytes[2]) << 16) | (Utils.byte2int(randomSeedBytes[1]) << 8) | (Utils.byte2int(randomSeedBytes[0]));
		try {
			seedRandom = SecureRandom.getInstance("SHA1PRNG");
			seedRandom.setSeed(randomSeed);
		}
		catch (Exception e) {
			System.out.println("WARNING: Could not create SHA1PRNG secure random renerator");
			seedRandom = new Random(randomSeed);
		}
		
		if (label != null) label.setText("Analyzing cover");
		
		randomness = testRandomness();
		
		if (progress != null) progress.setIndeterminate(true);
		randomnessFlat = Utils.collapse(randomness);
		Arrays.sort(randomnessFlat);

		rgb = grabRGB();
		
		if (useGaussianNorm) {
			ConvolutionFilter filter = new ConvolutionFilter(ConvolutionFilter.createKernel_GaussianSmoothing(0.5));
			gaussianBase = filter.applyRGB(rgb, coverWidth, coverHeight, progress);
		}
		else {
			gaussianBase = null;
		}
		
		if (label != null) label.setText("Preparing streams");
		
		fileStreams = prepareFileStreams();
		streams = generateStreams();

		leastRandom = Utils.maximum(randomnessStreams);
		leastRandomInt = Math.round(leastRandom * FLOAT_TO_INT) + 1;
		leastRandom = leastRandomInt / FLOAT_TO_INT;
		
		dataPlacement = determineDataPlacement();
		
		dataPlaced = new boolean[tilesWidth][tilesHeight];
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				dataPlaced[x][y] = false;
			}
		}
		
		if (label != null) label.setText("Encoding data");
		
		writeHeader();
		writeStreams();
		
		corruptForeignStreams();
		
		plotRGB(newImage);
		
		if (label != null) label.setText("(idle)");
		return newImage;
	}
	
	
	/*
	 * Some static functionality
	 */
	
	public static int coverCapacity(ImageIcon cover) {
		
		boolean grayscale = Utils.isGrayscale(cover);
		int dataPlanes = grayscale ? 1 : 3;
		
		int tileWidth = defaultTileWidth;
		int tileHeight = defaultTileHeight;
		
		if (grayscale) {
			tileWidth *= defaultGrayscaleWidthFactor;
			tileHeight *= defaultGrayscaleHeightFactor;
		}
		
		if (lsbBits == 1) {
			tileWidth *= defaultLSB1WidthFactor;
			tileHeight *= defaultLSB1HeightFactor;
		}
		
		int coverWidth = cover.getIconWidth();
		int coverHeight = cover.getIconHeight();
		int tilesWidth = coverWidth / tileWidth;
		int tilesHeight = coverHeight / tileHeight;
		
		int tileBytes = dataPlanes * tileWidth * tileHeight * lsbBits / 8;
		int tileCapacity = tileBytes - 3 - 16;
		
		int coverTiles = tilesWidth * tilesHeight;
		int coverCapacity = (coverTiles - 1) * tileCapacity;
		if (coverCapacity >= 16 * 1048576) coverCapacity = 16 * 1048576 - 1;
		
		return coverCapacity;
	}
	
	
	/*
	 * Common helper functions
	 */
	
	public float[][] testRandomness() {
		
		IconBitStream stream = new IconBitStream(cover);
		stream.setSquareMode(true);
		stream.setSquareRange(tileWidth, tileHeight);
		
		float[][] nres = (new NoiseTest()).test(cover, progress);
		for (int y = 0; y < coverHeight; y++) {
			for (int x = 0; x < coverWidth; x++) {
				nres[x][y] = (float) Math.max(8 - nres[x][y], 0);
			}
		}
		
		float[][] scores = new float[tilesWidth][tilesHeight];
		
		if (RandomnessTest.approximate) {
			
			if (progress != null) {
				progress.setIndeterminate(false);
				progress.setMinimum(0);
				progress.setMaximum(tilesHeight);
				progress.setValue(0);
			}
			
			for (int y = 0; y < tilesHeight; y++) {
				if (progress != null) progress.setValue(y);
					
				for (int x = 0; x < tilesWidth; x++) {
					stream.preparePixel(tileWidth / 2 + x * tileWidth, tileHeight / 2 + y * tileHeight);
					float r = RandomnessTest.test(stream);
				
					float s = 0;
					for (int i = 0; i < tileWidth; i++) {
						for (int j = 0; j < tileHeight; j++) {
							s += Utils.sqr(r + defaultNoiseScale * nres[x * tileWidth + i][y * tileHeight + j]);
						}
					}
					
					scores[x][y] = s;
				}
			}
			
		}
		else {
		
			if (grayscale) {
				stream.setSquareRange(defaultTileWidth, defaultTileHeight);
			}
			
			float[][] rres = RandomnessTest.test(cover, progress, stream);
			
			for (int y = 0; y < tilesHeight; y++) {
				for (int x = 0; x < tilesWidth; x++) {

					float s = 0;

					for (int i = 0; i < tileWidth; i++) {
						for (int j = 0; j < tileHeight; j++) {
							s += Utils.sqr(rres[x * tileWidth + i][y * tileHeight + j] + defaultNoiseScale * nres[x * tileWidth + i][y * tileHeight + j]);
						}
					}
					
					scores[x][y] = s;
				}
			}
			
		}
		
		if (progress != null) {
			progress.setValue(tilesHeight);
		}
		
		return scores;
	}
	
	
	private int[] grabRGB() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		int[] rgb = new int[coverWidth * coverHeight];
		PixelGrabber pg = new PixelGrabber(cover.getImage(), 0, 0, coverWidth, coverHeight, rgb, 0, coverWidth);
		
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for pixels!");
		}
		
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			throw new RuntimeException("Image fetch aborted or errored");
		}
		
		return rgb;
	}
	
	
	private void plotRGB(BufferedImage bi) {
		/*if (grayscale) {
			WritableRaster r = bi.getRaster();
			System.out.println(r.getNumBands());
			for (int y = 0; y < coverHeight; y++) {
				int base = y * coverWidth;
				for (int x = 0; x < coverWidth; x++) {
					r.setSample(x, y, 0, rgb[base + x] & 0xff);
				}
			}
			bi.setData(r);
		}
		else {*/
			for (int y = 0; y < coverHeight; y++) {
				int base = y * coverWidth;
				for (int x = 0; x < coverWidth; x++) {
					bi.setRGB(x, y, rgb[base + x]);
				}
			}
		//}
	}
	
	
	/*
	 * Encoder-specific helper functions
	 */
	
	private BitStream[] prepareFileStreams() throws IOException {
		
		BitStream[] fbs = new BitStream[fileTiles];
		FileInputStream fin = new FileInputStream(file);

		if (progress != null) progress.setIndeterminate(true);
		
		// Prepare the signatures
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not sign the data");
		}
		
		for (int i = 0; i < fileTiles; i++) {
			
			BitStream fs = new FileBitStream(fin, tileCapacity);
			if ((tileCapacity * 8) > fs.size()) fs = new AppendBitStream(fs, new RandomBitStream(paddingRandom, tileCapacity * 8 - fs.size()));
			fs.setStart(0);
			fs.setLimit(fs.size());
			fs.reset();
			BitStream signature = new DataBitStream(md.digest(Utils.captureStream(fs)));
			
			fbs[i] = new AppendBitStream(fs, signature);
		}
		
		fin.close();
		
		return fbs;
	}
	
	
	private BitStream[] generateStreams() {
		
		BitStream[] xbs = new BitStream[fileTiles];
		float[] rnd = new float[fileTiles];
		
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setMinimum(0);
			progress.setMaximum(fileTiles);
			progress.setValue(0);
		}
		
		byte[] seedBytes = new byte[3];
		
		for (int i = 0; i < fileTiles; i++) {
			
			if (progress != null) progress.setValue(i);
			
			BitStream x = null; float r = 1.0f;
			
			while (r > randomnessThreshold) {
				
				seedRandom.nextBytes(seedBytes);
				int seed = (Utils.byte2int(seedBytes[2]) << 16) | (Utils.byte2int(seedBytes[1]) << 8) | (Utils.byte2int(seedBytes[0]));
				
				x = new AppendBitStream(new DataBitStream(seedBytes), new XORBitStream(fileStreams[i], new RandomBitStream(new Random(seed), fileStreams[i].size())));
				if (tileBits > x.size()) x = new AppendBitStream(x, new RandomBitStream(paddingRandom, tileBits - x.size()));
				x.setStart(0);
				x.setLimit(x.size());
				
				r = RandomnessTest.test(x);
			}
			
			xbs[i] = x;
			rnd[i] = r;
		}
		
		if (progress != null) {
			progress.setValue(fileTiles);
		}
		
		randomnessStreams = rnd;
		
		return xbs;
	}
	
	
	private float determineDataPlacement() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		float requiredGap = defaultGap / FLOAT_TO_INT;
		
		for (int i = fileTiles; i < randomnessFlat.length - 1; i++) {
			if (randomnessFlat[i] < leastRandom) continue;
			if (randomnessFlat[i] - randomnessFlat[i + 1] < requiredGap) return randomnessFlat[i] + requiredGap / 2;
		}
		return randomnessFlat[randomnessFlat.length - 1] + requiredGap / 2;
	}
	
	
	private void writeStreams() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		int i = 0;
		if (i >= streams.length) return;
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				
				if (randomness[x][y] > dataPlacement) continue;
				if (dataPlaced[x][y]) continue;
				
				BitStream s = streams[i];
				dataPlaced[x][y] = true;
				s.setStart(0);
				s.setLimit(s.size());
				s.reset();
				
				for (int yi = 0; yi < tileHeight; yi++) {
					int base = (y * tileHeight + yi) * coverWidth + x * tileWidth;
					
					for (int xi = 0; xi < tileWidth; xi++) {
						int pixel = rgb[base + xi];
						if (!grayscale) {
							int red   = (pixel >> 16) & 0xff;
							int green = (pixel >>  8) & 0xff;
							int blue  = (pixel      ) & 0xff;
							int dred  = 0, dgreen = 0, dblue = 0;
							
							for (int k = lsbBits - 1; k >= 0; k--)
								dred   |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							for (int k = lsbBits - 1; k >= 0; k--)
								dgreen |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							for (int k = lsbBits - 1; k >= 0; k--)
								dblue  |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;

							red     = encodeXLSB2(red  , dred  , x * tileWidth + xi, y * tileHeight + yi, 0);
							green   = encodeXLSB2(green, dgreen, x * tileWidth + xi, y * tileHeight + yi, 1);
							blue    = encodeXLSB2(blue , dblue , x * tileWidth + xi, y * tileHeight + yi, 2);
							rgb[base + xi] = (red << 16) | (green << 8) | (blue);
						}
						else {
							int Y = pixel & 0xff;
							int dY = 0;
							for (int k = lsbBits - 1; k >= 0; k--)
								dY  |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							Y   = encodeXLSB2(Y, dY, x * tileWidth + xi, y * tileHeight + yi, 0);
							rgb[base + xi] = (Y << 16) | (Y << 8) | (Y);
						}
					}
				}
				
				i++;
				if (i >= streams.length) return;
			}
		}
		
		throw new IllegalStateException("The secret file is too big");
	}
	
	
	private void writeHeader() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		// Generate the header
		
		byte[] header = new byte[tileCapacity + 16];
		
		// bytes 0 - 3: leastRandomInt
		header[0] = (byte)((leastRandomInt      ) & 0xff);
		header[1] = (byte)((leastRandomInt >>  8) & 0xff);
		header[2] = (byte)((leastRandomInt >> 16) & 0xff);
		header[3] = (byte)((leastRandomInt >> 24) & 0xff);
		
		// bytes 4 - 7: file size
		header[4] = (byte)((fileSize      ) & 0xff);
		header[5] = (byte)((fileSize >>  8) & 0xff);
		header[6] = (byte)((fileSize >> 16) & 0xff);
		header[7] = (byte)((fileSize >> 24) & 0xff);
		
		// Padding
		for (int i = 8; i < header.length - 16; i++) {
			header[i] = (byte)(paddingRandom.nextInt() & 0xff);
		}
		
		// Clear the rest
		for (int i = header.length - 16; i < header.length; i++) {
			header[i] = (byte)0;
		}
		
		// Sign the header (allocate 16 bytes)
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not sign the data header");
		}
		
		byte[] digest = md.digest(header);
		if (digest.length > 16) throw new IllegalStateException("MD5 signature is too long");
		for (int i = 0; i < Math.max(digest.length, 16); i++) {
			header[header.length - 16 + i] = digest[i];
		}
		
		BitStream headerStream = new DataBitStream(header);
		
		// Write the header
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				
				if (randomness[x][y] > dataPlacement) continue;
				if (dataPlaced[x][y]) continue;
				dataPlaced[x][y] = true;
				
				BitStream s = null; float r = 1.0f;
				byte[] seedBytes = new byte[3];
				
				while (r > randomnessThreshold) {
					
					seedRandom.nextBytes(seedBytes);
					int seed = (Utils.byte2int(seedBytes[2]) << 16) | (Utils.byte2int(seedBytes[1]) << 8) | (Utils.byte2int(seedBytes[0]));
					
					s = new AppendBitStream(new DataBitStream(seedBytes), new XORBitStream(headerStream, new RandomBitStream(new Random(seed), headerStream.size())));
					s.setStart(0);
					s.setLimit(s.size());
					
					r = RandomnessTest.test(s);
				}
				
				s.reset();
				
				for (int yi = 0; yi < tileHeight; yi++) {
					int base = (y * tileHeight + yi) * coverWidth + x * tileWidth;
					
					for (int xi = 0; xi < tileWidth; xi++) {
						int pixel = rgb[base + xi];
						if (!grayscale) {
							int red   = (pixel >> 16) & 0xff;
							int green = (pixel >>  8) & 0xff;
							int blue  = (pixel      ) & 0xff;
							int dred  = 0, dgreen = 0, dblue = 0;
							
							for (int k = lsbBits - 1; k >= 0; k--)
								dred   |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							for (int k = lsbBits - 1; k >= 0; k--)
								dgreen |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							for (int k = lsbBits - 1; k >= 0; k--)
								dblue  |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							
							red     = encodeXLSB2(red  , dred  , x * tileWidth + xi, y * tileHeight + yi, 0);
							green   = encodeXLSB2(green, dgreen, x * tileWidth + xi, y * tileHeight + yi, 1);
							blue    = encodeXLSB2(blue , dblue , x * tileWidth + xi, y * tileHeight + yi, 2);
							rgb[base + xi] = (red << 16) | (green << 8) | (blue);
						}
						else {
							int Y = pixel & 0xff;
							int dY = 0;
							for (int k = lsbBits - 1; k >= 0; k--)
								dY  |= (s.hasNext() ? s.next() : paddingRandom.nextInt(2)) << k;
							Y   = encodeXLSB2(Y, dY, x * tileWidth + xi, y * tileHeight + yi, 0);
							rgb[base + xi] = (Y << 16) | (Y << 8) | (Y);
						}
					}
				}
				
				return;
			}
		}
		
		throw new IllegalStateException("Could not find a place to write the header!");
	}
	
	
	private void corruptForeignStreams() {
		
		if (progress != null) progress.setIndeterminate(true);
		
		byte[][] data = new byte[coverTiles][tileBytes];
		
		int tile = 0;
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				
				int d = 0, bits = 0, di = 0;
				
				for (int yi = 0; yi < tileHeight; yi++) {
					int base = (y * tileHeight + yi) * coverWidth + x * tileWidth;
					
					for (int xi = 0; xi < tileWidth; xi++) {
						int pixel = rgb[base + xi];
						
						if (!grayscale) {
							int red   = (pixel >> 16) & 0xff;
							int green = (pixel >>  8) & 0xff;
							int blue  = (pixel      ) & 0xff;
							
							for (int k = lsbBits - 1; k >= 0; k--) {
								d |= ((red   >> k) & 1) << (bits++);
								if (bits == 8) { data[tile][di++] = (byte)d; d = bits = 0; }
							}
							
							for (int k = lsbBits - 1; k >= 0; k--) {
								d |= ((green >> k) & 1) << (bits++);
								if (bits == 8) { data[tile][di++] = (byte)d; d = bits = 0; }
							}
							
							for (int k = lsbBits - 1; k >= 0; k--) {
								d |= ((blue  >> k) & 1) << (bits++);
								if (bits == 8) { data[tile][di++] = (byte)d; d = bits = 0; }
							}
						}
						else {
							int Y = pixel & 0xff;
							
							for (int k = lsbBits - 1; k >= 0; k--) {
								d |= ((Y >> k) & 1) << (bits++);
								if (bits == 8) { data[tile][di++] = (byte)d; d = bits = 0; }
							}
						}
					}
				}
				
				tile++;
			}
		}
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not verify data signatures");
		}
		
		int di = 0;
		byte[] buffer = new byte[tileCapacity];
		byte[] signature = md.digest(buffer);
		if (signature.length > 16) throw new IllegalStateException("MD5 signature is too long");
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				
				if (dataPlaced[x][y]) {
					di++;
					continue;
				}
				
				byte[] block = Utils.captureStream(AdvDecoder.getDecodedStream(data[di++]));
				
				// Test to see if the block contains data
					
				System.arraycopy(block, 0, buffer, 0, tileCapacity);
				System.arraycopy(block, tileCapacity, signature, 0, signature.length);
				byte[] digest = md.digest(buffer);
					
				if (MessageDigest.isEqual(digest, signature)) {
					rgb[y * tileWidth * coverWidth + x * tileWidth] ^= 0x333;
					continue;
				}
					
				// Test to see if the block contains the header
					
				for (int i = 0; i < signature.length; i++) {
					signature[i] = block[block.length - 16 + i];
					block[block.length - 16 + i] = (byte)0;
				}
					
				digest = md.digest(block);
					
				if (MessageDigest.isEqual(digest, signature)) {
					rgb[y * tileWidth * coverWidth + x * tileWidth] ^= 0x333;
				}
			}
		}
	}
	
	
	private int encodeXLSB2(int color, int data, int x, int y, int plane) {
		
		// Normalize the color intensity
		
		int c = color;
		if (c >= lsbBitsNorm) c -= lsbBitsNorm;
		
		// Prepare the data
		
		int d = data - (c & lsbBitsMask);
		if (d < 0) d += lsbBitsCMax;
		
		// Encode the data
		
		c += d;
		if (c > 0xff) c -= lsbBitsCMax;
		
		// Normalize towards the Gaussian mean
		
		if (gaussianBase != null) {
			int g = gaussianBase[coverWidth * y + x];
			switch (plane) {
				case 0: g = (g >> 16) & 0xff; break;
				case 1: g = (g >>  8) & 0xff; break;
				case 2: g =  g        & 0xff; break;
				default: new RuntimeException("Invalid index of data plane: " + plane);
			}
			if (c + lsbBitsCMax / 2 < g) c += lsbBitsCMax;
			if (c - lsbBitsCMax / 2 > g) c -= lsbBitsCMax;
		}

		if (c <    0) c += lsbBitsCMax;
		if (c > 0xff) c -= lsbBitsCMax;
		
		return c;
	}
}
