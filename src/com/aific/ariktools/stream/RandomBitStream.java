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


import java.util.Random;

public class RandomBitStream extends BitStream {

	@SuppressWarnings("unused")
	private Random rand;
	private int[] rnd;
	
	public RandomBitStream(int size) {
		this(new Random(), size);
	}
	
	public RandomBitStream(Random rand, int size) {
		super();
		this.size = size;
		rnd = new int[size / 16 + 1];
		this.rand = rand;
		for (int i = 0; i < rnd.length; i++) {
			rnd[i] = rand.nextInt();
		}
	}
	
	protected byte get(int index) {
		return (byte)((rnd[index / 16] >> (index % 16)) & 1);
	}
}
