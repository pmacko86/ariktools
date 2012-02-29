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


public class RGBBitStream extends BitStream {

	protected int[] rgb;
	protected int bits;
	protected boolean grayscale;
	protected int dataPlanes;

	protected RGBBitStream(int bits, boolean grayscale) {
		super();
		this.bits = bits;
		this.rgb = null;
		this.size = 0;
		this.grayscale = grayscale;
		this.dataPlanes = this.grayscale ? 1 : 3;
	}
	
	public RGBBitStream(int[] rgb, int bits) {
		super();
		this.bits = bits;
		this.rgb = rgb;
		this.size = rgb.length * bits * 3;
	}
	
	protected byte get(int index) {
		int pixel = rgb[index / (bits * dataPlanes)];
		int colorIndex = (index % (bits * dataPlanes)) / bits;
		int color = pixel >> (colorIndex * 8);
		return (byte)((color >> (index % bits)) & 1);
	}
	
}
