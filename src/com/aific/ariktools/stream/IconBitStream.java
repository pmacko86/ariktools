package com.aific.ariktools.stream;

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
import javax.swing.ImageIcon;

public class IconBitStream extends RGBBitStream {

	public static final int DEFAULT_RANGE = 9;
	public static final int DEFAULT_BITS = 2;
	
	protected ImageIcon icon;
	protected int width, height;
	
	protected int rangeLinear;
	protected int rangeWidth;
	protected int rangeHeight;
	
	protected int startIndex;
	protected boolean squareMode;
	protected int startX, startY, squareWidth, squareHeight;
	
	public IconBitStream(ImageIcon icon) {
		this(icon, DEFAULT_BITS, false);
	}
	
	public IconBitStream(ImageIcon icon, boolean grayscale) {
		this(icon, DEFAULT_BITS, grayscale);
	}
	
	public IconBitStream(ImageIcon icon, int bits, boolean grayscale) {
		super(bits, grayscale);
		this.icon = icon;
		width = icon.getIconWidth();
		height = icon.getIconHeight();
		rangeLinear = DEFAULT_RANGE;
		rangeWidth = DEFAULT_RANGE;
		rangeHeight = DEFAULT_RANGE;
		squareMode = true;
		generateRGB();
	}
	
	private void generateRGB() {
		size = width * height * bits * dataPlanes;
		rgb = new int[width * height];
		PixelGrabber pg = new PixelGrabber(icon.getImage(), 0, 0, width, height, rgb, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted waiting for pixels!");
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			throw new RuntimeException("image fetch aborted or errored");
		}
	}
	
	public int getLinearRange() {
		return rangeLinear;
	}
	
	public int getRangeWidth() {
		return rangeWidth;
	}
	
	public int getRangeHeight() {
		return rangeHeight;
	}
	
	public void setLinearRange(int range) {
		if (range <= 0) throw new IllegalArgumentException("The range is too small");
		this.rangeLinear = range;
	}
	
	public void setSquareRange(int rangeWidth, int rangeHeight) {
		if (rangeWidth <= 0 || rangeHeight <= 0) throw new IllegalArgumentException("The range is too small");
		this.rangeWidth = rangeWidth;
		this.rangeHeight = rangeHeight;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean isSquareMode() {
		return squareMode;
	}
	
	public void setSquareMode(boolean squareMode) {
		this.squareMode = squareMode;
	}
	
	protected byte get(int index) {
		if (!squareMode) return super.get(index);
		int pixel = index / (dataPlanes * bits);
		int within = index % (dataPlanes * bits);
		int x = pixel % squareWidth;
		int y = pixel / squareWidth;
		int n = (width * (startY + y) + startX + x) * bits * dataPlanes + within;
		try {
			return super.get(n);
		}
		catch (Exception e) {
			System.out.println("\nException while reading from an icon stream:");
			e.printStackTrace();
			System.out.println("index = " + index + ", x = " + x + " / " + squareWidth + ", y = " + y + " / " + squareHeight + ", n = " + n + " / " + size);
			throw new RuntimeException("Exception while reading from an icon stream");
		}
	}
	
	public void preparePixel(int x, int y) {
		if (!squareMode) {
			startIndex = (width * y + x) * bits * dataPlanes;
			int index = startIndex - (rangeLinear * dataPlanes * bits) / 2;
			int limit = rangeLinear;
			if (index < 0) {
				limit += index;
				index = 0;
			}
			if (index + limit > size) {
				limit = size - index;
			}
			setLimit(limit);
			setStart(index);
			setOffset(0);
		}
		else {
			startX = x - rangeWidth / 2;
			startY = y - rangeHeight / 2;
			squareWidth = rangeWidth;
			squareHeight = rangeHeight;
			if (startX < 0) {
				squareWidth += startX;
				startX = 0;
			}
			if (startY < 0) {
				squareHeight += startY;
				startY = 0;
			}
			if (startX + squareWidth >= width) {
				squareWidth = width - startX;
			}
			if (startY + squareHeight >= height) {
				squareHeight = height - startY;
			}
			startIndex = (width * startY + startX) * bits * dataPlanes;
			setLimit(squareWidth * squareHeight * dataPlanes * bits);
			setOffset(0);
			setStart(0);
		}
	}
}
