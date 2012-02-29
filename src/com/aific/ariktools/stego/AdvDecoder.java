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


import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.aific.ariktools.stream.BitStream;
import com.aific.ariktools.stream.DataBitStream;
import com.aific.ariktools.stream.RandomBitStream;
import com.aific.ariktools.stream.XORBitStream;
import com.aific.ariktools.util.Utils;

public class AdvDecoder {
	
	private ImageIcon cover;
	private File file;
	
	private int[] rgb;
	
	private int coverWidth;
	private int coverHeight;
	private int tilesWidth;
	private int tilesHeight;
	
	private int tileWidth;
	private int tileHeight;
	private int tileBytes;
	private int tileCapacity;
	private int coverCapacity;
	private int coverTiles;
	
	private boolean grayscale;
	private int dataPlanes;
	
	private JProgressBar progress;
	private JLabel label;

	private byte[][] data;
	
	@SuppressWarnings("unused")
	private float leastRandom;
	private int leastRandomInt;
	private int fileSize;
	@SuppressWarnings("unused")
	private int fileTiles;
	
	@SuppressWarnings("unused")
	private int headerX;
	@SuppressWarnings("unused")
	private int headerY;
	
	private int lsbBits;
	
	
	public AdvDecoder(ImageIcon cover, File file) {
		this.cover = cover;
		this.file = file;
		
		if (file == null) throw new IllegalArgumentException("Output file is not specified");
		if (cover == null) throw new IllegalArgumentException("Cover image is not loaded");
		
		grayscale = Utils.isGrayscale(cover);
		dataPlanes = grayscale ? 1 : 3;
		
		tileWidth = AdvEncoder.defaultTileWidth;
		tileHeight = AdvEncoder.defaultTileHeight;
		
		lsbBits = AdvEncoder.getLSBBits();
		
		if (grayscale) {
			tileWidth *= AdvEncoder.defaultGrayscaleWidthFactor;
			tileHeight *= AdvEncoder.defaultGrayscaleHeightFactor;
		}
		
		if (lsbBits == 1) {
			tileWidth *= AdvEncoder.defaultLSB1WidthFactor;
			tileHeight *= AdvEncoder.defaultLSB1HeightFactor;
		}

		coverWidth = cover.getIconWidth();
		coverHeight = cover.getIconHeight();
		tilesWidth = coverWidth / tileWidth;
		tilesHeight = coverHeight / tileHeight;
		if ((tilesWidth <= 0) || (tilesHeight <= 0)) throw new IllegalArgumentException("The cover image is too small");
		
		tileBytes = dataPlanes * tileWidth * tileHeight * lsbBits / 8;
		tileCapacity = tileBytes - 3 - 16;
		coverTiles = tilesWidth * tilesHeight;
		coverCapacity = (coverTiles - 1) * tileCapacity;
		if (coverCapacity >= 16 * 1048576) coverCapacity = 16 * 1048576 - 1;
		
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
	 * Decoder
	 */
	
	public void decode() throws IOException {
		
		if (label != null) label.setText("Starting");
		if (progress != null) progress.setIndeterminate(true);
		
		rgb = grabRGB();
		data = getData();
		
		if (label != null) label.setText("Decoding");
		
		decodeData();

		if (label != null) label.setText("(idle)");
	}
	
	
	/*
	 * Helper functions
	 */
	
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
	
	
	private byte[][] getData() {
		
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
						
		return data;
	}
	
	
	public static BitStream getDecodedStream(byte[] data) {
		if (data.length < 4) throw new RuntimeException("The stream is too small");
		int seed = (Utils.byte2int(data[2]) << 16) | (Utils.byte2int(data[1]) << 8) | (Utils.byte2int(data[0]));
		BitStream s = new XORBitStream(new DataBitStream(data, 3), new RandomBitStream(new Random(seed), (data.length - 3) * 8));
		s.setStart(0);
		s.setLimit(s.getSize());
		return s;
	}
	
	
	private void decodeData() throws IOException {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not verify data signatures");
		}
		
		int di = 0, bytesLeft = 0;
		boolean headerFound = false;
		FileOutputStream f = null;
		byte[] buffer = new byte[tileCapacity];
		byte[] signature = md.digest(buffer);
		if (signature.length > 16) throw new IllegalStateException("MD5 signature is too long");
		
		for (int y = 0; y < tilesHeight; y++) {
			for (int x = 0; x < tilesWidth; x++) {
				
				byte[] block = Utils.captureStream(getDecodedStream(data[di++]));
				
				if (headerFound) {
					
					// Test to see if the block contains data
					
					System.arraycopy(block, 0, buffer, 0, tileCapacity);
					System.arraycopy(block, tileCapacity, signature, 0, signature.length);
					byte[] digest = md.digest(buffer);

					if (MessageDigest.isEqual(digest, signature)) {
						
						// Data were found; write them to the file
						
						if (bytesLeft >= buffer.length) {
							f.write(buffer);
							bytesLeft -= buffer.length;
						}
						else {
							byte[] buffer2 = new byte[bytesLeft];
							for (int i = 0; i < bytesLeft; i++) buffer2[i] = buffer[i];
							f.write(buffer2);
							bytesLeft = 0;
						}
						
						if (bytesLeft == 0) {
							f.close();
							return;
						}
					}
				}
				else {
					
					// Test to see if the block contains the header
					
					for (int i = 0; i < signature.length; i++) {
						signature[i] = block[block.length - 16 + i];
						block[block.length - 16 + i] = (byte)0;
					}
					
					byte[] digest = md.digest(block);
					
					if (MessageDigest.isEqual(digest, signature)) {

						// The header was found; parse it
						
						headerX = x;
						headerY = y;
						
						leastRandomInt = (Utils.byte2int(block[3]) << 24) | (Utils.byte2int(block[2]) << 16) | (Utils.byte2int(block[1]) << 8) | (Utils.byte2int(block[0]));
						fileSize = (Utils.byte2int(block[7]) << 24) | (Utils.byte2int(block[6]) << 16) | (Utils.byte2int(block[5]) << 8) | (Utils.byte2int(block[4]));

						leastRandom = leastRandomInt / AdvEncoder.FLOAT_TO_INT;
						fileTiles = fileSize / tileCapacity + (fileSize % tileCapacity == 0 ? 0 : 1);
						
						headerFound = true;
						bytesLeft = fileSize;
						
						// Open the output file
						
						f = new FileOutputStream(file);
					}
				}
			}
		}
		
		if (f != null) f.close();
		
		if (headerFound)
			throw new RuntimeException("Could not recover the secret file");
		else
			throw new RuntimeException("No secret data were found");
	}

}
