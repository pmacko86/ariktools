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


import com.aific.ariktools.util.Utils;

public class DataBitStream extends BitStream {
	
	private int[] data;
	
	public DataBitStream(byte[] data) {
		this(data, 0);
	}
	
	public DataBitStream(byte[] data, int dataOffset) {
		super();
		this.size = (data.length - dataOffset) * 8;
		this.data = new int[data.length - dataOffset];
		for (int i = 0; i < data.length - dataOffset; i++) {
			this.data[i] = Utils.byte2int(data[i + dataOffset]);
		}
	}
	
	protected byte get(int index) {
		return (byte)((data[index / 8] >> (index % 8)) & 1);
	}
	
}
